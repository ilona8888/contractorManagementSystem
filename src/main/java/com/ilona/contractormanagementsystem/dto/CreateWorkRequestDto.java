package com.ilona.contractormanagementsystem.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateWorkRequestDto {

    @NotNull(message = "Выберите должность")
    private Long positionId;

    @FutureOrPresent(message = "Дата работы не может быть в прошлом")
    @NotNull(message = "Укажите дату работы")
    private LocalDate workDate;

    @NotNull(message = "Укажите время начала работы")
    private LocalTime startTime;

    @NotNull(message = "Укажите время окончания работы")
    private LocalTime endTime;
}
