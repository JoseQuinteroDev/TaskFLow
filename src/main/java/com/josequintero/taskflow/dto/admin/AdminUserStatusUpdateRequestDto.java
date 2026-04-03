package com.josequintero.taskflow.dto.admin;

import jakarta.validation.constraints.NotNull;
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
public class AdminUserStatusUpdateRequestDto {

    @NotNull(message = "Debes indicar si la cuenta queda activa o inactiva")
    private Boolean activo;
}
