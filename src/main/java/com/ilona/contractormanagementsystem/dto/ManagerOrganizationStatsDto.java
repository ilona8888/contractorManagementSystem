package com.ilona.contractormanagementsystem.dto;

public record ManagerOrganizationStatsDto(
        Long organizationId,
        String organizationName,
        String address,
        long closedRequests,
        long notClosedRequests
) {
    public long totalRequests() {
        return closedRequests + notClosedRequests;
    }
}
