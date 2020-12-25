package org.appxi.api.pieces.repo.solr;

import org.appxi.api.pieces.model.Piece;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class PieceSolrRepositoryEx {

    @Resource
    private SolrTemplate solrTemplate;

    public List<Piece> dynamicSearch(String searchTerm) {
        Criteria conditions = createConditions(searchTerm);
        SimpleQuery search = new SimpleQuery(conditions);

        search.addSort(sortByIdDesc());

        Page<Piece> results = solrTemplate.queryForPage("Order", search, Piece.class);
        return results.getContent();
    }

    private Criteria createConditions(String searchTerm) {
        Criteria conditions = null;

        for (String term : searchTerm.split(" ")) {
            if (conditions == null) {
                conditions = new Criteria("oid").contains(term)
                        .or(new Criteria("odesc").contains(term));
            } else {
                conditions = conditions.or(new Criteria("oid").contains(term))
                        .or(new Criteria("odesc").contains(term));
            }
        }
        return conditions;
    }

    private Sort sortByIdDesc() {
        return Sort.by(Sort.Direction.DESC, "oid");
    }

}
