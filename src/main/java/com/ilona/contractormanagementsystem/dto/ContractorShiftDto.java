package com.ilona.contractormanagementsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ContractorShiftDto(
        String organizationName,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal hourlyRate,
        BigDecimal hours,
        BigDecimal payment
) {
}
