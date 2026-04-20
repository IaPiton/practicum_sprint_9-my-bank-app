package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    @GetMapping("/")
    public String getIndexPage() {
        return "redirect:/signup";
    }

    @GetMapping("/main")
    public String getMainPage(Model model, @AuthenticationPrincipal OidcUser user) {
        if (user != null) {
            model.addAttribute("username", user.getPreferredUsername());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("firstName", user.getGivenName());
            model.addAttribute("lastName", user.getFamilyName());
        }
        return "main";
    }
}