package com.ilona.contractormanagementsystem.dto;


import com.ilona.contractormanagementsystem.enam.RequestStatus;
import com.ilona.contractormanagementsystem.entity.WorkRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class WorkRequestViewDto {

    private Long id;
    private String positionName;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal amount;
    private RequestStatus status;
    private long bidCount;

    public static WorkRequestViewDto from(WorkRequest request) {
        long minutes = Duration.between(
                request.getStartTime(),
                request.getEndTime()
        ).toMinutes();

        BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        BigDecimal amount = request.getHourlyRate().multiply(hours).setScale(2, RoundingMode.HALF_UP);

        return new WorkRequestViewDto(
                request.getId(),
                request.getPosition().getName(),
                request.getWorkDate(),
                request.getStartTime(),
                request.getEndTime(),
                amount,
                request.getStatus(),
                request.getBids().size()
        );
    }

}
