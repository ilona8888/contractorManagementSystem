package com.ilona.contractormanagementsystem.controller;

import com.ilona.contractormanagementsystem.dto.ClientOrgForm;
import com.ilona.contractormanagementsystem.service.ClientOrgService;
import com.ilona.contractormanagementsystem.service.TariffService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/client-org")
@RequiredArgsConstructor
public class ClientOrgController {

    private final ClientOrgService clientOrgService;
    private final SecurityContextRepository securityContextRepository;
    private final UserDetailsService userDetailsService;
    private final TariffService tariffService;

    @GetMapping("/become")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public String showBecomeClientForm(Model model) {
        if (!model.containsAttribute("clientOrgForm")) {
            model.addAttribute("clientOrgForm", new ClientOrgForm());
        }

        return "users-page/contractor/become-client";
    }

    @PostMapping("/become")
    @PreAuthorize("hasRole('CONTRACTOR')")
    public String becomeClient(
            @Valid @ModelAttribute("clientOrgForm") ClientOrgForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            return "users-page/contractor/become-client";
        }

        try {
            String email = authentication.getName();

            // 1. Создаём организацию и меняем роль в базе данных
            clientOrgService.becomeClient(email, form);

            // 2. Загружаем пользователя заново с новой ролью
            UserDetails updatedUser =
                    userDetailsService.loadUserByUsername(email);

            // 3. Создаём новую аутентификацию с актуальными полномочиями
            UsernamePasswordAuthenticationToken newAuthentication =
                    new UsernamePasswordAuthenticationToken(
                            updatedUser,
                            null,
                            updatedUser.getAuthorities()
                    );

            newAuthentication.setDetails(authentication.getDetails());

            // 4. Устанавливаем новый SecurityContext
            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(newAuthentication);
            SecurityContextHolder.setContext(context);

            // 5. Сохраняем обновлённую аутентификацию в сессии
            securityContextRepository.saveContext(
                    context,
                    request,
                    response
            );

            // 6. Показываем сообщение на следующей странице
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Вы успешно зарегистрированы как заказчик!"
            );

            return "redirect:/requests/client-org";

        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "users-page/contractor/become-client";
        }
    }

    // Страница тарифов и поиск по адресу
    @GetMapping("/tariffs")
    @PreAuthorize("hasRole('MANAGER')")
    public String getTariffs(
            @RequestParam(required = false) String address,
            Model model
    ) {
        model.addAttribute(
                "organizations",
                tariffService.searchOrganizations(address)
        );

        model.addAttribute("address", address);

        return "users-page/manager/tariffs";
    }

    // Изменение ставки организации
    @PostMapping("/tariffs/{id}/rate")
    @PreAuthorize("hasRole('MANAGER')")
    public String updateRate(
            @PathVariable Long id,
            @RequestParam BigDecimal rate,
            @RequestParam(required = false) String address,
            RedirectAttributes redirectAttributes
    ) {
        try {
            tariffService.updateRate(id, rate);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Ставка успешно обновлена."
            );
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        if (address != null && !address.isBlank()) {
            redirectAttributes.addAttribute("address", address);
        }

        return "redirect:/client-org/tariffs";
    }
}
