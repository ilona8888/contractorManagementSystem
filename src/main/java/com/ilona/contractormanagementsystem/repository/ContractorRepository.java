package com.ilona.contractormanagementsystem.repository;

import com.ilona.contractormanagementsystem.entity.Contractor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractorRepository extends JpaRepository<Contractor, Long> {

    Optional<Contractor> findByUser_Email(String userEmail);
}
