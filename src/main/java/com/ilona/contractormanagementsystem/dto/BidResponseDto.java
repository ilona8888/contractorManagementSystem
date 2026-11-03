package com.ilona.contractormanagementsystem.dto;

import com.ilona.contractormanagementsystem.entity.BidStatus;
import com.ilona.contractormanagementsystem.entity.Bid;
import com.ilona.contractormanagementsystem.entity.WorkRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public record BidResponseDto(
        Long id,
        Long requestId,
        String contractorName,
        BidStatus status,
        OffsetDateTime createdAt,
        String positionName,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime
) {
    public static BidResponseDto from(Bid bid) {
        WorkRequest request = bid.getRequest();

        return new BidResponseDto(
                bid.getId(),
                request.getId(),
                bid.getContractor().getUser().getEmail(),
                bid.getStatus(),
                bid.getCreatedAt(),
                request.getPosition().getName(),
                request.getWorkDate(),
                request.getStartTime(),
                request.getEndTime()
        );
    }
}
