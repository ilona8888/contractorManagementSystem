package com.ilona.contractormanagementsystem.service;

import com.ilona.contractormanagementsystem.dto.CreateWorkRequestDto;
import com.ilona.contractormanagementsystem.dto.WorkRequestViewDto;
import com.ilona.contractormanagementsystem.enam.RequestStatus;
import com.ilona.contractormanagementsystem.entity.ClientOrg;
import com.ilona.contractormanagementsystem.entity.Position;
import com.ilona.contractormanagementsystem.entity.WorkRequest;
import com.ilona.contractormanagementsystem.repository.BidRepository;
import com.ilona.contractormanagementsystem.repository.ClientOrgRepository;
import com.ilona.contractormanagementsystem.repository.PositionRepository;
import com.ilona.contractormanagementsystem.repository.WorkRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkRequestServiceTest {

    @Mock
    private WorkRequestRepository workRequestRepository;

    @Mock
    private ClientOrgRepository clientOrgRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private WorkRequestService workRequestService;

    private ClientOrg clientOrg;
    private Position position;
    private WorkRequest request;

    @BeforeEach
    void setUp() {
        clientOrg = new ClientOrg();
        clientOrg.setRate(new BigDecimal("20.00"));

        position = new Position();
        position.setName("Официант");

        request = new WorkRequest();
        request.setId(1L);
        request.setClientOrg(clientOrg);
        request.setPosition(position);
        request.setWorkDate(LocalDate.of(2026, 10, 25));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(15, 0));
        request.setHourlyRate(new BigDecimal("20.00"));
        request.setStatus(RequestStatus.OPEN);
    }


    @Test
    void create_shouldSaveRequest_whenDataIsValid() {
        CreateWorkRequestDto dto = new CreateWorkRequestDto();
        dto.setPositionId(1L);
        dto.setWorkDate(LocalDate.of(2026, 10, 25));
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndTime(LocalTime.of(15, 0));

        when(clientOrgRepository.findByUser_Email("client@test.com"))
                .thenReturn(Optional.of(clientOrg));

        when(positionRepository.findById(1L))
                .thenReturn(Optional.of(position));

        when(workRequestRepository.save(any(WorkRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkRequest result =
                workRequestService.create(dto, "client@test.com");

        assertNotNull(result);
        assertEquals(clientOrg, result.getClientOrg());
        assertEquals(position, result.getPosition());
        assertEquals(LocalDate.of(2026, 10, 25), result.getWorkDate());
        assertEquals(LocalTime.of(10, 0), result.getStartTime());
        assertEquals(LocalTime.of(15, 0), result.getEndTime());
        assertEquals(new BigDecimal("20.00"), result.getHourlyRate());

        verify(workRequestRepository).save(any(WorkRequest.class));
    }

    @Test
    void create_shouldThrowException_whenOrganizationDoesNotExist() {
        CreateWorkRequestDto dto = new CreateWorkRequestDto();

        when(clientOrgRepository.findByUser_Email("unknown@test.com"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> workRequestService.create(dto, "unknown@test.com")
        );

        assertEquals("Организация не найдена", exception.getMessage());

        verify(workRequestRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowException_whenRateIsNotAssigned() {
        CreateWorkRequestDto dto = new CreateWorkRequestDto();

        clientOrg.setRate(null);

        when(clientOrgRepository.findByUser_Email("client@test.com"))
                .thenReturn(Optional.of(clientOrg));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> workRequestService.create(dto, "client@test.com")
        );

        assertTrue(exception.getMessage().contains("тариф"));

        verify(positionRepository, never()).findById(any());
        verify(workRequestRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowException_whenPositionDoesNotExist() {
        CreateWorkRequestDto dto = new CreateWorkRequestDto();
        dto.setPositionId(999L);

        when(clientOrgRepository.findByUser_Email("client@test.com"))
                .thenReturn(Optional.of(clientOrg));

        when(positionRepository.findById(999L))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> workRequestService.create(dto, "client@test.com")
        );

        assertEquals("Позиция не найдена", exception.getMessage());

        verify(workRequestRepository, never()).save(any());
    }

    @Test
    void getOpenAllRequestsForView_shouldCalculateHoursAndAmount() {
        request.setBids(new ArrayList<>());

        when(workRequestRepository.findAllByStatus(RequestStatus.OPEN))
                .thenReturn(List.of(request));

        List<WorkRequestViewDto> result =
                workRequestService.getOpenAllRequestsForView();

        assertEquals(1, result.size());

        WorkRequestViewDto dto = result.get(0);

        assertEquals(1L, dto.getId());
        assertEquals("Официант", dto.getPositionName());
        assertEquals(new BigDecimal("100.00"), dto.getAmount());
        assertEquals(RequestStatus.OPEN, dto.getStatus());
        assertEquals(0L, dto.getBidCount());

        verify(workRequestRepository).findAllByStatus(RequestStatus.OPEN);
    }

    @Test
    void getOpenAllRequestsForView_shouldReturnEmptyList_whenNoOpenRequests() {
        when(workRequestRepository.findAllByStatus(RequestStatus.OPEN))
                .thenReturn(List.of());

        List<WorkRequestViewDto> result =
                workRequestService.getOpenAllRequestsForView();

        assertTrue(result.isEmpty());
    }

    @Test
    void completeRequest_shouldChangeStatusToClosed_whenRequestIsAgreed() {
        request.setStatus(RequestStatus.AGREED);

        when(workRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(workRequestRepository.save(request))
                .thenReturn(request);

        workRequestService.completeRequest(1L);

        assertEquals(RequestStatus.CLOSED, request.getStatus());
        verify(workRequestRepository).save(request);
    }

    @Test
    void completeRequest_shouldThrowException_whenRequestDoesNotExist() {
        when(workRequestRepository.findById(999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workRequestService.completeRequest(999L)
        );

        assertEquals("Заявка не найдена", exception.getMessage());

        verify(workRequestRepository, never()).save(any());
    }

    @Test
    void completeRequest_shouldThrowException_whenStatusIsOpen() {
        request.setStatus(RequestStatus.OPEN);

        when(workRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> workRequestService.completeRequest(1L)
        );

        assertEquals(
                "Завершить можно только согласованную заявку",
                exception.getMessage()
        );

        verify(workRequestRepository, never()).save(any());
    }

    @Test
    void cancelRequest_shouldChangeStatusToCancelled_whenRequestIsOpen() {
        request.setStatus(RequestStatus.OPEN);

        when(workRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(workRequestRepository.save(request))
                .thenReturn(request);

        workRequestService.cancelRequest(1L);

        assertEquals(RequestStatus.CANCELLED, request.getStatus());
        verify(workRequestRepository).save(request);
    }

    @Test
    void cancelRequest_shouldChangeStatusToCancelled_whenRequestIsAgreed() {
        request.setStatus(RequestStatus.AGREED);

        when(workRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(workRequestRepository.save(request))
                .thenReturn(request);

        workRequestService.cancelRequest(1L);

        assertEquals(RequestStatus.CANCELLED, request.getStatus());
        verify(workRequestRepository).save(request);
    }

    @Test
    void cancelRequest_shouldThrowException_whenRequestDoesNotExist() {
        when(workRequestRepository.findById(999L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workRequestService.cancelRequest(999L)
        );

        assertEquals("Заявка не найдена", exception.getMessage());

        verify(workRequestRepository, never()).save(any());
    }

    @Test
    void cancelRequest_shouldThrowException_whenRequestIsClosed() {
        assertCannotCancel(RequestStatus.CLOSED);
    }

    @Test
    void cancelRequest_shouldThrowException_whenRequestIsExpired() {
        assertCannotCancel(RequestStatus.EXPIRED);
    }

    @Test
    void cancelRequest_shouldThrowException_whenRequestIsAlreadyCancelled() {
        assertCannotCancel(RequestStatus.CANCELLED);
    }

    private void assertCannotCancel(RequestStatus status) {
        request.setStatus(status);

        when(workRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> workRequestService.cancelRequest(1L)
        );

        assertEquals(
                "Эту заявку уже нельзя отменить",
                exception.getMessage()
        );

        verify(workRequestRepository, never()).save(any());
    }
}