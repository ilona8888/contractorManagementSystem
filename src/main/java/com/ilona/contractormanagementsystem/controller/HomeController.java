package com.ilona.contractormanagementsystem.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        boolean isManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        boolean isContractor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CONTRACTOR"));

        boolean isClientOrg = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT_ORG"));

        if (isManager) {
            return "redirect:/requests/full-requests";
        }

        if (isContractor) {
            return "redirect:/requests";
        }

        if (isClientOrg) {
            return "redirect:/requests/client-org";
        }

        return "redirect:/auth/login";
    }
}
