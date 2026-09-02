package com.algaworks.algashop.gateway.ecommerce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    private static final Logger log = LoggerFactory.getLogger(RateLimitConfig.class);

    private static final String DEFAULT_KEY = "anonymous";

    @Bean
    public KeyResolver rateLimitKeyResolver() {
        return exchange -> exchange.getPrincipal().map(principal -> {
            if (principal instanceof JwtAuthenticationToken jwtToken) {
                String sub = jwtToken.getToken().getClaimAsString("sub");
                return sub != null ? sub : DEFAULT_KEY;
            }
            return DEFAULT_KEY;
        })
        .switchIfEmpty(Mono.just(DEFAULT_KEY))
        .doOnNext(key -> log.info("Rate limit key: {}", key));
    }
}
