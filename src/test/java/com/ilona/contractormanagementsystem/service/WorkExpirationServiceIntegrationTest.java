package com.ilona.contractormanagementsystem.service;

import com.ilona.contractormanagementsystem.entity.ClientOrg;
import com.ilona.contractormanagementsystem.entity.Position;
import com.ilona.contractormanagementsystem.entity.User;
import com.ilona.contractormanagementsystem.entity.WorkRequest;
import com.ilona.contractormanagementsystem.entity.WorkRequestStatus;
import com.ilona.contractormanagementsystem.repository.ClientOrgRepository;
import com.ilona.contractormanagementsystem.repository.PositionRepository;
import com.ilona.contractormanagementsystem.repository.UserRepository;
import com.ilona.contractormanagementsystem.repository.WorkRequestRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.profiles.active=test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WorkExpirationServiceIntegrationTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    @Autowired
    private RequestExpirationService expirationService;

    @Autowired
    private WorkRequestRepository requestRepository;

    @Autowired
    private ClientOrgRepository clientOrgRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void expireOldRequests_marksOldOpenAndAgreedRequestsAsExpired() {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate yesterday = today.minusDays(1);

        User user = new User(
                "expiration-test@example.com",
                "test-password"
        );
        user = userRepository.saveAndFlush(user);

        ClientOrg clientOrg = new ClientOrg();
        clientOrg.setName("Test Organization");
        clientOrg.setAddress("Test Address");
        clientOrg.setLegalEntity("Test Legal Entity");
        clientOrg.setRate(new BigDecimal("100.00"));
        clientOrg.setUser(user);
        clientOrg = clientOrgRepository.saveAndFlush(clientOrg);

        Position position = new Position();
        position.setName("Test Position");
        position.setActive(true);
        position = positionRepository.saveAndFlush(position);

        WorkRequest oldOpen = createRequest(
                yesterday,
                WorkRequestStatus.OPEN,
                clientOrg,
                position
        );
        oldOpen = requestRepository.saveAndFlush(oldOpen);
        Long oldOpenId = oldOpen.getId();

        WorkRequest oldAgreed = createRequest(
                yesterday,
                WorkRequestStatus.AGREED,
                clientOrg,
                position
        );
        oldAgreed = requestRepository.saveAndFlush(oldAgreed);
        Long oldAgreedId = oldAgreed.getId();

        // Заявка на сегодня должна остаться открытой.
        WorkRequest todayOpen = createRequest(
                today,
                WorkRequestStatus.OPEN,
                clientOrg,
                position
        );
        todayOpen = requestRepository.saveAndFlush(todayOpen);
        Long todayOpenId = todayOpen.getId();

        // Сбрасываем persistence context, чтобы читать актуальные данные из БД.
        entityManager.clear();

        expirationService.expireOldRequests();

        // Сбрасываем persistence context после массового UPDATE.
        entityManager.clear();

        assertThat(requestRepository.findById(oldOpenId))
                .isPresent()
                .get()
                .extracting(WorkRequest::getStatus)
                .isEqualTo(WorkRequestStatus.EXPIRED);

        assertThat(requestRepository.findById(oldAgreedId))
                .isPresent()
                .get()
                .extracting(WorkRequest::getStatus)
                .isEqualTo(WorkRequestStatus.EXPIRED);

        assertThat(requestRepository.findById(todayOpenId))
                .isPresent()
                .get()
                .extracting(WorkRequest::getStatus)
                .isEqualTo(WorkRequestStatus.OPEN);
    }

    private WorkRequest createRequest(
            LocalDate workDate,
            WorkRequestStatus status,
            ClientOrg clientOrg,
            Position position
    ) {
        WorkRequest request = new WorkRequest();
        request.setClientOrg(clientOrg);
        request.setPosition(position);
        request.setHourlyRate(new BigDecimal("100.00"));
        request.setWorkDate(workDate);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(18, 0));
        request.setStatus(status);

        return request;
    }
}