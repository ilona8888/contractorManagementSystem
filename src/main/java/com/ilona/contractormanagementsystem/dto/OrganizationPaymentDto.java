package com.ilona.contractormanagementsystem.dto;

import java.math.BigDecimal;

public record OrganizationPaymentDto(
        long shiftCount,
        BigDecimal payment
) {
}
