package com.ilona.contractormanagementsystem.controller;

import com.ilona.contractormanagementsystem.dto.ContractorShiftDto;
import com.ilona.contractormanagementsystem.dto.OrganizationPaymentDto;
import com.ilona.contractormanagementsystem.entity.Contractor;
import com.ilona.contractormanagementsystem.repository.ContractorRepository;
import com.ilona.contractormanagementsystem.service.ContractorShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/contractor")
@PreAuthorize("hasRole('CONTRACTOR')")
@RequiredArgsConstructor
public class ContractorController {

    private final ContractorRepository contractorRepository;
    private final ContractorShiftService contractorShiftService;

    @GetMapping("/my-shifts")
    public String getMyShifts(
            @RequestParam(required = false) String month,
            Authentication authentication,
            Model model
    ) {
        YearMonth selectedMonth = (month == null || month.isBlank())
                ? YearMonth.now()
                : YearMonth.parse(month);

        Contractor contractor = contractorRepository
                .findByUser_Email(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Подрядчик не найден")
                );

        List<ContractorShiftDto> shifts =
                contractorShiftService.getShifts(
                        contractor.getId(),
                        selectedMonth
                );

        BigDecimal totalPayment = shifts.stream()
                .map(ContractorShiftDto::payment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, OrganizationPaymentDto> paymentsByOrganization =
                new LinkedHashMap<>();

        for (ContractorShiftDto shift : shifts) {
            OrganizationPaymentDto current =
                    paymentsByOrganization.get(shift.organizationName());

            if (current == null) {
                paymentsByOrganization.put(
                        shift.organizationName(),
                        new OrganizationPaymentDto(
                                1,
                                shift.payment()
                        )
                );
            } else {
                paymentsByOrganization.put(
                        shift.organizationName(),
                        new OrganizationPaymentDto(
                                current.shiftCount() + 1,
                                current.payment().add(shift.payment())
                        )
                );
            }
        }

        model.addAttribute("shifts", shifts);
        model.addAttribute("selectedMonth", selectedMonth.toString());
        model.addAttribute("totalPayment", totalPayment);
        model.addAttribute(
                "paymentsByOrganization",
                paymentsByOrganization
        );

        return "users-page/contractor/my-shifts";
    }
}