package com.ilona.contractormanagementsystem.controller;

import com.ilona.contractormanagementsystem.dto.CreateWorkRequestDto;
import com.ilona.contractormanagementsystem.dto.WorkRequestDetailsDto;
import com.ilona.contractormanagementsystem.entity.WorkRequest;
import com.ilona.contractormanagementsystem.service.BidService;
import com.ilona.contractormanagementsystem.service.PositionService;
import com.ilona.contractormanagementsystem.service.WorkRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
public class WorkRequestController {

    private final WorkRequestService workRequestService;
    private final PositionService positionService;
    private final BidService bidService;

    //Страница, которая отдает все созданные заявки
    @GetMapping({"", "/"})
    @PreAuthorize("hasRole('CONTRACTOR')")
    public String Requests(Model model) {
        model.addAttribute("requests", workRequestService.getOpenAllRequestsForView());
        return "users-page/contractor/requests";
    }

    //страница заявок созданных клиентом
    @GetMapping({"/client-org"})
    @PreAuthorize("hasRole('CLIENT_ORG')")
    public String getRequests(@AuthenticationPrincipal UserDetails user, Model model) {
        model.addAttribute("requests", workRequestService.getClientOpenRequestsForView(user.getUsername()));
        return "users-page/client-org/requests";
    }

    //Страница с созданием заявки
    @GetMapping("/new")
    @PreAuthorize("hasRole('CLIENT_ORG')")
    public String showCreateForm(Model model) {
        model.addAttribute("request", new CreateWorkRequestDto());
        model.addAttribute("positions", positionService.getActivePositions());

        return "users-page/client-org/request-create";
    }

    // Создание заявки
    @PostMapping
    @PreAuthorize("hasRole('CLIENT_ORG')")
    public String create(
            @Valid @ModelAttribute("request") CreateWorkRequestDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails user,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("positions", positionService.getActivePositions());
            return "users-page/client-org/request-create";
        }

        try {
            workRequestService.create(dto, user.getUsername());

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Заявка успешно создана"
            );

            return "redirect:/requests/client-org";

        } catch (IllegalStateException e) {
            model.addAttribute("positions", positionService.getActivePositions());
            model.addAttribute("errorMessage", e.getMessage());

            return "users-page/client-org/request-create";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT_ORG')")
    public String getRequestDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user,
            Model model
    ) {
        WorkRequestDetailsDto details =
                workRequestService.getRequestDetails(
                        id,
                        user.getUsername()
                );

        model.addAttribute("details", details);
        model.addAttribute("request", details.request());
        model.addAttribute("bids", details.bids());

        return "users-page/client-org/request-details";
    }

    @PostMapping("/{requestId}/bids/{bidId}/accept")
    @PreAuthorize("hasRole('CLIENT_ORG')")
    public String acceptBid(
            @PathVariable Long requestId,
            @PathVariable Long bidId,
            @AuthenticationPrincipal UserDetails user,
            RedirectAttributes redirectAttributes) {

        // Проверяем доступ к заявке текущего заказчика.
        WorkRequestDetailsDto details =
                workRequestService.getRequestDetails(requestId, user.getUsername());

        // Проверяем, что выбранный отклик относится именно к этой заявке.
        boolean bidBelongsToRequest = details.bids().stream()
                .anyMatch(bid -> bid.id().equals(bidId));

        if (!bidBelongsToRequest) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "Отклик не найден"
            );
        }

        bidService.accept(bidId);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Исполнитель выбран. Остальные отклики отклонены."
        );

        return "redirect:/requests/" + requestId;
    }

    @GetMapping("full-requests")
    @PreAuthorize("hasRole('MANAGER')")
    public String getAllRequestsForManager(Model model) {
        model.addAttribute(
                "requests",
                workRequestService.getAllRequestsForManager()
        );

        return "users-page/manager/requests";
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('MANAGER')")
    public String getOrganizationStatistics(Model model) {
        model.addAttribute(
                "statistics",
                workRequestService.getOrganizationStatistics()
        );

        return "users-page/manager/statistics";
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('CLIENT_ORG')")
    public String completeRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user,
            RedirectAttributes redirectAttributes
    ) {
        try {
            workRequestService.completeRequest(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Работа отмечена как выполненная."
            );
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/requests/client-org";
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CLIENT_ORG')")
    public String cancelRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user,
            RedirectAttributes redirectAttributes
    ) {
        try {
            workRequestService.cancelRequest(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Заявка отменена."
            );
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/requests/client-org";
    }
}
