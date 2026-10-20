package com.ilona.contractormanagementsystem.service;

import com.ilona.contractormanagementsystem.entity.ClientOrg;
import com.ilona.contractormanagementsystem.repository.ClientOrgRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TariffService {
    private final ClientOrgRepository clientOrgRepository;

    public TariffService(ClientOrgRepository clientOrgRepository) {
        this.clientOrgRepository = clientOrgRepository;
    }

    @Transactional(readOnly = true)
    public List<ClientOrg> searchOrganizations(String address) {
        if (address == null || address.isBlank()) {
            return clientOrgRepository.findAllByOrderByNameAsc();
        }

        return clientOrgRepository
                .findByAddressContainingIgnoreCaseOrderByNameAsc(
                        address.trim()
                );
    }

    @Transactional
    public void updateRate(Long clientOrgId, BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Ставка должна быть больше нуля"
            );
        }

        ClientOrg clientOrg = clientOrgRepository.findById(clientOrgId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Организация не найдена"
                ));

        clientOrg.setRate(rate);
        clientOrgRepository.save(clientOrg);
    }
}
