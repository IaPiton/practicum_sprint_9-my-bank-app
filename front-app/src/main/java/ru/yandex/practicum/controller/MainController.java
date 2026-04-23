package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.Account;
import ru.yandex.practicum.dto.CashAction;
import ru.yandex.practicum.service.AccountService;
import ru.yandex.practicum.service.CashService;
import ru.yandex.practicum.validator.BirthDay;

import java.time.LocalDate;

@Controller
@Validated
@RequiredArgsConstructor
public class MainController {
    private final AccountService accountService;
    private final CashService cashService;

    @GetMapping("/")
    public String getIndexPage() {
        return "redirect:/signup";
    }

    @GetMapping("/main")
    public String getMainPage(Model model,
                              @AuthenticationPrincipal OidcUser user) {
        accountService.getAccount(user.getSubject(), model);
        return "main";
    }

    @PostMapping("/account")
    public String editAccount(
            Model model,
            @RequestParam("name") String fullName,
            @RequestParam("birthdate") @BirthDay LocalDate birthdate,
            @AuthenticationPrincipal OidcUser user) {
        model.addAttribute("info", accountService.updateAccount(user.getSubject(), fullName, birthdate));
        accountService.getAccount(user.getSubject(), model);
        return "main";
    }

    @PostMapping("/cash")
    public String editCash(
            Model model,
            @RequestParam("value") int value,
            @RequestParam("action") CashAction action,
            @AuthenticationPrincipal OidcUser user
    ) {
        cashService.editCash(model, value, action, user.getSubject());
        accountService.getAccount(user.getSubject(), model);
        return "main";
    }
}