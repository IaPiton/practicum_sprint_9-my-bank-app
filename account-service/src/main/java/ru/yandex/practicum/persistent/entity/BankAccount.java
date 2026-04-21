package ru.yandex.practicum.persistent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bank_account", schema = "bank_account")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private BankUser bankUser;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(name = "account_number", unique = true, length = 50)
    private String accountNumber;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "RUB";

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}