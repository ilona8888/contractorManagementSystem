package com.ilona.contractormanagementsystem.dto;

import com.ilona.contractormanagementsystem.entity.BidStatus;

public record ManagerBidDto(
        String contractorEmail,
        BidStatus status
) {
}
