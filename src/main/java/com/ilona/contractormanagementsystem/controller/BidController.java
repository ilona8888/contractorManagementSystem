package com.ilona.contractormanagementsystem.controller;

import com.ilona.contractormanagementsystem.entity.Contractor;
import com.ilona.contractormanagementsystem.repository.ContractorRepository;
import com.ilona.contractormanagementsystem.service.BidService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/contractor/bids")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CONTRACTOR')")
public class BidController {

    private final BidService bidService;
    private final ContractorRepository contractorRepository;

    @PostMapping
    public String apply(
            @RequestParam Long requestId,
            @AuthenticationPrincipal UserDetails user,
            RedirectAttributes redirectAttributes) {

        Contractor contractor = contractorRepository
                .findByUser_Email(user.getUsername())
                .orElseThrow(() ->
                        new EntityNotFoundException("Исполнитель не найден"));

        try {
            bidService.apply(requestId, contractor.getId());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Вы успешно откликнулись на заявку!"
            );

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/requests";
    }

    @GetMapping("/my-bids")
    public String getMyBids(
            @AuthenticationPrincipal UserDetails user,
            Model model) {

        Contractor contractor = contractorRepository
                .findByUser_Email(user.getUsername())
                .orElseThrow(() ->
                        new EntityNotFoundException("Исполнитель не найден"));

        model.addAttribute(
                "bids",
                bidService.getByContractor(contractor.getId())
        );

        return "/users-page/contractor/my-bits";
    }
}
