package ru.yandex.practicum.configuration.propertise;

import jakarta.annotation.PostConstruct;
import lombok.*;
import org.springframework.stereotype.Component;

import lombok.Data;
import ru.yandex.practicum.service.ConsulConfigReader;

import java.util.Map;

@Data
@Component
public class KeycloakProperties {

    private final ConsulConfigReader consulConfigReader;

    private String serverUrl;
    private String realm;
    private String clientId;
    private String secret;

    public KeycloakProperties(ConsulConfigReader consulConfigReader) {
        this.consulConfigReader = consulConfigReader;
    }

    @PostConstruct
    public void loadFromConsul() {
        System.out.println("=== Loading KeycloakProperties from Consul ===");

        Map<String, Object> keycloakConfig = consulConfigReader.readConfigSection(
                "config/account-service/application/data",
                "keycloak"
        );

        if (keycloakConfig != null) {
            if (keycloakConfig.containsKey("server-url")) {
                this.serverUrl = (String) keycloakConfig.get("server-url");
            }
            if (keycloakConfig.containsKey("realm")) {
                this.realm = (String) keycloakConfig.get("realm");
            }
            if (keycloakConfig.containsKey("client-id")) {
                this.clientId = (String) keycloakConfig.get("client-id");
            }
            if (keycloakConfig.containsKey("secret")) {
                this.secret = (String) keycloakConfig.get("secret");
            }
            System.out.println("✅ Loaded Keycloak config from Consul successfully!");
        } else {
            System.out.println("⚠️ No Keycloak config found in Consul, using default values");
        }

    }
}