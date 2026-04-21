package ru.yandex.practicum.persistent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bank_user", schema = "bank_account")
public class BankUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "keycloak_id", nullable = false, unique = true, length = 255)
    private String keycloakId;

    @Column(nullable = false, unique = true, length = 255)
    private String username;

    @Column(nullable = false)
    private LocalDate birthday;

    @Column(name = "first_name", nullable = false, length = 255)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 255)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @OneToOne(mappedBy = "bankUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private BankAccount bankAccount;

}
