package com.ilona.contractormanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {

    @NotBlank(message = "Введите email")
    @Email(message = "Введите корректный email")
    @Size(max = 254, message = "Email слишком длинный")
    private String email;

    @NotBlank(message = "Введите пароль")
    @Size(min = 8, max = 72,
            message = "Пароль должен содержать от 8 до 72 символов")
    private String password;

    @NotBlank(message = "Повторите пароль")
    private String confirmPassword;

}
