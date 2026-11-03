package com.ilona.contractormanagementsystem.service;

import com.ilona.contractormanagementsystem.entity.WorkRequestStatus;
import com.ilona.contractormanagementsystem.repository.WorkRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class RequestExpirationService {

    private final WorkRequestRepository requestRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireOldRequests() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));
        requestRepository.expireOldRequests(
                today,
                WorkRequestStatus.EXPIRED,
                WorkRequestStatus.OPEN,
                WorkRequestStatus.AGREED
        );
    }
}
