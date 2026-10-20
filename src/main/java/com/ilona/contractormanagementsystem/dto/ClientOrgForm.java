package com.ilona.contractormanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientOrgForm {

    @NotBlank(message = "Введите название организации")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String name;

    @NotBlank(message = "Введите адрес организации")
    @Size(max = 255, message = "Адрес не должен превышать 255 символов")
    private String address;

    @NotBlank(message = "Введите юридическое лицо")
    @Size(max = 255, message = "Поле не должно превышать 255 символов")
    private String legalEntity;
}

