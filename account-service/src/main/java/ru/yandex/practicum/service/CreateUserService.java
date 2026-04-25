package ru.yandex.practicum.service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.configuration.propertise.KeycloakProperties;
import ru.yandex.practicum.model.UserDto;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateUserService {
    private final KeycloakProperties keycloakProperties;

    public void addUser(UserDto userDto) {
        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getServerUrl())
                .realm(keycloakProperties.getRealm())
                .clientId(keycloakProperties.getClientId())
                .clientSecret(keycloakProperties.getSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build()) {

            String realm = keycloakProperties.getRealm();
            RealmResource realmResource = keycloak.realm(realm);

            UserRepresentation user = getUserRepresentation(userDto);
            UsersResource usersResource = realmResource.users();
            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                String error = response.readEntity(String.class);
                throw new RuntimeException("Ошибка создания пользователя: " + error);
            }

            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            response.close();

            createRoleIfNotExists(realmResource);

            assignRoleToUser(realmResource, userId);

            userDto.setKeycloakId(userId);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка создания пользователя в Keycloak", e);
        }
    }

    private UserRepresentation getUserRepresentation(UserDto userDto) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(userDto.getLogin());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(userDto.getPassword());
        user.setCredentials(Collections.singletonList(passwordCred));
        return user;
    }

    private void createRoleIfNotExists(RealmResource realmResource) {
        try {
            realmResource.roles().get("USER").toRepresentation();
        } catch (Exception e) {
            RoleRepresentation newRole = new RoleRepresentation();
            newRole.setName("USER");
            newRole.setDescription("Standard user role");
            realmResource.roles().create(newRole);
        }
    }

    private void assignRoleToUser(RealmResource realmResource, String userId) {
        try {
            RoleRepresentation role = realmResource.roles().get("USER").toRepresentation();

            realmResource.users().get(userId).roles().realmLevel().add(Collections.singletonList(role));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании роли", e);
        }
    }
}