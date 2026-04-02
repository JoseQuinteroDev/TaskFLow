package com.josequintero.taskflow.service;

import com.josequintero.taskflow.dto.tarea.CambiarEstadoRequestDto;
import com.josequintero.taskflow.dto.tarea.TareaCreateRequestDto;
import com.josequintero.taskflow.dto.tarea.TareaFiltroRequestDto;
import com.josequintero.taskflow.dto.tarea.TareaResponseDto;
import com.josequintero.taskflow.dto.tarea.TareaResumenDto;
import com.josequintero.taskflow.dto.tarea.TareaUpdateRequestDto;

import java.util.List;

public interface TareaService {

    List<TareaResponseDto> getAll();

    TareaResponseDto getById(Long id);

    TareaResponseDto create(TareaCreateRequestDto request);

    TareaResponseDto update(Long id, TareaUpdateRequestDto request);

    void delete(Long id);

    TareaResponseDto completar(Long id);

    TareaResponseDto cambiarEstado(Long id, CambiarEstadoRequestDto request);

    TareaResumenDto getResumen();

    List<TareaResponseDto> filtrar(TareaFiltroRequestDto filtros);
}
