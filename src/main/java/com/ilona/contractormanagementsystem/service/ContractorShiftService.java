package com.ilona.contractormanagementsystem.service;

import com.ilona.contractormanagementsystem.dto.ContractorShiftDto;
import com.ilona.contractormanagementsystem.enam.BidStatus;
import com.ilona.contractormanagementsystem.enam.RequestStatus;
import com.ilona.contractormanagementsystem.entity.Bid;
import com.ilona.contractormanagementsystem.entity.WorkRequest;
import com.ilona.contractormanagementsystem.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractorShiftService {

    private final BidRepository bidRepository;

    public List<ContractorShiftDto> getShifts(
            Long contractorId,
            YearMonth month
    ) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        List<Bid> bids = bidRepository
                .findAllByContractor_IdAndStatusAndRequest_StatusAndRequest_WorkDateBetweenOrderByRequest_WorkDateAsc(
                        contractorId,
                        BidStatus.ACCEPTED,
                        RequestStatus.CLOSED,
                        startDate,
                        endDate
                );

        return bids.stream()
                .map(bid -> {
                    WorkRequest request = bid.getRequest();

                    long minutes = Duration.between(
                            request.getStartTime(),
                            request.getEndTime()
                    ).toMinutes();

                    BigDecimal hours = BigDecimal.valueOf(minutes)
                            .divide(
                                    BigDecimal.valueOf(60),
                                    2,
                                    RoundingMode.HALF_UP
                            );

                    BigDecimal payment = request.getHourlyRate()
                            .multiply(hours)
                            .setScale(2, RoundingMode.HALF_UP);

                    return new ContractorShiftDto(
                            request.getClientOrg().getName(),
                            request.getWorkDate(),
                            request.getStartTime(),
                            request.getEndTime(),
                            request.getHourlyRate(),
                            hours,
                            payment
                    );
                })
                .toList();
    }
}