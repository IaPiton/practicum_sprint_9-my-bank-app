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
import ru.yandex.practicum.handler.CashException;
import ru.yandex.practicum.model.CashAccountDto;
import ru.yandex.practicum.model.UserDto;
import ru.yandex.practicum.service.AccountService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CashController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Тесты контроллера работы с наличными (CashController)")
class CashControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    private static final String CASH_GET_URL = "/cash/action/get";
    private static final String CASH_PUT_URL = "/cash/action/put";

    @Test
    @DisplayName("POST /cash/action/get - успешное снятие денег со счета")
    @WithMockUser(username = "user")
    void cashGet_ShouldReturnUserDto_WhenSuccessful() throws Exception {
        CashAccountDto requestDto = new CashAccountDto();
        requestDto.setKeycloakId("test-keycloak-id-123");
        requestDto.setValue("500");

        UserDto expectedResponse = new UserDto();
        expectedResponse.setKeycloakId("test-keycloak-id-123");
        expectedResponse.setLogin("testuser");
        expectedResponse.setBalance(new BigDecimal("1500"));
        expectedResponse.setFirstName("Test");
        expectedResponse.setLastName("User");
        expectedResponse.setEmail("test@example.com");

        when(accountService.getCash(any(CashAccountDto.class))).thenReturn(expectedResponse);

        mockMvc.perform(post(CASH_GET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakId").value("test-keycloak-id-123"))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.balance").value(1500))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(accountService).getCash(any(CashAccountDto.class));
    }

    @Test
    @DisplayName("POST /cash/action/get - при ошибке недостаточно средств должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void cashGet_WhenCashException_ShouldReturnBadRequest() throws Exception {
        CashAccountDto requestDto = new CashAccountDto();
        requestDto.setKeycloakId("test-keycloak-id-123");
        requestDto.setValue("10000");

        when(accountService.getCash(any(CashAccountDto.class)))
                .thenThrow(new CashException("Недостаточно средств на счете"));

        mockMvc.perform(post(CASH_GET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Недостаточно средств на счете"));

        verify(accountService).getCash(any(CashAccountDto.class));
    }

    @Test
    @DisplayName("POST /cash/action/get - при неожиданной ошибке сервиса должен вернуть BAD_REQUEST с общим сообщением")
    @WithMockUser(username = "user")
    void cashGet_WhenUnexpectedException_ShouldReturnBadRequestWithGenericMessage() throws Exception {
        CashAccountDto requestDto = new CashAccountDto();
        requestDto.setKeycloakId("test-keycloak-id-123");
        requestDto.setValue("500");

        when(accountService.getCash(any(CashAccountDto.class)))
                .thenThrow(new RuntimeException("Ошибка базы данных"));

        mockMvc.perform(post(CASH_GET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ошибка при снятии денег со счета"));

        verify(accountService).getCash(any(CashAccountDto.class));
    }

    @Test
    @DisplayName("POST /cash/action/get - при некорректном значении суммы должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void cashGet_WithInvalidAmount_ShouldReturnBadRequest() throws Exception {
        CashAccountDto requestDto = new CashAccountDto();
        requestDto.setKeycloakId("test-keycloak-id-123");
        requestDto.setValue("invalid-amount");

        when(accountService.getCash(any(CashAccountDto.class)))
                .thenThrow(new CashException("Некорректная сумма операции"));

        mockMvc.perform(post(CASH_GET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(accountService).getCash(any(CashAccountDto.class));
    }

    @Test
    @DisplayName("POST /cash/action/put - успешное пополнение счета")
    @WithMockUser(username = "user")
    void cashPut_ShouldReturnUserDto_WhenSuccessful() throws Exception {
        CashAccountDto requestDto = new CashAccountDto();
        requestDto.setKeycloakId("test-keycloak-id-123");
        requestDto.setValue("1000");

        UserDto expectedResponse = new UserDto();
        expectedResponse.setKeycloakId("test-keycloak-id-123");
        expectedResponse.setLogin("testuser");
        expectedResponse.setBalance(new BigDecimal("2500"));

        when(accountService.putCash(any(CashAccountDto.class))).thenReturn(expectedResponse);

        mockMvc.perform(post(CASH_PUT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakId").value("test-keycloak-id-123"))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.balance").value(2500));

        verify(accountService).putCash(any(CashAccountDto.class));
    }

    @Test
    @DisplayName("POST /cash/action/put - при ошибке валидации счета должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void cashPut_WhenCashException_ShouldReturnBadRequest() throws Exception {
        CashAccountDto requestDto = new CashAccountDto();
        requestDto.setKeycloakId("non-existent-keycloak-id");
        requestDto.setValue("1000");

        when(accountService.putCash(any(CashAccountDto.class)))
                .thenThrow(new CashException("Пользователь с таким keycloakId не найден"));

        mockMvc.perform(post(CASH_PUT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Пользователь с таким keycloakId не найден"));

        verify(accountService).putCash(any(CashAccountDto.class));
    }

    @Test
    @DisplayName("POST /cash/action/put - при неожиданной ошибке сервиса должен вернуть BAD_REQUEST с общим сообщением")
    @WithMockUser(username = "user")
    void cashPut_WhenUnexpectedException_ShouldReturnBadRequestWithGenericMessage() throws Exception {
        CashAccountDto requestDto = new CashAccountDto();
        requestDto.setKeycloakId("test-keycloak-id-123");
        requestDto.setValue("500");

        when(accountService.putCash(any(CashAccountDto.class)))
                .thenThrow(new RuntimeException("Сервис временно недоступен"));

        mockMvc.perform(post(CASH_PUT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ошибка при пополнении счета"));

        verify(accountService).putCash(any(CashAccountDto.class));
    }

    @Test
    @DisplayName("POST /cash/action/get - при отрицательной сумме должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void cashGet_WithNegativeAmount_ShouldReturnBadRequest() throws Exception {
        CashAccountDto requestDto = new CashAccountDto();
        requestDto.setKeycloakId("test-keycloak-id-123");
        requestDto.setValue("-100");

        when(accountService.getCash(any(CashAccountDto.class)))
                .thenThrow(new CashException("Сумма должна быть положительной"));

        mockMvc.perform(post(CASH_GET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Сумма должна быть положительной"));

        verify(accountService).getCash(any(CashAccountDto.class));
    }

    @Test
    @DisplayName("POST /cash/action/put - при нулевой сумме должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void cashPut_WithZeroAmount_ShouldReturnBadRequest() throws Exception {
        CashAccountDto requestDto = new CashAccountDto();
        requestDto.setKeycloakId("test-keycloak-id-123");
        requestDto.setValue("0");

        when(accountService.putCash(any(CashAccountDto.class)))
                .thenThrow(new CashException("Сумма должна быть больше нуля"));

        mockMvc.perform(post(CASH_PUT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Сумма должна быть больше нуля"));

        verify(accountService).putCash(any(CashAccountDto.class));
    }

    @Test
    @DisplayName("POST /cash/action/get - без авторизации должен вернуть UNAUTHORIZED")
    void cashGet_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        CashAccountDto requestDto = new CashAccountDto();
        requestDto.setKeycloakId("test-keycloak-id-123");
        requestDto.setValue("500");

        mockMvc.perform(post(CASH_GET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(accountService, never()).getCash(any());
    }

    @Test
    @DisplayName("POST /cash/action/put - без авторизации должен вернуть UNAUTHORIZED")
    void cashPut_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        CashAccountDto requestDto = new CashAccountDto();
        requestDto.setKeycloakId("test-keycloak-id-123");
        requestDto.setValue("500");

        mockMvc.perform(post(CASH_PUT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(accountService, never()).putCash(any());
    }

    @Test
    @DisplayName("POST /cash/action/get - при пустом keycloakId должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void cashGet_WithEmptyKeycloakId_ShouldReturnBadRequest() throws Exception {
        CashAccountDto requestDto = new CashAccountDto();
        requestDto.setKeycloakId("");
        requestDto.setValue("500");

        when(accountService.getCash(any(CashAccountDto.class)))
                .thenThrow(new CashException("keycloakId не может быть пустым"));

        mockMvc.perform(post(CASH_GET_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(accountService).getCash(any(CashAccountDto.class));
    }

    @Test
    @DisplayName("POST /cash/action/put - при большом значении суммы должен успешно выполниться")
    @WithMockUser(username = "user")
    void cashPut_WithLargeAmount_ShouldReturnSuccess() throws Exception {
        CashAccountDto requestDto = new CashAccountDto();
        requestDto.setKeycloakId("test-keycloak-id-123");
        requestDto.setValue("999999999999");

        UserDto expectedResponse = new UserDto();
        expectedResponse.setKeycloakId("test-keycloak-id-123");
        expectedResponse.setLogin("testuser");
        expectedResponse.setBalance(new BigDecimal("1000000000000"));

        when(accountService.putCash(any(CashAccountDto.class))).thenReturn(expectedResponse);

        mockMvc.perform(post(CASH_PUT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000000000000L));

        verify(accountService).putCash(any(CashAccountDto.class));
    }
}