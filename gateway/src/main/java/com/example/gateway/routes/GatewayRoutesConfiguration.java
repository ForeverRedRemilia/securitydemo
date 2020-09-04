package com.example.gateway.routes;

import com.example.gateway.filter.DecryptFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfiguration {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("gate1", r -> r.readBody(Object.class, requestBody -> true)
                        .and().path("/test")
                        .filters(
                                f -> f.filters(new DecryptFilter())
                        )
                        .uri("lb://cloud-discovery-server")
                )
                .build();
    }

}
