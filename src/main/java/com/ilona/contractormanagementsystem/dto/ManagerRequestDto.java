package com.ilona.contractormanagementsystem.dto;


import com.ilona.contractormanagementsystem.entity.WorkRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ManagerRequestDto(
        Long id,
        String clientOrgName,
        String positionName,
        BigDecimal hourlyRate,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        WorkRequestStatus status,
        List<ManagerBidDto> bids
) {
}