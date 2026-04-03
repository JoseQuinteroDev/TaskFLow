package com.josequintero.taskflow.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTimezoneRequestDto {

    @NotBlank(message = "La zona horaria es obligatoria")
    @Size(max = 60, message = "La zona horaria no puede superar los 60 caracteres")
    private String timezone;
}
