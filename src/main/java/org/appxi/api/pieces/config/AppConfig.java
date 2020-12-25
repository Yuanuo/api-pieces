package org.appxi.api.pieces.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableJpaRepositories(basePackages = "org.appxi.api.pieces.repo.db")
@EnableSolrRepositories(basePackages = "org.appxi.api.pieces.repo.solr")
class AppConfig {
    @Bean(name = "allowedClients")
    public Set<String> allowedClients(@Value("${allowed.clients}") String targets) {
        final Set<String> serviceTargets = new HashSet<>();
        for (String ip : targets.split(",")) {
            serviceTargets.add(ip.trim());
        }
        return serviceTargets;
    }

    @Bean
    public SolrClient solrClient(@Value("${solr.host}") String solrHost) {
        return new Http2SolrClient.Builder(solrHost).build();
    }

    @Bean
    public SolrTemplate solrTemplate(SolrClient client) throws Exception {
        return new SolrTemplate(client);
    }
}
