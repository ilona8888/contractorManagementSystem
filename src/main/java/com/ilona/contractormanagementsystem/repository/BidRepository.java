package com.ilona.contractormanagementsystem.repository;

import com.ilona.contractormanagementsystem.entity.BidStatus;
import com.ilona.contractormanagementsystem.entity.WorkRequestStatus;
import com.ilona.contractormanagementsystem.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findAllByRequest_IdOrderByCreatedAtDesc(Long requestId);

    boolean existsByRequest_IdAndContractor_Id(Long requestId, Long contractorId);

    List<Bid> findAllByRequest_Id(Long requestId);

    List<Bid> findAllByContractor_Id(Long contractorId);

    List<Bid> findAllByContractor_IdAndStatusAndRequest_StatusAndRequest_WorkDateBetweenOrderByRequest_WorkDateAsc(
            Long contractorId,
            BidStatus bidStatus,
            WorkRequestStatus requestStatus,
            LocalDate startDate,
            LocalDate endDate
    );
}
