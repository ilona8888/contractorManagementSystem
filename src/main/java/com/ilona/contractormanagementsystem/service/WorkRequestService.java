package com.ilona.contractormanagementsystem.service;

import com.ilona.contractormanagementsystem.dto.*;
import com.ilona.contractormanagementsystem.entity.WorkRequestStatus;
import com.ilona.contractormanagementsystem.entity.ClientOrg;
import com.ilona.contractormanagementsystem.entity.Position;
import com.ilona.contractormanagementsystem.entity.WorkRequest;
import com.ilona.contractormanagementsystem.repository.BidRepository;
import com.ilona.contractormanagementsystem.repository.ClientOrgRepository;
import com.ilona.contractormanagementsystem.repository.PositionRepository;
import com.ilona.contractormanagementsystem.repository.WorkRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkRequestService {

    private final WorkRequestRepository workRequestRepository;
    private final ClientOrgRepository clientOrgRepository;
    private final PositionRepository positionRepository;
    private final BidRepository bidRepository;

    @Transactional
    public WorkRequest create(CreateWorkRequestDto dto, String email) {

        ClientOrg clientOrg = clientOrgRepository.findByUser_Email(email)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Организация не найдена"
                        ));

        if (clientOrg.getRate() == null) {
            throw new IllegalStateException(
                    "Менеджер ещё не назначил тариф. Создание заявок пока недоступно."
            );
        }

        Position position = positionRepository
                .findById(dto.getPositionId())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Позиция не найдена"
                        ));


        validateWorkTime(dto.getWorkDate(), dto.getStartTime(), dto.getEndTime());
        WorkRequest request = new WorkRequest();

        request.setClientOrg(clientOrg);
        request.setPosition(position);
        request.setWorkDate(dto.getWorkDate());
        request.setStartTime(dto.getStartTime());
        request.setEndTime(dto.getEndTime());
        request.setHourlyRate(clientOrg.getRate());

        return workRequestRepository.save(request);
    }

    private void validateWorkTime(LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Время начала должно быть раньше времени окончания");
        }

        if (workDate.isEqual(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            if (!startTime.isAfter(now)) {
                throw new IllegalArgumentException(
                        "Для сегодняшней работы время начала должно быть позже текущего времени"
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public List<WorkRequestViewDto> getOpenAllRequestsForView() {

        List<WorkRequest> requests =
                workRequestRepository.findAllByStatus(WorkRequestStatus.OPEN);

        return requests.stream()
                .map(request -> {
                    long minutes = Duration.between(
                            request.getStartTime(),
                            request.getEndTime()
                    ).toMinutes();

                    BigDecimal hours = BigDecimal.valueOf(minutes)
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

                    BigDecimal amount = request.getHourlyRate()
                            .multiply(hours)
                            .setScale(2, RoundingMode.HALF_UP);

                    return new WorkRequestViewDto(
                            request.getId(),
                            request.getPosition().getName(),
                            request.getWorkDate(),
                            request.getStartTime(),
                            request.getEndTime(),
                            amount,
                            request.getStatus(),
                            request.getBids().size(),
                            request.getClientOrg().getAddress()
                    );
                })
                .toList();
    }

    //Отображение заявки у заказчика
    public List<WorkRequestViewDto> getClientRequestsForView(String email) {
        ClientOrg clientOrg = clientOrgRepository.findByUser_Email(email)
                .orElseThrow(() ->
                        new EntityNotFoundException("Организация не найдена"));

        return workRequestRepository
                .findAllByClientOrg_IdOrderByWorkDateDesc(clientOrg.getId())
                .stream()
                .map(WorkRequestViewDto::from)
                .toList();
    }


    @Transactional(readOnly = true)
    public WorkRequestDetailsDto getRequestDetails(
            Long requestId,
            String email
    ) {
        ClientOrg clientOrg = clientOrgRepository
                .findByUser_Email(email)
                .orElseThrow(() ->
                        new EntityNotFoundException("Организация не найдена")
                );

        WorkRequest request = workRequestRepository
                .findByIdAndClientOrg_Id(requestId, clientOrg.getId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Заявка не найдена")
                );

        List<BidResponseDto> bids = bidRepository
                .findAllByRequest_IdOrderByCreatedAtDesc(requestId)
                .stream()
                .map(BidResponseDto::from)
                .toList();

        return new WorkRequestDetailsDto(
                WorkRequestViewDto.from(request),
                bids
        );
    }

    @Transactional(readOnly = true)
    public List<ManagerRequestDto> getAllRequestsForManager() {
        return workRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(request -> {
                    List<ManagerBidDto> bids =
                            bidRepository
                                    .findAllByRequest_IdOrderByCreatedAtDesc(
                                            request.getId()
                                    )
                                    .stream()
                                    .map(bid -> new ManagerBidDto(
                                            bid.getContractor()
                                                    .getUser()
                                                    .getEmail(),
                                            bid.getStatus()
                                    ))
                                    .toList();

                    return new ManagerRequestDto(
                            request.getId(),
                            request.getClientOrg().getName(),
                            request.getPosition().getName(),
                            request.getHourlyRate(),
                            request.getWorkDate(),
                            request.getStartTime(),
                            request.getEndTime(),
                            request.getStatus(),
                            bids
                    );
                })
                .toList();
    }

    public List<ManagerOrganizationStatsDto> getOrganizationStatistics() {
        List<ClientOrg> organizations =
                clientOrgRepository.findAllByOrderByNameAsc();

        return organizations.stream()
                .map(org -> {
                    long closed = workRequestRepository
                            .countByClientOrg_IdAndStatus(
                                    org.getId(),
                                    WorkRequestStatus.CLOSED
                            );

                    long expired = workRequestRepository.
                            countByClientOrg_IdAndStatus(
                                    org.getId(),
                                    WorkRequestStatus.EXPIRED);

                    return new ManagerOrganizationStatsDto(
                            org.getId(),
                            org.getName(),
                            org.getAddress(),
                            closed,
                            expired
                    );
                })
                .toList();
    }


    @Transactional
    public void completeRequest(Long requestId) {
        WorkRequest request = workRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Заявка не найдена"));

        if (request.getStatus() != WorkRequestStatus.AGREED) {
            throw new IllegalStateException(
                    "Завершить можно только согласованную заявку");
        }

        request.setStatus(WorkRequestStatus.CLOSED);
        workRequestRepository.save(request);
    }

    @Transactional
    public void cancelRequest(Long requestId) {
        WorkRequest request = workRequestRepository.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Заявка не найдена"));

        if (request.getStatus() == WorkRequestStatus.CLOSED
                || request.getStatus() == WorkRequestStatus.EXPIRED
                || request.getStatus() == WorkRequestStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Эту заявку уже нельзя отменить");
        }

        request.setStatus(WorkRequestStatus.CANCELLED);
        workRequestRepository.save(request);
    }
}
