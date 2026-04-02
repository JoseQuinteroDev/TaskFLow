package com.josequintero.taskflow.mapper;

import com.josequintero.taskflow.dto.tarea.TareaCreateRequestDto;
import com.josequintero.taskflow.dto.tarea.TareaResponseDto;
import com.josequintero.taskflow.dto.tarea.TareaUpdateRequestDto;
import com.josequintero.taskflow.model.Categoria;
import com.josequintero.taskflow.model.Tarea;
import com.josequintero.taskflow.model.Usuario;
import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.service.TareaTemporalService;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TareaMapper {

    private final CategoriaMapper categoriaMapper;
    private final TareaTemporalService tareaTemporalService;

    public TareaMapper(CategoriaMapper categoriaMapper, TareaTemporalService tareaTemporalService) {
        this.categoriaMapper = categoriaMapper;
        this.tareaTemporalService = tareaTemporalService;
    }

    public Tarea toEntity(
            TareaCreateRequestDto dto,
            Instant fechaLimite,
            Usuario usuario,
            Categoria categoria
    ) {
        if (dto == null) {
            return null;
        }

        return Tarea.builder()
                .titulo(dto.getTitulo().trim())
                .descripcion(dto.getDescripcion() == null ? null : dto.getDescripcion().trim())
                .prioridad(dto.getPrioridad())
                .fechaLimite(fechaLimite)
                .recordatorioActivo(Boolean.TRUE.equals(dto.getRecordatorioActivo()))
                .recordatorioMinutosAntes(Boolean.TRUE.equals(dto.getRecordatorioActivo()) ? dto.getRecordatorioMinutosAntes() : null)
                .estado(EstadoTarea.PENDIENTE)
                .usuario(usuario)
                .categoria(categoria)
                .build();
    }

    public TareaResponseDto toResponseDto(Tarea tarea) {
        if (tarea == null) {
            return null;
        }

        return TareaResponseDto.builder()
                .id(tarea.getId())
                .titulo(tarea.getTitulo())
                .descripcion(tarea.getDescripcion())
                .prioridad(tarea.getPrioridad())
                .estado(tarea.getEstado())
                .fechaLimite(tarea.getFechaLimite())
                .vencida(tarea.estaVencida(tareaTemporalService.ahora()))
                .completada(tarea.estaCompletada())
                .recordatorioActivo(Boolean.TRUE.equals(tarea.getRecordatorioActivo()))
                .recordatorioMinutosAntes(tarea.getRecordatorioMinutosAntes())
                .categoria(categoriaMapper.toResumenDto(tarea.getCategoria()))
                .fechaCreacion(tarea.getFechaCreacion())
                .fechaActualizacion(tarea.getFechaActualizacion())
                .build();
    }

    public void updateEntity(
            Tarea tarea,
            TareaUpdateRequestDto dto,
            Instant fechaLimite,
            Categoria categoria
    ) {
        if (tarea == null || dto == null) {
            return;
        }

        tarea.setTitulo(dto.getTitulo().trim());
        tarea.setDescripcion(dto.getDescripcion() == null ? null : dto.getDescripcion().trim());
        tarea.setPrioridad(dto.getPrioridad());
        tarea.setEstado(dto.getEstado());
        tarea.setFechaLimite(fechaLimite);
        tarea.setRecordatorioActivo(Boolean.TRUE.equals(dto.getRecordatorioActivo()));
        tarea.setRecordatorioMinutosAntes(Boolean.TRUE.equals(dto.getRecordatorioActivo()) ? dto.getRecordatorioMinutosAntes() : null);
        tarea.setCategoria(categoria);
    }
}
