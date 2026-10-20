package com.ilona.contractormanagementsystem.dto;

import java.util.List;

public record WorkRequestDetailsDto(
        WorkRequestViewDto request,
        List<BidResponseDto> bids
) {
}
