package com.josequintero.taskflow.dto.admin;

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
public class AdminDashboardSummaryDto {

    private long totalUsuarios;
    private long usuariosActivos;
    private long usuariosInactivos;
    private long tareasActivas;
    private long tareasVencidas;
    private long recordatoriosFallidos;
}
