package com.library.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator swaggerRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service-api-docs", r -> r.path("/v3/api-docs/user-service")
                        .filters(f -> f.rewritePath("/v3/api-docs/user-service", "/v3/api-docs"))
                        .uri("http://user-service:8881"))
                .route("book-service-api-docs", r -> r.path("/v3/api-docs/book-service")
                        .filters(f -> f.rewritePath("/v3/api-docs/book-service", "/v3/api-docs"))
                        .uri("http://book-service:8880"))
                .route("loan-service-api-docs", r -> r.path("/v3/api-docs/loan-service")
                        .filters(f -> f.rewritePath("/v3/api-docs/loan-service", "/v3/api-docs"))
                        .uri("http://loan-service:8882"))
                .route("auth-service-api-docs", r -> r.path("/v3/api-docs/auth-service")
                        .filters(f -> f.rewritePath("/v3/api-docs/auth-service", "/v3/api-docs"))
                        .uri("http://auth-service:8884"))
                .build();
    }
}