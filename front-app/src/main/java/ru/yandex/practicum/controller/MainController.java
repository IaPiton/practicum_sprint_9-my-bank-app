package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.yandex.practicum.dto.Account;
import ru.yandex.practicum.service.AccountService;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final AccountService accountService;

    @GetMapping("/")
    public String getIndexPage() {
        return "redirect:/signup";
    }

    @GetMapping("/main")
    public String getMainPage(Model model,
                              @AuthenticationPrincipal OidcUser user) {

       Account account =  accountService.getAccount(user.getSubject());
        model.addAttribute("fullName", account.getFirstName() + " " + account.getLastName());
        model.addAttribute("birthdate", account.getBirthday());
        model.addAttribute("sum", account.getBalance());
        return "main";
    }
}