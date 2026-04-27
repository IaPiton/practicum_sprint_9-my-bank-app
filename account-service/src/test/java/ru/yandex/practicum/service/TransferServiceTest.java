package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.configuration.TestOAuth2Config;
import ru.yandex.practicum.configuration.TestcontainersTest;
import ru.yandex.practicum.handler.CashException;
import ru.yandex.practicum.model.CashAccountDto;
import ru.yandex.practicum.model.TransferDto;
import ru.yandex.practicum.model.UserDto;
import ru.yandex.practicum.persistent.entity.BankUser;
import ru.yandex.practicum.persistent.repository.BankAccountRepository;
import ru.yandex.practicum.persistent.repository.BankUserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@Import({TestcontainersTest.class, TestOAuth2Config.class})
@DisplayName("Интеграционные тесты TransferService")
class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private BankUserRepository bankUserRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    private static final String SENDER_KEYCLOAK_ID = "sender-keycloak-id-123";
    private static final String SENDER_USERNAME = "sender";
    private static final String SENDER_EMAIL = "sender@example.com";
    private static final BigDecimal SENDER_INITIAL_BALANCE = new BigDecimal("1000");

    private static final String RECIPIENT_KEYCLOAK_ID = "recipient-keycloak-id-456";
    private static final String RECIPIENT_USERNAME = "recipient";
    private static final String RECIPIENT_EMAIL = "recipient@example.com";
    private static final BigDecimal RECIPIENT_INITIAL_BALANCE = new BigDecimal("500");

    @BeforeEach
    void setUp() {
        bankUserRepository.deleteAll();
        bankAccountRepository.deleteAll();
    }

    @Test
    @DisplayName("transfer - успешный перевод средств между пользователями")
    void transfer_ShouldTransferFunds_WhenSuccessful() {
        createSenderAndRecipient();

        TransferDto transferDto = new TransferDto();
        transferDto.setKeycloakId(SENDER_KEYCLOAK_ID);
        transferDto.setLogin(RECIPIENT_USERNAME);
        transferDto.setValue("200");

        UserDto result = transferService.transfer(transferDto);

        assertThat(result.getBalance()).isEqualTo(new BigDecimal("800"));

        BankUser recipient = bankUserRepository.findByKeycloakId(RECIPIENT_KEYCLOAK_ID).orElseThrow();
        assertThat(recipient.getBankAccount().getBalance()).isEqualTo(new BigDecimal("700"));
    }

    @Test
    @DisplayName("transfer - при недостатке средств у отправителя должно выбросить CashException")
    void transfer_WhenInsufficientFunds_ShouldThrowCashException() {
        createSenderAndRecipient();

        TransferDto transferDto = new TransferDto();
        transferDto.setKeycloakId(SENDER_KEYCLOAK_ID);
        transferDto.setLogin(RECIPIENT_USERNAME);
        transferDto.setValue("1500"); // Больше чем есть

        assertThatThrownBy(() -> transferService.transfer(transferDto))
                .isInstanceOf(CashException.class)
                .hasMessageContaining("Недостаточно средств");

        BankUser sender = bankUserRepository.findByKeycloakId(SENDER_KEYCLOAK_ID).orElseThrow();
        BankUser recipient = bankUserRepository.findByKeycloakId(RECIPIENT_KEYCLOAK_ID).orElseThrow();

        assertThat(sender.getBankAccount().getBalance()).isEqualTo(SENDER_INITIAL_BALANCE);
        assertThat(recipient.getBankAccount().getBalance()).isEqualTo(RECIPIENT_INITIAL_BALANCE);
    }

    @Test
    @DisplayName("transfer - перевод всей суммы (обнуление счета отправителя)")
    void transfer_AllFunds_ShouldSetSenderBalanceToZero() {
        createSenderAndRecipient();

        TransferDto transferDto = new TransferDto();
        transferDto.setKeycloakId(SENDER_KEYCLOAK_ID);
        transferDto.setLogin(RECIPIENT_USERNAME);
        transferDto.setValue("1000");

        UserDto result = transferService.transfer(transferDto);

        assertThat(result.getBalance()).isEqualTo(BigDecimal.ZERO);

        BankUser recipient = bankUserRepository.findByKeycloakId(RECIPIENT_KEYCLOAK_ID).orElseThrow();
        assertThat(recipient.getBankAccount().getBalance()).isEqualTo(new BigDecimal("1500"));
    }

    @Test
    @DisplayName("transfer - перевод с последующим пополнением счета отправителя")
    void transfer_ThenDeposit_ShouldWorkCorrectly() {
        createSenderAndRecipient();

        TransferDto transferDto = new TransferDto();
        transferDto.setKeycloakId(SENDER_KEYCLOAK_ID);
        transferDto.setLogin(RECIPIENT_USERNAME);
        transferDto.setValue("300");

        UserDto afterTransfer = transferService.transfer(transferDto);
        assertThat(afterTransfer.getBalance()).isEqualTo(new BigDecimal("700"));

        CashAccountDto depositDto = new CashAccountDto();
        depositDto.setKeycloakId(SENDER_KEYCLOAK_ID);
        depositDto.setValue("200");
        UserDto afterDeposit = accountService.putCash(depositDto);

        assertThat(afterDeposit.getBalance()).isEqualTo(new BigDecimal("900"));

        BankUser recipient = bankUserRepository.findByKeycloakId(RECIPIENT_KEYCLOAK_ID).orElseThrow();
        assertThat(recipient.getBankAccount().getBalance()).isEqualTo(new BigDecimal("800"));
    }

    @Test
    @DisplayName("transfer - множественные переводы между пользователями")
    void multipleTransfers_ShouldMaintainConsistentBalances() {
        createSenderAndRecipient();

        TransferDto firstTransfer = new TransferDto();
        firstTransfer.setKeycloakId(SENDER_KEYCLOAK_ID);
        firstTransfer.setLogin(RECIPIENT_USERNAME);
        firstTransfer.setValue("200");
        transferService.transfer(firstTransfer);

        TransferDto secondTransfer = new TransferDto();
        secondTransfer.setKeycloakId(SENDER_KEYCLOAK_ID);
        secondTransfer.setLogin(RECIPIENT_USERNAME);
        secondTransfer.setValue("150");
        transferService.transfer(secondTransfer);

        TransferDto thirdTransfer = new TransferDto();
        thirdTransfer.setKeycloakId(SENDER_KEYCLOAK_ID);
        thirdTransfer.setLogin(RECIPIENT_USERNAME);
        thirdTransfer.setValue("100");
        UserDto result = transferService.transfer(thirdTransfer);

        assertThat(result.getBalance()).isEqualTo(new BigDecimal("550"));

        BankUser recipient = bankUserRepository.findByKeycloakId(RECIPIENT_KEYCLOAK_ID).orElseThrow();
        assertThat(recipient.getBankAccount().getBalance()).isEqualTo(new BigDecimal("950"));
    }

    private void createSenderAndRecipient() {
        createUser(SENDER_KEYCLOAK_ID, SENDER_USERNAME, SENDER_EMAIL, SENDER_INITIAL_BALANCE);
        createUser(RECIPIENT_KEYCLOAK_ID, RECIPIENT_USERNAME, RECIPIENT_EMAIL, RECIPIENT_INITIAL_BALANCE);
    }

    private void createUser(String keycloakId, String username, String email, BigDecimal balance) {
        UserDto userDto = new UserDto();
        userDto.setKeycloakId(keycloakId);
        userDto.setLogin(username);
        userDto.setPassword("password123");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setBirthday(LocalDate.of(1990, 1, 1));
        userDto.setEmail(email);
        userDto.setBalance(balance);
        accountService.createUserAndAccount(userDto);
    }
}