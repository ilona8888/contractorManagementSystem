package com.ilona.contractormanagementsystem.repository;

import com.ilona.contractormanagementsystem.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findAllByActiveTrue();
    boolean existsByNameIgnoreCase(String name);

}
