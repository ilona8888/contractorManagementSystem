package com.ilona.contractormanagementsystem.dto;

import com.ilona.contractormanagementsystem.enam.BidStatus;

public record ManagerBidDto(
        String contractorEmail,
        BidStatus status
) {
}
