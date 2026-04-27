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
import ru.yandex.practicum.model.TransferDto;
import ru.yandex.practicum.model.UserDto;
import ru.yandex.practicum.service.TransferService;
import ru.yandex.practicum.service.UserService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Тесты контроллера переводов (TransferController)")
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TransferService transferService;

    private static final String TRANSFER_URL = "/transfer/action";

    @Test
    @DisplayName("POST /transfer/action - успешный перевод средств")
    @WithMockUser(username = "user")
    void transfer_ShouldReturnUserDto_WhenSuccessful() throws Exception {
        TransferDto requestDto = new TransferDto();
        requestDto.setKeycloakId("sender-keycloak-id-123");
        requestDto.setLogin("receiver-login");
        requestDto.setValue("500");

        UserDto transferResultDto = new UserDto();
        transferResultDto.setKeycloakId("sender-keycloak-id-123");
        transferResultDto.setLogin("sender-login");
        transferResultDto.setBalance(new BigDecimal("1500"));

        UserDto expectedResponse = new UserDto();
        expectedResponse.setKeycloakId("sender-keycloak-id-123");
        expectedResponse.setLogin("sender-login");
        expectedResponse.setBalance(new BigDecimal("1500"));
        expectedResponse.setFirstName("Test");
        expectedResponse.setLastName("Sender");
        expectedResponse.setEmail("sender@example.com");

        when(transferService.transfer(any(TransferDto.class))).thenReturn(transferResultDto);
        when(userService.getUser(transferResultDto.getKeycloakId())).thenReturn(expectedResponse);

        mockMvc.perform(post(TRANSFER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakId").value("sender-keycloak-id-123"))
                .andExpect(jsonPath("$.login").value("sender-login"))
                .andExpect(jsonPath("$.balance").value(1500))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("Sender"))
                .andExpect(jsonPath("$.email").value("sender@example.com"));

        verify(transferService).transfer(any(TransferDto.class));
        verify(userService).getUser(transferResultDto.getKeycloakId());
    }

    @Test
    @DisplayName("POST /transfer/action - при ошибке недостаточно средств должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void transfer_WhenInsufficientFunds_ShouldReturnBadRequest() throws Exception {
        TransferDto requestDto = new TransferDto();
        requestDto.setKeycloakId("sender-keycloak-id-123");
        requestDto.setLogin("receiver-login");
        requestDto.setValue("10000");

        when(transferService.transfer(any(TransferDto.class)))
                .thenThrow(new CashException("Недостаточно средств на счете для перевода"));

        mockMvc.perform(post(TRANSFER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Недостаточно средств на счете для перевода"));

        verify(transferService).transfer(any(TransferDto.class));
        verify(userService, never()).getUser(any());
    }

    @Test
    @DisplayName("POST /transfer/action - при ошибке получатель не найден должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void transfer_WhenReceiverNotFound_ShouldReturnBadRequest() throws Exception {
        TransferDto requestDto = new TransferDto();
        requestDto.setKeycloakId("sender-keycloak-id-123");
        requestDto.setLogin("non-existent-login");
        requestDto.setValue("500");

        when(transferService.transfer(any(TransferDto.class)))
                .thenThrow(new CashException("Получатель с логином non-existent-login не найден"));

        mockMvc.perform(post(TRANSFER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Получатель с логином non-existent-login не найден"));

        verify(transferService).transfer(any(TransferDto.class));
        verify(userService, never()).getUser(any());
    }

    @Test
    @DisplayName("POST /transfer/action - при переводе самому себе должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void transfer_ToSameUser_ShouldReturnBadRequest() throws Exception {
        TransferDto requestDto = new TransferDto();
        requestDto.setKeycloakId("sender-keycloak-id-123");
        requestDto.setLogin("same-user-login");
        requestDto.setValue("500");

        when(transferService.transfer(any(TransferDto.class)))
                .thenThrow(new CashException("Нельзя перевести средства самому себе"));

        mockMvc.perform(post(TRANSFER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Нельзя перевести средства самому себе"));

        verify(transferService).transfer(any(TransferDto.class));
        verify(userService, never()).getUser(any());
    }

    @Test
    @DisplayName("POST /transfer/action - при отрицательной сумме перевода должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void transfer_WithNegativeAmount_ShouldReturnBadRequest() throws Exception {
        TransferDto requestDto = new TransferDto();
        requestDto.setKeycloakId("sender-keycloak-id-123");
        requestDto.setLogin("receiver-login");
        requestDto.setValue("-100");

        when(transferService.transfer(any(TransferDto.class)))
                .thenThrow(new CashException("Сумма перевода должна быть положительной"));

        mockMvc.perform(post(TRANSFER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Сумма перевода должна быть положительной"));

        verify(transferService).transfer(any(TransferDto.class));
        verify(userService, never()).getUser(any());
    }

    @Test
    @DisplayName("POST /transfer/action - при нулевой сумме перевода должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void transfer_WithZeroAmount_ShouldReturnBadRequest() throws Exception {
        TransferDto requestDto = new TransferDto();
        requestDto.setKeycloakId("sender-keycloak-id-123");
        requestDto.setLogin("receiver-login");
        requestDto.setValue("0");

        when(transferService.transfer(any(TransferDto.class)))
                .thenThrow(new CashException("Сумма перевода должна быть больше нуля"));

        mockMvc.perform(post(TRANSFER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Сумма перевода должна быть больше нуля"));

        verify(transferService).transfer(any(TransferDto.class));
        verify(userService, never()).getUser(any());
    }

    @Test
    @DisplayName("POST /transfer/action - при неожиданной ошибке сервиса должен вернуть BAD_REQUEST с общим сообщением")
    @WithMockUser(username = "user")
    void transfer_WhenUnexpectedException_ShouldReturnBadRequestWithGenericMessage() throws Exception {
        TransferDto requestDto = new TransferDto();
        requestDto.setKeycloakId("sender-keycloak-id-123");
        requestDto.setLogin("receiver-login");
        requestDto.setValue("500");

        when(transferService.transfer(any(TransferDto.class)))
                .thenThrow(new RuntimeException("Ошибка базы данных"));

        mockMvc.perform(post(TRANSFER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ошибка при переводе денег со счета"));

        verify(transferService).transfer(any(TransferDto.class));
        verify(userService, never()).getUser(any());
    }

    @Test
    @DisplayName("POST /transfer/action - при пустом keycloakId отправителя должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void transfer_WithEmptySenderKeycloakId_ShouldReturnBadRequest() throws Exception {
        TransferDto requestDto = new TransferDto();
        requestDto.setKeycloakId("");
        requestDto.setLogin("receiver-login");
        requestDto.setValue("500");

        when(transferService.transfer(any(TransferDto.class)))
                .thenThrow(new CashException("keycloakId отправителя не может быть пустым"));

        mockMvc.perform(post(TRANSFER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(transferService).transfer(any(TransferDto.class));
        verify(userService, never()).getUser(any());
    }

    @Test
    @DisplayName("POST /transfer/action - при пустом логине получателя должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void transfer_WithEmptyReceiverLogin_ShouldReturnBadRequest() throws Exception {
        TransferDto requestDto = new TransferDto();
        requestDto.setKeycloakId("sender-keycloak-id-123");
        requestDto.setLogin("");
        requestDto.setValue("500");

        when(transferService.transfer(any(TransferDto.class)))
                .thenThrow(new CashException("Логин получателя не может быть пустым"));

        mockMvc.perform(post(TRANSFER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(transferService).transfer(any(TransferDto.class));
        verify(userService, never()).getUser(any());
    }

    @Test
    @DisplayName("POST /transfer/action - при переводе с несуществующего счета должен вернуть BAD_REQUEST")
    @WithMockUser(username = "user")
    void transfer_FromNonExistentAccount_ShouldReturnBadRequest() throws Exception {
        TransferDto requestDto = new TransferDto();
        requestDto.setKeycloakId("non-existent-keycloak-id");
        requestDto.setLogin("receiver-login");
        requestDto.setValue("500");

        when(transferService.transfer(any(TransferDto.class)))
                .thenThrow(new CashException("Отправитель с таким keycloakId не найден"));

        mockMvc.perform(post(TRANSFER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Отправитель с таким keycloakId не найден"));

        verify(transferService).transfer(any(TransferDto.class));
        verify(userService, never()).getUser(any());
    }

    @Test
    @DisplayName("POST /transfer/action - при большой сумме перевода должен вернуть успешный результат")
    @WithMockUser(username = "user")
    void transfer_WithLargeAmount_ShouldReturnSuccess() throws Exception {
        TransferDto requestDto = new TransferDto();
        requestDto.setKeycloakId("sender-keycloak-id-123");
        requestDto.setLogin("receiver-login");
        requestDto.setValue("999999999");

        UserDto transferResultDto = new UserDto();
        transferResultDto.setKeycloakId("sender-keycloak-id-123");
        transferResultDto.setLogin("sender-login");
        transferResultDto.setBalance(new BigDecimal("1000000000"));

        UserDto expectedResponse = new UserDto();
        expectedResponse.setKeycloakId("sender-keycloak-id-123");
        expectedResponse.setLogin("sender-login");
        expectedResponse.setBalance(new BigDecimal("1000000000"));

        when(transferService.transfer(any(TransferDto.class))).thenReturn(transferResultDto);
        when(userService.getUser(transferResultDto.getKeycloakId())).thenReturn(expectedResponse);

        mockMvc.perform(post(TRANSFER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000000000L));

        verify(transferService).transfer(any(TransferDto.class));
        verify(userService).getUser(any());
    }

    @Test
    @DisplayName("POST /transfer/action - без авторизации должен вернуть UNAUTHORIZED")
    void transfer_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        TransferDto requestDto = new TransferDto();
        requestDto.setKeycloakId("sender-keycloak-id-123");
        requestDto.setLogin("receiver-login");
        requestDto.setValue("500");

        mockMvc.perform(post(TRANSFER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());

        verify(transferService, never()).transfer(any());
        verify(userService, never()).getUser(any());
    }

    @Test
    @DisplayName("POST /transfer/action - после успешного перевода должен обновить баланс отправителя")
    @WithMockUser(username = "user")
    void transfer_AfterSuccess_ShouldReturnUpdatedBalance() throws Exception {
        TransferDto requestDto = new TransferDto();
        requestDto.setKeycloakId("sender-keycloak-id-123");
        requestDto.setLogin("receiver-login");
        requestDto.setValue("500");

        UserDto transferResultDto = new UserDto();
        transferResultDto.setKeycloakId("sender-keycloak-id-123");
        transferResultDto.setLogin("sender-login");
        transferResultDto.setBalance(new BigDecimal("1500"));

        UserDto expectedResponse = new UserDto();
        expectedResponse.setKeycloakId("sender-keycloak-id-123");
        expectedResponse.setLogin("sender-login");
        expectedResponse.setBalance(new BigDecimal("1500"));

        when(transferService.transfer(any(TransferDto.class))).thenReturn(transferResultDto);
        when(userService.getUser(transferResultDto.getKeycloakId())).thenReturn(expectedResponse);

        mockMvc.perform(post(TRANSFER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500L));

        verify(transferService).transfer(any(TransferDto.class));
        verify(userService).getUser("sender-keycloak-id-123");
    }
}