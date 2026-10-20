package com.ilona.contractormanagementsystem.repository;

import com.ilona.contractormanagementsystem.entity.ClientOrg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientOrgRepository extends JpaRepository<ClientOrg, Long> {

    Optional<ClientOrg> findByUser_Email(String email);

    boolean existsByUser_Email(String email);

    List<ClientOrg> findAllByOrderByNameAsc();

    List<ClientOrg> findByAddressContainingIgnoreCaseOrderByNameAsc(
            String address
    );


}
