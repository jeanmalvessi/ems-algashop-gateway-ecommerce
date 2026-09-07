# gateway-ecommerce

Microservice responsible for routing, protecting, and aggregating storefront traffic for the [AlgaShop](https://github.com/jeanmalvessi/ems-algashop-meta) platform.

Built with **Spring Cloud Gateway** (WebFlux) reactive edge server, sitting in front of `ordering`, `billing`, `product-catalog`, and `authorization-server` for the `ecommerce` app, and also acting as a lightweight **BFF** for the storefront home page.

## Responsibilities

- Routing storefront requests to `ordering`, `billing`, `product-catalog`, and `authorization-server` via Eureka-discovered (`lb://`) instances
- Aggregating product highlights and categories into a single home-page response (BFF endpoint)
- OAuth2 resource-server JWT validation on every `/api/**` route
- Rate limiting (Redis token bucket) and local response caching on product/category routes
- Circuit breaking, retries, and response-header deduplication on proxied calls

## Architecture

- **Gateway Layer:** Declarative predicate/filter route definitions (path/method matching, `RequestRateLimiter`, `LocalResponseCache`, `CircuitBreaker`, `Retry`)
- **BFF Layer:** `EcommerceApiController` composes `product-catalog` product and category calls (via `WebClient`) into a single `/api/v1/ecommerce/home` response
- **Security:** `GatewayEcommerceSecurityConfig` (JWT-authenticated `/api/**`), `RateLimitConfig` (per-authenticated-subject rate-limit key resolver)
- **Service Discovery:** `EurekaClientConfig` registers with and resolves peers through `service-registry`

## Tech Stack

- **Java 25**, Spring Boot 4.0.8
- **Spring Cloud Gateway Server WebFlux** (reactive routing, filters, predicates)
- **Spring WebClient** (reactive BFF aggregation calls)
- **Spring Cloud Netflix Eureka Client** (service discovery, load-balanced routing)
- **Spring Security OAuth2 Resource Server** (JWT validation)
- **Spring Cloud Circuit Breaker** (Resilience4j, reactive)
- **Spring Data Redis Reactive** + Caffeine (request rate limiting, local response caching)
- **Spring Cloud AWS Secrets Manager / Parameter Store** (externalized configuration, mocked via LocalStack)
- **Spring Boot Actuator** (monitoring, gateway routes and circuit-breaker health endpoints)

## API / Routes

Base path: `/api/v1`

| Route | Path | Target | Notes |
|-------|------|--------|-------|
| GET | `/ecommerce/home` | *(local, BFF)* | Aggregates highlighted products + categories from `product-catalog` |
| billing-route | `/customers/me/credit-cards`, `/customers/me/credit-cards/{creditCardId}` | `billing` | |
| ordering-route | `/customers/**`, `/shipping-cost-previews` | `ordering` | Circuit breaker |
| product-catalog-route | `/products/**`, `/categories/**` | `product-catalog` | Rate limited, local response cache |
| user-management-route | `/users/**` | `authorization-server` | |

## Running

```bash
./gradlew bootRun
```

Default port: **9999** (development profile)

Requires `service-registry` running and reachable via Eureka, and Redis for rate limiting.
