package com.josequintero.taskflow.mapper;

import com.josequintero.taskflow.dto.tarea.TareaCreateRequestDto;
import com.josequintero.taskflow.dto.tarea.TareaResponseDto;
import com.josequintero.taskflow.dto.tarea.TareaUpdateRequestDto;
import com.josequintero.taskflow.model.Categoria;
import com.josequintero.taskflow.model.Tarea;
import com.josequintero.taskflow.model.Usuario;
import com.josequintero.taskflow.model.enums.EstadoTarea;
import org.springframework.stereotype.Component;

@Component
public class TareaMapper {

    private final CategoriaMapper categoriaMapper; //necesario porque dentro de TareaResponseDto hay una categoría

    public TareaMapper(CategoriaMapper categoriaMapper) {
        this.categoriaMapper = categoriaMapper;
    }

    public Tarea toEntity(TareaCreateRequestDto dto, Usuario usuario, Categoria categoria) {
        if (dto == null) {
            return null;
        }

        return Tarea.builder()
                .titulo(dto.getTitulo())
                .descripcion(dto.getDescripcion())
                .prioridad(dto.getPrioridad())
                .fechaLimite(dto.getFechaLimite())
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
                .vencida(tarea.estaVencida())
                .categoria(categoriaMapper.toResumenDto(tarea.getCategoria()))
                .fechaCreacion(tarea.getFechaCreacion())
                .fechaActualizacion(tarea.getFechaActualizacion())
                .build();
    }

    public void updateEntity(Tarea tarea, TareaUpdateRequestDto dto, Categoria categoria) {
        if (tarea == null || dto == null) {
            return;
        }

        tarea.setTitulo(dto.getTitulo());
        tarea.setDescripcion(dto.getDescripcion());
        tarea.setPrioridad(dto.getPrioridad());
        tarea.setEstado(dto.getEstado());
        tarea.setFechaLimite(dto.getFechaLimite());
        tarea.setCategoria(categoria);
    }
}