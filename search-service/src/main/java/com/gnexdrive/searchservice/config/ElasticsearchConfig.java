package com.gnexdrive.searchservice.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch configuration
 */
@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.host:localhost}")
    private String host;

    @Value("${elasticsearch.port:9200}")
    private int port;

    @Bean
    public RestClient restClient() {
        // TODO: Implement RestClient bean
        return null;
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        // TODO: Implement ElasticsearchTransport bean
        return null;
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        // TODO: Implement ElasticsearchClient bean
        return null;
    }
}
