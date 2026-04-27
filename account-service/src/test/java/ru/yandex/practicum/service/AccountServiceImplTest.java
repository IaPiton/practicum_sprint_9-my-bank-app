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
@DisplayName("Интеграционные тесты AccountServiceImpl")
class AccountServiceImplTest {

    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private BankUserRepository bankUserRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    private static final String TEST_KEYCLOAK_ID = "test-keycloak-id-123";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000");

    @BeforeEach
    void setUp() {
        bankUserRepository.deleteAll();
        bankAccountRepository.deleteAll();
    }

    @Test
    @DisplayName("createUserAndAccount - успешное создание пользователя и аккаунта")
    void createUserAndAccount_ShouldCreateUserAndAccount_WhenSuccessful() {
        UserDto userDto = createTestUserDto();

        accountService.createUserAndAccount(userDto);

        BankUser savedUser = bankUserRepository.findByKeycloakId(TEST_KEYCLOAK_ID).orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(savedUser.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(savedUser.getFirstName()).isEqualTo("Test");
        assertThat(savedUser.getLastName()).isEqualTo("User");
        assertThat(savedUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(savedUser.getKeycloakId()).isEqualTo(TEST_KEYCLOAK_ID);

        assertThat(savedUser.getBankAccount()).isNotNull();
        assertThat(savedUser.getBankAccount().getBalance()).isEqualTo(INITIAL_BALANCE);
        assertThat(savedUser.getBankAccount().getCurrency()).isEqualTo("RUB");
        assertThat(savedUser.getBankAccount().getIsActive()).isTrue();
        assertThat(savedUser.getBankAccount().getAccountNumber()).startsWith("ACC");
    }

    @Test
    @DisplayName("createUserAndAccount - должен генерировать уникальный номер счета")
    void createUserAndAccount_ShouldGenerateUniqueAccountNumber() {
        UserDto userDto1 = createTestUserDto();
        UserDto userDto2 = createTestUserDto();
        userDto2.setKeycloakId("different-keycloak-id-456");
        userDto2.setLogin("testuser2");
        userDto2.setEmail("test2@example.com");

        accountService.createUserAndAccount(userDto1);
        accountService.createUserAndAccount(userDto2);

        BankUser user1 = bankUserRepository.findByKeycloakId(TEST_KEYCLOAK_ID).orElseThrow();
        BankUser user2 = bankUserRepository.findByKeycloakId("different-keycloak-id-456").orElseThrow();

        assertThat(user1.getBankAccount().getAccountNumber())
                .isNotEqualTo(user2.getBankAccount().getAccountNumber());
    }

    @Test
    @DisplayName("putCash - успешное пополнение счета")
    void putCash_ShouldIncreaseBalance_WhenSuccessful() {
        createTestUserAndAccount();
        CashAccountDto cashAccountDto = new CashAccountDto();
        cashAccountDto.setKeycloakId(TEST_KEYCLOAK_ID);
        cashAccountDto.setValue("500");

        UserDto result = accountService.putCash(cashAccountDto);

        assertThat(result.getBalance()).isEqualTo(new BigDecimal("1500"));

        BankUser updatedUser = bankUserRepository.findByKeycloakId(TEST_KEYCLOAK_ID).orElseThrow();
        assertThat(updatedUser.getBankAccount().getBalance()).isEqualTo(new BigDecimal("1500"));
    }

    @Test
    @DisplayName("putCash - множественное пополнение счета")
    void putCash_MultipleDeposits_ShouldAccumulateBalance() {
        createTestUserAndAccount();
        CashAccountDto firstDeposit = new CashAccountDto();
        firstDeposit.setKeycloakId(TEST_KEYCLOAK_ID);
        firstDeposit.setValue("300");

        CashAccountDto secondDeposit = new CashAccountDto();
        secondDeposit.setKeycloakId(TEST_KEYCLOAK_ID);
        secondDeposit.setValue("200");

        accountService.putCash(firstDeposit);
        UserDto result = accountService.putCash(secondDeposit);

        assertThat(result.getBalance()).isEqualTo(new BigDecimal("1500")); // 1000 + 300 + 200
    }

    @Test
    @DisplayName("putCash - при несуществующем пользователе должен выбросить исключение")
    void putCash_WhenUserNotFound_ShouldThrowException() {
        CashAccountDto cashAccountDto = new CashAccountDto();
        cashAccountDto.setKeycloakId("non-existent-id");
        cashAccountDto.setValue("500");

        assertThatThrownBy(() -> accountService.putCash(cashAccountDto))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getCash - успешное снятие денег со счета")
    void getCash_ShouldDecreaseBalance_WhenSufficientFunds() {
        createTestUserAndAccount();
        CashAccountDto cashAccountDto = new CashAccountDto();
        cashAccountDto.setKeycloakId(TEST_KEYCLOAK_ID);
        cashAccountDto.setValue("300");

        UserDto result = accountService.getCash(cashAccountDto);

        assertThat(result.getBalance()).isEqualTo(new BigDecimal("700"));

        BankUser updatedUser = bankUserRepository.findByKeycloakId(TEST_KEYCLOAK_ID).orElseThrow();
        assertThat(updatedUser.getBankAccount().getBalance()).isEqualTo(new BigDecimal("700"));
    }

    @Test
    @DisplayName("getCash - при недостатке средств должно выбросить CashException")
    void getCash_WhenInsufficientFunds_ShouldThrowCashException() {
        createTestUserAndAccount();
        CashAccountDto cashAccountDto = new CashAccountDto();
        cashAccountDto.setKeycloakId(TEST_KEYCLOAK_ID);
        cashAccountDto.setValue("1500");

        assertThatThrownBy(() -> accountService.getCash(cashAccountDto))
                .isInstanceOf(CashException.class)
                .hasMessageContaining("Недостаточно средств")
                .hasMessageContaining("Доступно: 1000,00")  // Используем запятую
                .hasMessageContaining("Запрошено: 1500,00");  // Используем запятую

        BankUser unchangedUser = bankUserRepository.findByKeycloakId(TEST_KEYCLOAK_ID).orElseThrow();
        assertThat(unchangedUser.getBankAccount().getBalance()).isEqualTo(INITIAL_BALANCE);
    }

    @Test
    @DisplayName("getCash - снятие точной суммы (весь баланс)")
    void getCash_WithdrawAllFunds_ShouldSetBalanceToZero() {
        createTestUserAndAccount();
        CashAccountDto cashAccountDto = new CashAccountDto();
        cashAccountDto.setKeycloakId(TEST_KEYCLOAK_ID);
        cashAccountDto.setValue("1000");

        UserDto result = accountService.getCash(cashAccountDto);

        assertThat(result.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getCash - при несуществующем пользователе должен выбросить исключение")
    void getCash_WhenUserNotFound_ShouldThrowException() {
        CashAccountDto cashAccountDto = new CashAccountDto();
        cashAccountDto.setKeycloakId("non-existent-id");
        cashAccountDto.setValue("500");

        assertThatThrownBy(() -> accountService.getCash(cashAccountDto))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Комбинированный тест - пополнение и снятие")
    void putCashAndGetCash_CombinedOperations_ShouldWorkCorrectly() {
        createTestUserAndAccount();
        CashAccountDto putDto = new CashAccountDto();
        putDto.setKeycloakId(TEST_KEYCLOAK_ID);
        putDto.setValue("500");

        CashAccountDto getDto = new CashAccountDto();
        getDto.setKeycloakId(TEST_KEYCLOAK_ID);
        getDto.setValue("200");

        accountService.putCash(putDto);
        UserDto result = accountService.getCash(getDto);

        assertThat(result.getBalance()).isEqualTo(new BigDecimal("1300"));

        BankUser finalUser = bankUserRepository.findByKeycloakId(TEST_KEYCLOAK_ID).orElseThrow();
        assertThat(finalUser.getBankAccount().getBalance()).isEqualTo(new BigDecimal("1300"));
    }

    @Test
    @DisplayName("Комбинированный тест - несколько операций с проверкой истории")
    void multipleOperations_ShouldMaintainConsistentBalance() {
        createTestUserAndAccount();

        CashAccountDto op1 = new CashAccountDto();
        op1.setKeycloakId(TEST_KEYCLOAK_ID);
        op1.setValue("200");
        accountService.putCash(op1);

        CashAccountDto op2 = new CashAccountDto();
        op2.setKeycloakId(TEST_KEYCLOAK_ID);
        op2.setValue("500");
        accountService.putCash(op2);

        CashAccountDto op3 = new CashAccountDto();
        op3.setKeycloakId(TEST_KEYCLOAK_ID);
        op3.setValue("300");
        accountService.getCash(op3);

        CashAccountDto op4 = new CashAccountDto();
        op4.setKeycloakId(TEST_KEYCLOAK_ID);
        op4.setValue("100");
        accountService.putCash(op4);

        BankUser finalUser = bankUserRepository.findByKeycloakId(TEST_KEYCLOAK_ID).orElseThrow();
        assertThat(finalUser.getBankAccount().getBalance()).isEqualTo(new BigDecimal("1500"));
    }

    @Test
    @DisplayName("createUserAndAccount - создание нескольких пользователей")
    void createUserAndAccount_MultipleUsers_ShouldAllBePersisted() {
        UserDto user1 = createTestUserDto();

        UserDto user2 = createTestUserDto();
        user2.setKeycloakId("keycloak-id-456");
        user2.setLogin("user2");
        user2.setEmail("user2@example.com");

        UserDto user3 = createTestUserDto();
        user3.setKeycloakId("keycloak-id-789");
        user3.setLogin("user3");
        user3.setEmail("user3@example.com");

        accountService.createUserAndAccount(user1);
        accountService.createUserAndAccount(user2);
        accountService.createUserAndAccount(user3);

        assertThat(bankUserRepository.findAll()).hasSize(3);
        assertThat(bankAccountRepository.findAll()).hasSize(3);

        assertThat(bankUserRepository.findByKeycloakId("keycloak-id-789")).isPresent();
        assertThat(bankUserRepository.findByKeycloakId("keycloak-id-789").get().getBankAccount())
                .isNotNull();
    }

    private UserDto createTestUserDto() {
        UserDto userDto = new UserDto();
        userDto.setKeycloakId(TEST_KEYCLOAK_ID);
        userDto.setLogin(TEST_USERNAME);
        userDto.setPassword("password123");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setBirthday(LocalDate.of(1990, 1, 1));
        userDto.setEmail(TEST_EMAIL);
        userDto.setBalance(INITIAL_BALANCE);
        return userDto;
    }

    private void createTestUserAndAccount() {
        UserDto userDto = createTestUserDto();
        accountService.createUserAndAccount(userDto);
    }
}