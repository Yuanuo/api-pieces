package org.appxi.api.pieces.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.appxi.api.pieces.model.Piece;
import org.appxi.api.pieces.repo.db.PiecePersistRepository;
import org.appxi.api.pieces.repo.solr.PieceSolrRepository;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
class ServiceController {
    private static final Log logger = LogFactory.getLog(ServiceController.class);

    private final PiecePersistRepository persistRepository;
    private final PieceSolrRepository solrRepository;
    private final Set<String> allowedClients;

    public ServiceController(PiecePersistRepository persistRepository, PieceSolrRepository solrRepository, Set<String> allowedClients) {
        this.persistRepository = persistRepository;
        this.solrRepository = solrRepository;
        this.allowedClients = allowedClients;
    }

    @GetMapping("/piece/{id}")
    public Piece detail(@PathVariable("id") String id,
                        HttpServletResponse resp) throws IOException {
        if (StringUtils.isBlank(id)) {
            resp.sendError(400);
            return null;
        }
        Piece result = persistRepository.findById(id).orElse(null);
        if (null == result)
            resp.sendError(404);
        return result;
    }

    @PostMapping("/piece")
    public String create(@RequestBody List<Piece> entries,
                         HttpServletRequest request, HttpServletResponse resp) throws IOException {
        if (isNotAllowedClient(request, resp))
            return null;
        if (null == entries || entries.isEmpty()) {
            resp.sendError(400);
            return null;
        }
        Iterable<Piece> savedList = persistRepository.saveAll(entries);
        solrRepository.saveAll(savedList);
        return "created";
    }

    @PutMapping("/piece")
    public String update(@RequestBody List<Piece> entries,
                         HttpServletRequest request, HttpServletResponse resp) throws IOException {
        if (isNotAllowedClient(request, resp))
            return null;
        if (null == entries || entries.isEmpty()) {
            resp.sendError(400);
            return null;
        }
        Iterable<Piece> savedList = persistRepository.saveAll(entries);
        solrRepository.saveAll(savedList);
        return "updated";
    }

    @DeleteMapping("/piece/{id}")
    public String delete(@PathVariable("id") String id,
                         HttpServletRequest request, HttpServletResponse resp) throws IOException {
        if (isNotAllowedClient(request, resp))
            return null;
        if (StringUtils.isBlank(id)) {
            resp.sendError(400);
            return null;
        }
        String result = null;
        try {
            persistRepository.deleteById(id);
            result = "deleted";
        } catch (Exception e) {
            // ignore
        }
        try {
            solrRepository.deleteById(id);
            result = "deleted";
        } catch (Exception e) {
            // ignore
        }
        if (null == result)
            resp.sendError(404);
        return result;
    }


    @PostMapping("/service/ip/{ip}")
    public String serviceIpAdd(@PathVariable("ip") String ip,
                               HttpServletRequest request, HttpServletResponse resp) throws IOException {
        if (isNotAllowedClient(request, resp))
            return null;
        allowedClients.add(ip);
        return "added";
    }

    @DeleteMapping("/service/ip/{ip}")
    public String serviceIpRemove(@PathVariable("ip") String ip,
                                  HttpServletRequest request, HttpServletResponse resp) throws IOException {
        if (isNotAllowedClient(request, resp))
            return null;
        allowedClients.remove(ip);
        return "removed";
    }

    private boolean isNotAllowedClient(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String remoteIp = req.getRemoteAddr();
        boolean allowed = allowedClients.contains(remoteIp);
        if (!allowed) {
            logger.warn("Denied: " + remoteIp);
            resp.sendError(403);
        }
        return !allowed;
    }
}
