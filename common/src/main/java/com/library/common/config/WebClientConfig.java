package com.library.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    @Value("${internal.api-key.header-name}")
    private String internalApiKeyHeaderName;

    @Value("${internal.api-key.value}")
    private String internalApiKeyValue;

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .filter(addApiKeyHeaderFilter())
                .build();
    }

    private ExchangeFilterFunction addApiKeyHeaderFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                return Mono.just(ClientRequest.from(clientRequest)
                        .header(internalApiKeyHeaderName, internalApiKeyValue)
                        .build());
        });
    }
}