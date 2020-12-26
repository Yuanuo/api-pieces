package org.appxi.api.pieces.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.appxi.api.pieces.model.Piece;
import org.appxi.api.pieces.repo.solr.PieceSolrRepository;
import org.appxi.api.pieces.repo.solr.PieceSolrRepositoryEx;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetFieldEntry;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
class SearchController {
    private static final Log logger = LogFactory.getLog(SearchController.class);
    private static final Pattern IGNORED_CHARS_PATTERN = Pattern.compile("\\p{Punct}");
    private final PieceSolrRepository solrRepository;
    private final PieceSolrRepositoryEx solrRepositoryEx;

    public SearchController(PieceSolrRepository solrRepository, PieceSolrRepositoryEx solrRepositoryEx) {
        this.solrRepository = solrRepository;
        this.solrRepositoryEx = solrRepositoryEx;
    }

    @GetMapping("/detail/{id}")
    public Piece detail(@PathVariable("id") String id,
                        HttpServletResponse resp) throws IOException {
        if (StringUtils.isBlank(id)) {
            resp.sendError(400);
            return null;
        }
        Piece result = solrRepository.findById(id).orElse(null);
        if (null == result)
            resp.sendError(404);
        return result;
    }

    @GetMapping(value = "/search/{project}")
    public Page<Piece> search(@PathVariable("project") String project,
                              @RequestParam(value = "q") String query,
                              @RequestParam(value = "t", required = false) String type,
                              @PageableDefault(page = 0, size = 5) Pageable pageable,
                              HttpServletResponse resp) throws IOException {
        if (StringUtils.isBlank(query)) {
            resp.sendError(400, "q is empty");
            return null;
        }
        logger.info("search: " + project + "/" + query);

        Collection<String> names = preparedSearchTerms(query);
        Collection<String> types = preparedSearchTypes(type);
        if (null == types)
            return solrRepository.findByProjectAndNameIn(project, names, pageable);
        return solrRepository.findByProjectAndTypeInAndNameIn(project, types, names, pageable);
    }

    @PostMapping(value = "/search/{project}")
    public Page<Piece> searchByJson(@PathVariable("project") String project,
                                    @RequestBody String jsonTxt,
                                    HttpServletResponse resp) throws IOException {
        if (StringUtils.isBlank(jsonTxt)) {
            resp.sendError(400, "body is empty");
            return null;
        }
        if (jsonTxt.length() > 2048) {
            resp.sendError(400, "body too large(>2k)");
            return null;
        }
        Page<Piece> result = null;
        try {
            result = solrRepositoryEx.searchByJson(project, jsonTxt);
        } catch (Exception e) {
            resp.sendError(400, "body is not json?");
            return null;
        }
        if (null == result) {
            resp.sendError(400, "bad conditions");
            return null;
        }
        return result;
    }

    @GetMapping(value = "/autocomplete/{project}", produces = "application/json")
    public Set<String> autoComplete(@PathVariable("project") String project,
                                    @RequestParam("term") String query,
                                    @RequestParam(value = "t", required = false) String type,
                                    @PageableDefault(page = 0, size = 1) Pageable pageable) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptySet();
        }
        Collection<String> names = preparedSearchTerms(query);
        Collection<String> types = preparedSearchTypes(type);
        FacetPage<Piece> facet;
        if (null == types)
            facet = solrRepository.findByProjectAndNameStartsWith(project, names, pageable);
        else facet = solrRepository.findByProjectAndTypeInAndNameStartsWith(project, types, names, pageable);

        Set<String> result = new LinkedHashSet<>();
        for (Page<FacetFieldEntry> page : facet.getFacetResultPages()) {
            for (FacetFieldEntry entry : page) {
                if (entry.getValue().contains(query)) { // we have to do this as we do not use terms vector or a string field
                    result.add(entry.getValue());
                }
            }
        }
        return result;
    }

    private static Collection<String> preparedSearchTerms(String input) {
        String[] array = StringUtils.split(input, " ");
        List<String> result = new ArrayList<>(array.length);
        for (String item : array) {
            if (StringUtils.isNotEmpty(item)) {
                result.add(IGNORED_CHARS_PATTERN.matcher(item).replaceAll(" "));
            }
        }
        return result;
    }

    private static Collection<String> preparedSearchTypes(String input) {
        if (StringUtils.isBlank(input))
            return null;
        String[] array = StringUtils.split(input, ",");
        List<String> result = new ArrayList<>(array.length);
        for (String item : array) {
            if (StringUtils.isNotEmpty(item)) {
                result.add(item);
            }
        }
        return result;
    }

//    @GetMapping("/piece/desc/{orderDesc}/{page}")
//    public List<Piece> find(@PathVariable String orderDesc, @PathVariable int page) {
//        return reposolr.findByDescription(orderDesc, PageRequest.of(page, 2)).getContent();
//    }
//
//    @GetMapping("/piece/search/{searchTerm}/{page}")
//    public List<Piece> findBySearchTerm(@PathVariable String searchTerm, @PathVariable int page) {
//        return reposolr.findByCustomerQuery(searchTerm, PageRequest.of(page, 2)).getContent();
//    }
}
