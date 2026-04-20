package ru.yandex.practicum.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Gateway-фильтр, реализующий Token Relay:
 * забирает JWT из SecurityContext (если есть) или из входящего заголовка Authorization
 * и подставляет его в исходящий запрос к микросервису.
 * <p>
 * Поддерживает список разрешённых путей, для которых токен не требуется.
 * <p>
 * В application.yml используется как:
 * <p>
 * filters:
 * - name: JwtTokenRelay
 *   args:
 *     permittedPaths: /actuator/health, /actuator/info, /public/**
 */
public class JwtTokenRelayGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtTokenRelayGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenRelayGatewayFilterFactory.class);

    public JwtTokenRelayGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // Если путь разрешённый — пропускаем без токена
            if (isPermittedPath(path, config.getPermittedPaths())) {
                log.debug("Permitted path '{}' - skipping token relay", path);
                return chain.filter(exchange);
            }

            // Для защищённых путей требуем токен
            return extractToken(exchange)
                    .switchIfEmpty(Mono.error(new IllegalStateException(
                            "JWT token not found for protected path: " + path)))
                    .flatMap(token -> chain.filter(addToken(exchange, token)));
        };
    }

    /**
     * Проверяет, относится ли путь к разрешённым (с поддержкой wildcard **).
     */
    private boolean isPermittedPath(String requestPath, List<String> permittedPaths) {
        if (permittedPaths == null || permittedPaths.isEmpty()) {
            return false;
        }

        return permittedPaths.stream().anyMatch(pattern -> matchPath(pattern, requestPath));
    }

    /**
     * Простое сопоставление пути с поддержкой ** в конце (например /public/**).
     */
    private boolean matchPath(String pattern, String path) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return path.startsWith(prefix);
        }
        return pattern.equals(path);
    }

    /**
     * Пытаемся достать токен:
     * 1) сначала из SecurityContext (если Gateway выступает как Resource Server),
     * 2) если там пусто — из входящего заголовка Authorization.
     */
    private Mono<String> extractToken(ServerWebExchange exchange) {
        Mono<String> fromContext = ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .map(auth -> ((JwtAuthenticationToken) auth).getToken().getTokenValue());

        Mono<String> fromHeader = Mono
                .justOrEmpty(exchange.getRequest().getHeaders().getFirst("Authorization"))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7));

        return fromContext.switchIfEmpty(fromHeader);
    }

    /**
     * Добавляем в исходящий запрос заголовок Authorization: Bearer <token>.
     */
    private ServerWebExchange addToken(ServerWebExchange exchange, String token) {
        var mutated = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("Authorization", "Bearer " + token)
                        .build())
                .build();

        log.debug("Token relayed for path {} (len={})",
                exchange.getRequest().getPath(), token.length());

        return mutated;
    }

    /**
     * Конфигурация фильтра.
     */
    public static class Config {
        private List<String> permittedPaths;

        public List<String> getPermittedPaths() {
            return permittedPaths;
        }

        public void setPermittedPaths(List<String> permittedPaths) {
            this.permittedPaths = permittedPaths;
        }
    }
}