package com.ilona.contractormanagementsystem.service;

import com.ilona.contractormanagementsystem.enam.RequestStatus;
import com.ilona.contractormanagementsystem.repository.WorkRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RequestExpirationService {

    private final WorkRequestRepository requestRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireOldRequests() {
        requestRepository.expireOldRequests(
                LocalDate.now(),
                RequestStatus.EXPIRED,
                RequestStatus.OPEN,
                RequestStatus.AGREED
        );
    }
}
