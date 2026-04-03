package com.josequintero.taskflow.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponseDto {

    private Long id;
    private String nombre;
    private String email;
    private Boolean activo;
    private String timezone;
    private Set<String> roles;
    private boolean admin;
    private LocalDateTime fechaCreacion;
}
