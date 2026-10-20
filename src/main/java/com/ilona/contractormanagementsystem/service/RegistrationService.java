package com.ilona.contractormanagementsystem.service;

import com.ilona.contractormanagementsystem.dto.UserDto;
import com.ilona.contractormanagementsystem.enam.UserRole;
import com.ilona.contractormanagementsystem.entity.Contractor;
import com.ilona.contractormanagementsystem.entity.User;
import com.ilona.contractormanagementsystem.repository.ContractorRepository;
import com.ilona.contractormanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ContractorRepository contractorRepository;

    @Transactional
    public void register(UserDto userDto) {
        String email = userDto.getEmail().trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Этот логин уже занят");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Этот email уже зарегистрирован");
        }

        if (!userDto.getPassword()
                .equals(userDto.getConfirmPassword())) {
            throw new IllegalArgumentException(
                    "Пароли не совпадают");
        }

        User user = new User(
                email,
                passwordEncoder.encode(userDto.getPassword())
        );


        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == UserRole.CONTRACTOR) {
            Contractor contractor = new Contractor();
            contractor.setUser(savedUser);
            contractorRepository.save(contractor);
        }
    }
}
