package com.ilona.contractormanagementsystem.service;


import com.ilona.contractormanagementsystem.dto.BidResponseDto;
import com.ilona.contractormanagementsystem.enam.BidStatus;
import com.ilona.contractormanagementsystem.enam.RequestStatus;
import com.ilona.contractormanagementsystem.entity.Bid;
import com.ilona.contractormanagementsystem.entity.Contractor;
import com.ilona.contractormanagementsystem.entity.WorkRequest;
import com.ilona.contractormanagementsystem.repository.BidRepository;
import com.ilona.contractormanagementsystem.repository.ContractorRepository;
import com.ilona.contractormanagementsystem.repository.WorkRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final WorkRequestRepository requestRepository;
    private final ContractorRepository contractorRepository;

    @Transactional
    public BidResponseDto apply(Long requestId, Long contractorId) {
        WorkRequest request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Заявка не найдена"));

        Contractor contractor = contractorRepository
                .findById(contractorId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Исполнитель не найден"));

        if (request.getStatus() != RequestStatus.OPEN) {
            throw new IllegalStateException(
                    "На эту заявку больше нельзя откликнуться");
        }

        if (bidRepository.existsByRequest_IdAndContractor_Id(
                requestId, contractorId)) {
            throw new IllegalStateException(
                    "Вы уже откликнулись на эту заявку");
        }

        Bid bid = new Bid();
        bid.setRequest(request);
        bid.setContractor(contractor);
        bid.setStatus(BidStatus.PENDING);

        if (bidRepository.existsByRequest_IdAndContractor_Id(
                requestId, contractorId)) {
            throw new IllegalStateException(
                    "Отклик уже найден при предварительной проверке. " +
                            "requestId=" + requestId +
                            ", contractorId=" + contractorId);
        }
        try {
            return BidResponseDto.from(bidRepository.saveAndFlush(bid));
        } catch (DataIntegrityViolationException ex) {
             //UNIQUE в БД защищает и от двух одновременных запросов.
            throw new IllegalStateException(
                    "Отклик уже существует", ex);
        }

    }

    @Transactional(readOnly = true)
    public List<BidResponseDto> getByContractor(Long contractorId) {
        if (!contractorRepository.existsById(contractorId)) {
            throw new EntityNotFoundException("Исполнитель не найден");
        }

        return bidRepository.findAllByContractor_Id(contractorId)
                .stream()
                .map(BidResponseDto::from)
                .toList();
    }

    @Transactional
    public BidResponseDto accept(Long bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Отклик не найден"));

        WorkRequest request = requestRepository
                .findByIdForUpdate(bid.getRequest().getId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Заявка не найдена"));

        if (request.getStatus() != RequestStatus.OPEN) {
            throw new IllegalStateException(
                    "Заявка уже недоступна для выбора исполнителя");
        }

        if (bid.getStatus() != BidStatus.PENDING) {
            throw new IllegalStateException(
                    "Можно принять только ожидающий отклик");
        }

        bid.setStatus(BidStatus.ACCEPTED);

        List<Bid> otherBids =
                bidRepository.findAllByRequest_Id(request.getId());

        for (Bid other : otherBids) {
            if (!other.getId().equals(bidId)
                    && other.getStatus() == BidStatus.PENDING) {
                other.setStatus(BidStatus.REJECTED);
            }
        }

        request.setStatus(RequestStatus.AGREED);

        requestRepository.save(request);
        bidRepository.saveAll(otherBids);

        return BidResponseDto.from(bid);
    }
}

