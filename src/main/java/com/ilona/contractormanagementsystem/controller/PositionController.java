package com.ilona.contractormanagementsystem.controller;

import com.ilona.contractormanagementsystem.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/positions")
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public String positions(Model model) {
        model.addAttribute("positions",
                positionService.getActivePositions());

        return "users-page/manager/positions";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('MANAGER')")
    public String addPosition(
            @RequestParam String name,
            RedirectAttributes redirectAttributes) {

        try {
            positionService.addPosition(name);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Должность успешно добавлена."
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage()
            );
        }

        return "redirect:/positions";
    }

    @PostMapping("/{id}/toggle")
    public String togglePosition(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            positionService.changeActiveStatus(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Статус должности изменён."
            );
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage()
            );
        }

        return "redirect:/positions";
    }
}
