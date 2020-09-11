package com.example.gateway.routes;

import com.example.gateway.filter.CryptFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfiguration {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {

        return builder.routes()
                .route("gate1", r -> r.path("/test")
                        .filters(
                                f -> f.filters(cryptFilter())
                        )
                        .uri("http://localhost:8082")
                )
                .build();
    }

    @Bean
    public CryptFilter cryptFilter() {
        return new CryptFilter();
    }

}
