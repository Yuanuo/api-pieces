package org.appxi.api.pieces.repo.solr;

import org.appxi.api.pieces.model.Piece;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.repository.Facet;
import org.springframework.data.solr.repository.Highlight;
import org.springframework.data.solr.repository.SolrCrudRepository;

import java.util.Collection;

public interface PieceSolrRepository extends SolrCrudRepository<Piece, String> {

    Page<Piece> findByProjectAndNameIn(String project, Collection<String> names, Pageable pageable);

    Page<Piece> findByProjectAndTypeInAndNameIn(String project, Collection<String> types, Collection<String> names, Pageable pageable);

    @Facet(fields = {"name"})
    FacetPage<Piece> findByProjectAndNameStartsWith(String project, Collection<String> nameFragments, Pageable pageable);

    @Facet(fields = {"name"})
    FacetPage<Piece> findByProjectAndTypeInAndNameStartsWith(String project, Collection<String> types, Collection<String> nameFragments, Pageable pageable);


    //    @Query(fields = {SearchableDictionary.ID_FIELD_NAME, SearchableDictionary.NAME_FIELD_NAME,
//            SearchableDictionary.TEXT_FIELD_NAME, SearchableDictionary.LOCATION_FIELD_NAME
//    }, defaultOperator = Operator.AND)
    @Highlight(prefix = "<b>", postfix = "</b>")
    HighlightPage<Piece> findByNameInAndProject(Collection<String> names, String project, Pageable pageable);

//    @Query("odesc:*?0*")
//    Page<Piece> findByOrderDescription(String searchTerm, Pageable pageable);

//    @Query("odesc:*?0* OR oname:*?0* OR pname:*?0*")
//    Page<Piece> findByCustomerQuery(String searchTerm, Pageable pageable);

//    @Query(name = "Order.findByNamedQuery")
//    Page<Order> findByNamedQuery(String searchTerm, Pageable pageable);
}
