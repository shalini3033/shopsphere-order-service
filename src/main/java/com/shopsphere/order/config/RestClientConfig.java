
package com.shopsphere.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
//if services.product.url is not present in docker default is http://localhost:8080
// when we do not through docker we dont have this services.product.url
    @Value("${services.product.url:http://localhost:8080}")
    private String productServiceUrl;

    @Value("${services.inventory.url:http://localhost:8081}")
    private String inventoryServiceUrl;

    @Bean
    public RestClient productRestClient() {
        return RestClient.builder()
                .baseUrl(productServiceUrl)
                .build();
    }

    @Bean
    public RestClient inventoryRestClient() {
        return RestClient.builder()
                .baseUrl(inventoryServiceUrl)
                .build();
    }
}
