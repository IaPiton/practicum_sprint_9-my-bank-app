package ru.yandex.practicum.persistent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.persistent.entity.BankAccount;
import ru.yandex.practicum.persistent.entity.BankUser;

import java.util.UUID;

@Repository
public interface BankUserRepository extends JpaRepository<BankUser, UUID> {
}
