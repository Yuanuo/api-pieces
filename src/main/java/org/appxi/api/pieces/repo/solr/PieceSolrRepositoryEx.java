package org.appxi.api.pieces.repo.solr;

import org.appxi.api.pieces.model.Piece;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class PieceSolrRepositoryEx {

    @Resource
    private SolrTemplate solrTemplate;

    public Page<Piece> searchByJson(String project, String jsonTxt) {
        final JSONObject jsonObj = new JSONObject(jsonTxt);
        Criteria conditions = new Criteria("project_s").is(project);

        if (jsonObj.has("query")) {
            conditions = conditions.and("name_s").is(jsonObj.getString("query"));
        } else if (jsonObj.has("match")) {
            Object matchObj = jsonObj.get("match");
            if (matchObj instanceof JSONObject map) {
                for (String key : map.keySet()) {
                    conditions = buildConditions(conditions, map, key, true);
                }
            } else if (matchObj instanceof JSONArray lst) {
                for (Object obj : lst) {
                    if (obj instanceof JSONObject map) {
                        for (String key : map.keySet()) {
                            conditions = buildConditions(conditions, map, key, false);
                        }
                    }
                }
            }
        } else {
            return null;
        }

        final SimpleQuery query = new SimpleQuery(conditions);
        final Pageable pageable = PageRequest.of(jsonObj.optInt("page", 0), jsonObj.optInt("size", 5));
        query.setPageRequest(pageable);
        return solrTemplate.queryForPage("pieces", query, Piece.class);
    }

    private static Criteria buildConditions(Criteria conditions, JSONObject map, String key, boolean defaultOpIsAnd) {
        if (key.startsWith("AND "))
            conditions = conditions.and(key.substring(4));
        else if (key.startsWith("OR "))
            conditions = conditions.or(key.substring(3));
        else conditions = defaultOpIsAnd ? conditions.and(key) : conditions.or(key);

        String val = map.getString(key);
        if (val.startsWith("is "))
            conditions = conditions.is(val.substring(3));
        else if (val.startsWith("startsWith "))
            conditions = conditions.startsWith(val.substring(11));
        else if (val.startsWith("endsWith "))
            conditions = conditions.endsWith(val.substring(9));
        else if (val.startsWith("contains "))
            conditions = conditions.contains(val.substring(9));
        else conditions = conditions.is(val);
        return conditions;
    }
}
