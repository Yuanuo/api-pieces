package org.appxi.api.pieces.repo.solr;

import org.appxi.api.pieces.model.Piece;
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
        final Criteria mustProject = new Criteria("project_s").is(project);

        Criteria conditions = null;
        if (jsonObj.has("query")) {
            conditions = new Criteria("name_s").is(valObjects(jsonObj.getString("query")));
        } else if (jsonObj.has("match")) {
            final JSONObject map = jsonObj.getJSONObject("match");
            for (String mapKey : map.keySet()) {
                conditions = buildConditions(conditions, map, mapKey);
            }
        }
        if (null == conditions) {
            return null;
        }

        final SimpleQuery query = new SimpleQuery(mustProject.and(conditions));
        final Pageable pageable = PageRequest.of(jsonObj.optInt("page", 0), jsonObj.optInt("size", 5));
        query.setPageRequest(pageable);
        return solrTemplate.queryForPage("pieces", query, Piece.class);
    }

    private static Criteria buildConditions(Criteria conditions, final JSONObject map, final String key) {
        String field = key;
        boolean opIsAnd;
        // clean prefix, it used for order only
        if (field.matches("^\\d+ .*")) {
            field = field.substring(field.indexOf(' ') + 1);
        }

        if (field.equals("AND") || field.equals("OR")) {
            final JSONObject subMap = map.getJSONObject(key);
            Criteria subConditions = null;
            for (String subMapKey : subMap.keySet()) {
                subConditions = buildConditions(subConditions, subMap, subMapKey);
            }
            if (null != subConditions) {
                if (null == conditions) {
                    conditions = subConditions;
                } else {
                    conditions = field.equals("AND") ? conditions.and(subConditions) : conditions.or(subConditions);
                }
            }
            return conditions;
        } else if (field.startsWith("AND ")) {
            field = field.substring(4);
            opIsAnd = true;
        } else if (field.startsWith("OR ")) {
            field = field.substring(3);
            opIsAnd = false;
        } else {
            field = field;
            opIsAnd = true;
        }
        if (null == conditions) {
            conditions = new Criteria(field);
        } else {
            conditions = opIsAnd ? conditions.and(field) : conditions.or(field);
        }

        final String val = map.getString(key);
        if (val.startsWith("is "))
            conditions = conditions.is(valObjects(val.substring(3)));
        else if (val.startsWith("in "))
            conditions = conditions.in(valObjects(val.substring(3)));
        else if (val.startsWith("startsWith "))
            conditions = conditions.startsWith(val.substring(11).split("\\|"));
        else if (val.startsWith("endsWith "))
            conditions = conditions.endsWith(val.substring(9).split("\\|"));
        else if (val.startsWith("contains "))
            conditions = conditions.contains(val.substring(9).split("\\|"));
        else if (val.equals("isNull"))
            conditions = conditions.isNull();
        else if (val.equals("isNotNull"))
            conditions = conditions.isNotNull();
        else conditions = conditions.is(valObjects(val));
        return conditions;
    }

    private static Object[] valObjects(String val) {
        return val.split("\\|");
    }
}
