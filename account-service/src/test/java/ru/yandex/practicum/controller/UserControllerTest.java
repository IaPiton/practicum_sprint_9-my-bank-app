package ru.yandex.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.configuration.TestSecurityConfig;
import ru.yandex.practicum.model.UserDto;
import ru.yandex.practicum.service.AccountService;
import ru.yandex.practicum.service.CreateUserService;
import ru.yandex.practicum.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Тесты контроллера пользователей (UserController)")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateUserService createUserService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private UserService userService;

    private static final String REGISTER_URL = "/user/register";
    private static final String GET_USER_URL = "/user/{keycloakId}";
    private static final String UPDATE_USER_URL = "/user";

    @Test
    @DisplayName("POST /user/register - успешная регистрация нового пользователя")
    @WithMockUser(username = "user")
    void registerNewUser_ShouldReturnCreated_WhenSuccessful() throws Exception {
        UserDto requestDto = createTestUserDto();

        doNothing().when(createUserService).addUser(any(UserDto.class));
        doNothing().when(accountService).createUserAndAccount(any(UserDto.class));

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        verify(createUserService).addUser(any(UserDto.class));
        verify(accountService).createUserAndAccount(any(UserDto.class));
    }

    @Test
    @DisplayName("POST /user/register - при ошибке создания пользователя должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void registerNewUser_WhenException_ShouldReturnBadRequest() throws Exception {
        UserDto requestDto = createTestUserDto();

        doThrow(new RuntimeException("Пользователь с таким email уже существует"))
                .when(createUserService).addUser(any(UserDto.class));

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(createUserService).addUser(any(UserDto.class));
        verify(accountService, never()).createUserAndAccount(any(UserDto.class));
    }

    @Test
    @DisplayName("POST /user/register - при ошибке создания аккаунта должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void registerNewUser_WhenAccountCreationFails_ShouldReturnBadRequest() throws Exception {
        UserDto requestDto = createTestUserDto();

        doNothing().when(createUserService).addUser(any(UserDto.class));
        doThrow(new RuntimeException("Ошибка при создании аккаунта"))
                .when(accountService).createUserAndAccount(any(UserDto.class));

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(createUserService).addUser(any(UserDto.class));
        verify(accountService).createUserAndAccount(any(UserDto.class));
    }

    @Test
    @DisplayName("POST /user/register - при невалидном email должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void registerNewUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserDto requestDto = createTestUserDto();
        requestDto.setEmail("invalid-email");

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(createUserService, never()).addUser(any());
        verify(accountService, never()).createUserAndAccount(any());
    }

    @Test
    @DisplayName("GET /user/{keycloakId} - успешное получение пользователя по keycloakId")
    @WithMockUser(username = "user")
    void getUserByKeycloakId_ShouldReturnUserDto_WhenSuccessful() throws Exception {
        String keycloakId = "test-keycloak-id-123";
        UserDto expectedResponse = createTestUserDto();
        expectedResponse.setKeycloakId(keycloakId);

        when(userService.getUser(keycloakId)).thenReturn(expectedResponse);

        mockMvc.perform(get(GET_USER_URL, keycloakId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakId").value(keycloakId))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.balance").value(1000));

        verify(userService).getUser(keycloakId);
    }


    @Test
    @DisplayName("GET /user/{keycloakId} - без авторизации должен вернуть UNAUTHORIZED")
    void getUserByKeycloakId_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        String keycloakId = "test-keycloak-id-123";

        mockMvc.perform(get(GET_USER_URL, keycloakId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(userService, never()).getUser(any());
    }

    @Test
    @DisplayName("POST /user - успешное обновление пользователя")
    @WithMockUser(username = "user")
    void updateUser_ShouldReturnSuccessMessage_WhenSuccessful() throws Exception {
        UserDto requestDto = createTestUserDto();
        String successMessage = "Пользователь успешно обновлен";

        when(userService.updateUser(any(UserDto.class))).thenReturn(successMessage);

        mockMvc.perform(post(UPDATE_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage));

        verify(userService).updateUser(any(UserDto.class));
    }

    @Test
    @DisplayName("POST /user - при ошибке обновления должен вернуть BAD_REQUEST с сообщением")
    @WithMockUser(username = "user")
    void updateUser_WhenException_ShouldReturnBadRequestWithMessage() throws Exception {
        UserDto requestDto = createTestUserDto();

        when(userService.updateUser(any(UserDto.class)))
                .thenThrow(new RuntimeException("Ошибка обновления"));

        mockMvc.perform(post(UPDATE_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Неудалось обновить пользователя"));

        verify(userService).updateUser(any(UserDto.class));
    }

    @Test
    @DisplayName("POST /user - при обновлении несуществующего пользователя должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void updateUser_WhenUserNotFound_ShouldReturnBadRequest() throws Exception {
        UserDto requestDto = createTestUserDto();
        requestDto.setKeycloakId("non-existent-id");

        when(userService.updateUser(any(UserDto.class)))
                .thenThrow(new RuntimeException("Пользователь не найден"));

        mockMvc.perform(post(UPDATE_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Неудалось обновить пользователя"));

        verify(userService).updateUser(any(UserDto.class));
    }

    @Test
    @DisplayName("POST /user - без авторизации должен вернуть UNAUTHORIZED")
    void updateUser_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        UserDto requestDto = createTestUserDto();

        mockMvc.perform(post(UPDATE_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(userService, never()).updateUser(any());
    }

    @Test
    @DisplayName("POST /user/register - при существующем пользователе должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void registerNewUser_WhenUserAlreadyExists_ShouldReturnBadRequest() throws Exception {
        UserDto requestDto = createTestUserDto();

        doThrow(new RuntimeException("Пользователь с таким login уже существует"))
                .when(createUserService).addUser(any(UserDto.class));

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(createUserService).addUser(any(UserDto.class));
        verify(accountService, never()).createUserAndAccount(any());
    }

    @Test
    @DisplayName("POST /user/register - полная валидация всех полей пользователя")
    @WithMockUser(username = "user")
    void registerNewUser_WithAllFields_ShouldReturnCreated() throws Exception {
        UserDto requestDto = new UserDto();
        requestDto.setKeycloakId("keycloak-123");
        requestDto.setLogin("fulluser");
        requestDto.setPassword("SecurePass123!");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setBirthday(LocalDate.of(1990,1,1));
        requestDto.setEmail("john.doe@example.com");
        requestDto.setBalance(new BigDecimal("0"));

        doNothing().when(createUserService).addUser(any(UserDto.class));
        doNothing().when(accountService).createUserAndAccount(any(UserDto.class));

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        verify(createUserService).addUser(any(UserDto.class));
        verify(accountService).createUserAndAccount(any(UserDto.class));
    }

    @Test
    @DisplayName("POST /user - обновление email пользователя")
    @WithMockUser(username = "user")
    void updateUser_EmailUpdate_ShouldReturnSuccess() throws Exception {
        UserDto requestDto = createTestUserDto();
        requestDto.setEmail("newemail@example.com");
        String successMessage = "Email успешно обновлен";

        when(userService.updateUser(any(UserDto.class))).thenReturn(successMessage);

        mockMvc.perform(post(UPDATE_USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage));

        verify(userService).updateUser(any(UserDto.class));
    }

    private UserDto createTestUserDto() {
        UserDto userDto = new UserDto();
        userDto.setKeycloakId("test-keycloak-id-123");
        userDto.setLogin("testuser");
        userDto.setPassword("password123");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setBirthday(LocalDate.of(1990,1,1));
        userDto.setEmail("test@example.com");
        userDto.setBalance(new BigDecimal("1000"));
        return userDto;
    }
}