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
import ru.yandex.practicum.account.model.SignupUserInfoDto;
import ru.yandex.practicum.configuration.propertise.KeycloakProperties;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateUserService {
    private final KeycloakProperties keycloakProperties;

    public String addUser(SignupUserInfoDto signupUserInfoDto) {
        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getServerUrl())
                .realm(keycloakProperties.getRealm())
                .clientId(keycloakProperties.getClientId())
                .clientSecret(keycloakProperties.getSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build()) {

            String realm = keycloakProperties.getRealm();
            RealmResource realmResource = keycloak.realm(realm);

            UserRepresentation user = getUserRepresentation(signupUserInfoDto);
            UsersResource usersResource = realmResource.users();
            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                String error = response.readEntity(String.class);
                throw new RuntimeException("Failed to create user: " + error);
            }

            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            log.info("User created successfully with ID: {}", userId);
            response.close();

            createRoleIfNotExists(realmResource, "USER");

            assignRoleToUser(realmResource, userId, "USER");

            log.info("Role USER assigned to user: {}", signupUserInfoDto.getLogin());

            return userId;
        } catch (Exception e) {
            log.error("Error creating user in Keycloak", e);
            throw new RuntimeException("Ошибка создания пользователя в Keycloak", e);
        }
    }

    private UserRepresentation getUserRepresentation(SignupUserInfoDto signupUserInfoDto) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(signupUserInfoDto.getLogin());
        user.setEmail(signupUserInfoDto.getPersonalInfo().getEmail());
        user.setFirstName(signupUserInfoDto.getPersonalInfo().getFirstName());
        user.setLastName(signupUserInfoDto.getPersonalInfo().getLastName());

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(signupUserInfoDto.getPasswordInfo().getPassword());
        user.setCredentials(Collections.singletonList(passwordCred));
        return user;
    }

    private void createRoleIfNotExists(RealmResource realmResource, String roleName) {
        try {
            realmResource.roles().get(roleName).toRepresentation();
        } catch (Exception e) {
            RoleRepresentation newRole = new RoleRepresentation();
            newRole.setName(roleName);
            newRole.setDescription("Standard user role");
            realmResource.roles().create(newRole);
            log.info("Role {} created successfully", roleName);
        }
    }

    private void assignRoleToUser(RealmResource realmResource, String userId, String roleName) {
        try {
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();

            realmResource.users().get(userId).roles().realmLevel().add(Collections.singletonList(role));
            log.info("Role {} assigned to user {}", roleName, userId);
        } catch (Exception e) {
            log.error("Failed to assign role {} to user {}", roleName, userId, e);
            throw new RuntimeException("Failed to assign role to user", e);
        }
    }
}