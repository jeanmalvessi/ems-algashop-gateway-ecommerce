package com.algaworks.algashop.gateway.ecommerce;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ecommerce/home")
public class EcommerceApiController {

    private final WebClient webClient;

    public EcommerceApiController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_products:read') and hasAuthority('SCOPE_categories:read')")
    public Mono<Map<String, Object>> getHome() {
        Mono<Map> productList = webClient.get()
                .uri("lb://product-catalog/api/v1/products?hasDiscount=true")
                .retrieve()
                .bodyToMono(Map.class);

        Mono<Map> categoryList = webClient.get()
                .uri("lb://product-catalog/api/v1/categories")
                .retrieve()
                .bodyToMono(Map.class);

        return Mono.zip(productList, categoryList)
                .map(tuple -> Map.of(
                        "highlights", tuple.getT1().get("content"),
                        "categories", tuple.getT2().get("content")
                ));
    }
}
