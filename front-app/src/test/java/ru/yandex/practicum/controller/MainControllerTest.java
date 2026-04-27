package ru.yandex.practicum.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import ru.yandex.practicum.configuration.TestSecurityConfig;
import ru.yandex.practicum.dto.Account;
import ru.yandex.practicum.dto.AccountsTransfer;
import ru.yandex.practicum.dto.CashAction;
import ru.yandex.practicum.handler.AccountException;
import ru.yandex.practicum.service.AccountService;
import ru.yandex.practicum.service.CashService;
import ru.yandex.practicum.service.TransferService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Тесты MainController")
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private CashService cashService;

    @MockitoBean
    private TransferService transferService;

    private static final String TEST_SUBJECT = "test-user-123";
    private static final String TEST_LOGIN = "john_doe";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_EMAIL = "john.doe@example.com";
    private static final LocalDate TEST_BIRTHDATE = LocalDate.of(1990, 1, 1);
    private static final BigDecimal TEST_BALANCE = new BigDecimal("1000");


    @Test
    @DisplayName("GET /main - должен вернуть главную страницу с данными пользователя")
    void getMainPage_ShouldReturnMainPage_WhenAuthenticated() throws Exception {
        Account testAccount = createTestAccount();

        doAnswer(invocation -> {
            Model model = invocation.getArgument(1);
            model.addAttribute("account", testAccount);
            return null;
        }).when(accountService).getAccount(eq(TEST_SUBJECT), any());

        mockMvc.perform(get("/main")
                        .with(oidcLogin().oidcUser(createTestOidcUser())))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("account"));

        verify(accountService).getAccount(eq(TEST_SUBJECT), any());
    }

    @Test
    @DisplayName("GET /main - без авторизации должен вернуть UNAUTHORIZED")
    void getMainPage_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/main"))
                .andExpect(status().isForbidden());

        verify(accountService, never()).getAccount(any(), any());
    }

    @Test
    @DisplayName("POST /account - успешное обновление аккаунта")
    void editAccount_ShouldUpdateAccount_WhenValidData() throws Exception {
        String updateInfo = "Аккаунт успешно обновлен";
        String fullName = TEST_FIRST_NAME + " " + TEST_LAST_NAME;

        when(accountService.updateAccount(eq(TEST_SUBJECT), eq(fullName), eq(TEST_BIRTHDATE)))
                .thenReturn(updateInfo);

        Account testAccount = createTestAccount();
        doAnswer(invocation -> {
            Model model = invocation.getArgument(1);
            model.addAttribute("account", testAccount);
            return null;
        }).when(accountService).getAccount(eq(TEST_SUBJECT), any());

        mockMvc.perform(post("/account")
                        .with(oidcLogin().oidcUser(createTestOidcUser()))
                        .param("name", fullName)
                        .param("birthdate", "1990-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("info", updateInfo))
                .andExpect(model().attributeExists("account"));

        verify(accountService).updateAccount(eq(TEST_SUBJECT), eq(fullName), eq(TEST_BIRTHDATE));
        verify(accountService).getAccount(eq(TEST_SUBJECT), any());
    }

    @Test
    @DisplayName("POST /account - с датой рождения в будущем должна вернуть ошибку валидации")
    void editAccount_WithFutureBirthdate_ShouldReturnValidationError() throws Exception {
        mockMvc.perform(post("/account")
                        .with(oidcLogin().oidcUser(createTestOidcUser()))
                        .param("name", TEST_FIRST_NAME + " " + TEST_LAST_NAME)
                        .param("birthdate", LocalDate.now().plusYears(1).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors",
                        hasItem("Доступ к сервису доступен лишь по достижению 18-ти лет")));

        verify(accountService, never()).updateAccount(any(), any(), any());
    }

    @Test
    @DisplayName("POST /account - с датой рождения менее 18 лет должна вернуть ошибку валидации")
    void editAccount_WithUnderageBirthdate_ShouldReturnValidationError() throws Exception {
        LocalDate underageDate = LocalDate.now().minusYears(17);

        mockMvc.perform(post("/account")
                        .with(oidcLogin().oidcUser(createTestOidcUser()))
                        .param("name", TEST_FIRST_NAME + " " + TEST_LAST_NAME)
                        .param("birthdate", underageDate.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors",
                        hasItem("Доступ к сервису доступен лишь по достижению 18-ти лет")));

        verify(accountService, never()).updateAccount(any(), any(), any());
    }

    @Test
    @DisplayName("POST /account - с некорректной датой рождения должен вернуть ошибку")
    void editAccount_WithInvalidBirthdate_ShouldReturnError() throws Exception {
        mockMvc.perform(post("/account")
                        .with(oidcLogin().oidcUser(createTestOidcUser()))
                        .param("name", TEST_FIRST_NAME + " " + TEST_LAST_NAME)
                        .param("birthdate", "2025-13-45"))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).updateAccount(any(), any(), any());
    }

    @Test
    @DisplayName("POST /account - при ошибке сервиса AccountException должен вернуть страницу с ошибкой")
    void editAccount_WhenAccountException_ShouldReturnError() throws Exception {
        String fullName = TEST_FIRST_NAME + " " + TEST_LAST_NAME;
        String errorMessage = "Пользователь не найден";

        when(accountService.updateAccount(eq(TEST_SUBJECT), eq(fullName), eq(TEST_BIRTHDATE)))
                .thenThrow(new AccountException(errorMessage));

        mockMvc.perform(post("/account")
                        .with(oidcLogin().oidcUser(createTestOidcUser()))
                        .param("name", fullName)
                        .param("birthdate", "1990-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("errors", errorMessage));

        verify(accountService).updateAccount(eq(TEST_SUBJECT), eq(fullName), eq(TEST_BIRTHDATE));
        // getAccount НЕ должен вызываться, так как updateAccount выбросил исключение
        verify(accountService, never()).getAccount(any(), any());
    }

    @Test
    @DisplayName("POST /account - без авторизации должен вернуть UNAUTHORIZED")
    void editAccount_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/account")
                        .param("name", TEST_FIRST_NAME + " " + TEST_LAST_NAME)
                        .param("birthdate", "1990-01-01"))
                .andExpect(status().isForbidden());

        verify(accountService, never()).updateAccount(any(), any(), any());
    }

    @Test
    @DisplayName("POST /cash - успешное пополнение счета (PUT)")
    void editCash_WithPutAction_ShouldProcessDeposit() throws Exception {
        Account testAccount = createTestAccount();

        doNothing().when(cashService).editCash(any(), eq(500), eq(CashAction.PUT), eq(TEST_SUBJECT));

        doAnswer(invocation -> {
            Model model = invocation.getArgument(1);
            model.addAttribute("account", testAccount);
            return null;
        }).when(accountService).getAccount(eq(TEST_SUBJECT), any());

        mockMvc.perform(post("/cash")
                        .with(oidcLogin().oidcUser(createTestOidcUser()))
                        .param("value", "500")
                        .param("action", CashAction.PUT.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("account"));

        verify(cashService).editCash(any(), eq(500), eq(CashAction.PUT), eq(TEST_SUBJECT));
        verify(accountService).getAccount(eq(TEST_SUBJECT), any());
    }

    @Test
    @DisplayName("POST /cash - успешное снятие средств (GET)")
    void editCash_WithGetAction_ShouldProcessWithdraw() throws Exception {
        Account testAccount = createTestAccount();

        doNothing().when(cashService).editCash(any(), eq(300), eq(CashAction.GET), eq(TEST_SUBJECT));

        doAnswer(invocation -> {
            Model model = invocation.getArgument(1);
            model.addAttribute("account", testAccount);
            return null;
        }).when(accountService).getAccount(eq(TEST_SUBJECT), any());

        mockMvc.perform(post("/cash")
                        .with(oidcLogin().oidcUser(createTestOidcUser()))
                        .param("value", "300")
                        .param("action", CashAction.GET.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("account"));

        verify(cashService).editCash(any(), eq(300), eq(CashAction.GET), eq(TEST_SUBJECT));
        verify(accountService).getAccount(eq(TEST_SUBJECT), any());
    }


    @Test
    @DisplayName("POST /cash - при ошибке AccountException должен вернуть страницу с ошибкой без вызова getAccount")
    void editCash_WhenAccountException_ShouldReturnError() throws Exception {
        String errorMessage = "Недостаточно средств для снятия";

        doThrow(new AccountException(errorMessage))
                .when(cashService).editCash(any(), eq(500), eq(CashAction.GET), eq(TEST_SUBJECT));

        mockMvc.perform(post("/cash")
                        .with(oidcLogin().oidcUser(createTestOidcUser()))
                        .param("value", "500")
                        .param("action", CashAction.GET.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("errors", errorMessage));

        verify(cashService).editCash(any(), eq(500), eq(CashAction.GET), eq(TEST_SUBJECT));
        verify(accountService, never()).getAccount(any(), any());
    }

    @Test
    @DisplayName("POST /cash - без авторизации должен вернуть UNAUTHORIZED")
    void editCash_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/cash")
                        .param("value", "500")
                        .param("action", CashAction.PUT.name()))
                .andExpect(status().isForbidden());

        verify(cashService, never()).editCash(any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("POST /transfer - успешный перевод средств")
    void transfer_ShouldProcessTransfer_WhenValidData() throws Exception {
        Account testAccount = createTestAccount();
        String recipientLogin = "recipient_user";

        doNothing().when(transferService).transferCash(any(), eq(TEST_SUBJECT), eq(200), eq(recipientLogin));

        doAnswer(invocation -> {
            Model model = invocation.getArgument(1);
            model.addAttribute("account", testAccount);
            return null;
        }).when(accountService).getAccount(eq(TEST_SUBJECT), any());

        mockMvc.perform(post("/transfer")
                        .with(oidcLogin().oidcUser(createTestOidcUser()))
                        .param("value", "200")
                        .param("login", recipientLogin))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("account"));

        verify(transferService).transferCash(any(), eq(TEST_SUBJECT), eq(200), eq(recipientLogin));
        verify(accountService).getAccount(eq(TEST_SUBJECT), any());
    }

    @Test
    @DisplayName("POST /transfer - при ошибке AccountException должен вернуть страницу с ошибкой")
    void transfer_WhenAccountException_ShouldReturnError() throws Exception {
        String recipientLogin = "recipient_user";
        String errorMessage = "Получатель с таким логином не найден";

        doThrow(new AccountException(errorMessage))
                .when(transferService).transferCash(any(), eq(TEST_SUBJECT), eq(200), eq(recipientLogin));

        mockMvc.perform(post("/transfer")
                        .with(oidcLogin().oidcUser(createTestOidcUser()))
                        .param("value", "200")
                        .param("login", recipientLogin))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("errors", errorMessage));

        verify(transferService).transferCash(any(), eq(TEST_SUBJECT), eq(200), eq(recipientLogin));
        verify(accountService, never()).getAccount(any(), any());
    }

    @Test
    @DisplayName("POST /transfer - без авторизации должен вернуть UNAUTHORIZED")
    void transfer_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/transfer")
                        .param("value", "200")
                        .param("login", "recipient-user"))
                .andExpect(status().isForbidden());

        verify(transferService, never()).transferCash(any(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("POST /cash - с максимальным значением суммы")
    void editCash_WithMaxValue_ShouldProcess() throws Exception {
        Account testAccount = createTestAccount();

        doNothing().when(cashService).editCash(any(), eq(999999999), eq(CashAction.PUT), eq(TEST_SUBJECT));

        doAnswer(invocation -> {
            Model model = invocation.getArgument(1);
            model.addAttribute("account", testAccount);
            return null;
        }).when(accountService).getAccount(eq(TEST_SUBJECT), any());

        mockMvc.perform(post("/cash")
                        .with(oidcLogin().oidcUser(createTestOidcUser()))
                        .param("value", "999999999")
                        .param("action", CashAction.PUT.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("account"));

        verify(cashService).editCash(any(), eq(999999999), eq(CashAction.PUT), eq(TEST_SUBJECT));
    }

    private Account createTestAccount() {
        return Account.builder()
                .keycloakId(TEST_SUBJECT)
                .login(TEST_LOGIN)
                .firstName(TEST_FIRST_NAME)
                .lastName(TEST_LAST_NAME)
                .birthday(TEST_BIRTHDATE)
                .email(TEST_EMAIL)
                .balance(TEST_BALANCE)
                .password("encrypted_password")
                .accounts(List.of(
                        new AccountsTransfer("user1", "User One"),
                        new AccountsTransfer("user2", "User Two")
                ))
                .build();
    }

    private DefaultOidcUser createTestOidcUser() {
        OidcIdToken token = new OidcIdToken("token",
                java.time.Instant.now(),
                java.time.Instant.now().plusSeconds(3600),
                Collections.singletonMap("sub", TEST_SUBJECT));

        return new DefaultOidcUser(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                token, "sub");
    }
}