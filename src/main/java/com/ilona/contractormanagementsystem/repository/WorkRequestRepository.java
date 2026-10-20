package com.ilona.contractormanagementsystem.repository;

import com.ilona.contractormanagementsystem.enam.RequestStatus;
import com.ilona.contractormanagementsystem.entity.WorkRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkRequestRepository extends JpaRepository<WorkRequest, Long> {

    List<WorkRequest> findAllByStatus(RequestStatus status);

    List<WorkRequest> findAllByClientOrg_IdOrderByWorkDateDesc(
            Long clientOrgId
    );

    Optional<WorkRequest> findByIdAndClientOrg_Id(
            Long id,
            Long clientOrgId
    );

    List<WorkRequest> findAllByOrderByCreatedAtDesc();

    long countByClientOrg_IdAndStatus(
            Long clientOrgId,
            RequestStatus status
    );

    long countByClientOrg_Id(Long clientOrgId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from WorkRequest r where r.id = :id")
    Optional<WorkRequest> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("""
    UPDATE WorkRequest r
    SET r.status = :expiredStatus
    WHERE r.workDate < :today
      AND r.status IN (:openStatus, :agreedStatus)
""")
    int expireOldRequests(
            @Param("today") LocalDate today,
            @Param("expiredStatus") RequestStatus expiredStatus,
            @Param("openStatus") RequestStatus openStatus,
            @Param("agreedStatus") RequestStatus agreedStatus
    );
}
