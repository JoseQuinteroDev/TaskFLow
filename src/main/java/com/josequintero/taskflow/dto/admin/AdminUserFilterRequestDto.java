package com.josequintero.taskflow.dto.admin;

import com.josequintero.taskflow.model.enums.NombreRol;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class AdminUserFilterRequestDto {

    @Size(max = 120, message = "El filtro de correo electrónico no puede superar los 120 caracteres")
    private String email;

    @Size(max = 100, message = "El filtro de nombre no puede superar los 100 caracteres")
    private String nombre;

    private NombreRol rol;

    private Boolean activo;

    @Builder.Default
    @Min(value = 0, message = "La página no puede ser negativa")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "El tamaño mínimo de página es 1")
    @Max(value = 50, message = "El tamaño máximo de página es 50")
    private Integer size = 10;
}
