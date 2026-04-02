package com.josequintero.taskflow.repositories.custom;

import com.josequintero.taskflow.model.Tarea;
import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.model.enums.PrioridadTarea;

import java.time.Instant;
import java.util.List;

public interface TareaRepositoryCustom {

    List<Tarea> buscarConFiltros(
            Long usuarioId,
            String texto,
            EstadoTarea estado,
            PrioridadTarea prioridad,
            Instant fechaDesde,
            Instant fechaHasta,
            Long categoriaId
    );
}
