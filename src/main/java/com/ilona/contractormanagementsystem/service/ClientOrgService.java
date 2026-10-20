package com.ilona.contractormanagementsystem.service;

import com.ilona.contractormanagementsystem.dto.ClientOrgForm;
import com.ilona.contractormanagementsystem.enam.UserRole;
import com.ilona.contractormanagementsystem.entity.ClientOrg;
import com.ilona.contractormanagementsystem.entity.User;
import com.ilona.contractormanagementsystem.repository.ClientOrgRepository;
import com.ilona.contractormanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientOrgService {

    private final ClientOrgRepository clientOrgRepository;
    private final UserRepository userRepository;

    @Transactional
    public void becomeClient(String email, ClientOrgForm form) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Пользователь не найден"
                        )
                );

        if (user.getRole() != UserRole.CONTRACTOR) {
            throw new IllegalStateException(
                    "Стать заказчиком может только подрядчик"
            );
        }

        if (clientOrgRepository.existsByUser_Email(email)) {
            throw new IllegalStateException(
                    "У вас уже зарегистрирована организация"
            );
        }

        ClientOrg clientOrg = new ClientOrg();

        clientOrg.setName(form.getName().trim());
        clientOrg.setAddress(form.getAddress().trim());
        clientOrg.setLegalEntity(form.getLegalEntity().trim());

        clientOrg.setRate(null);


        clientOrg.setUser(user);

        clientOrgRepository.save(clientOrg);


        user.setRole(UserRole.CLIENT_ORG);
        userRepository.save(user);
    }
}
