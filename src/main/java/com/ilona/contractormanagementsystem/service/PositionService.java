package com.ilona.contractormanagementsystem.service;

import com.ilona.contractormanagementsystem.entity.Position;
import com.ilona.contractormanagementsystem.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;

    public List<Position> getActivePositions() {
        return positionRepository.findAllByActiveTrue();
    }

    @Transactional
    public void addPosition(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Введите название должности.");
        }

        String normalizedName = name.trim();

        if (positionRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException(
                    "Такая должность уже существует."
            );
        }

        Position position = new Position();
        position.setName(normalizedName);
        position.setActive(true);

        positionRepository.save(position);
    }

    @Transactional
    public void changeActiveStatus(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Должность не найдена."));

        position.setActive(!position.isActive());
        positionRepository.save(position);
    }

}
