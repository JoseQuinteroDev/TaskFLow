package com.josequintero.taskflow.mapper;

import com.josequintero.taskflow.dto.categoria.CategoriaRequestDto;
import com.josequintero.taskflow.dto.categoria.CategoriaResponseDto;
import com.josequintero.taskflow.dto.categoria.CategoriaResumenDto;
import com.josequintero.taskflow.model.Categoria;
import com.josequintero.taskflow.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public Categoria toEntity(CategoriaRequestDto dto, Usuario usuario) {
        if (dto == null) {
            return null;
        }

        return Categoria.builder()
                .nombre(dto.getNombre().trim())
                .color(dto.getColor())
                .usuario(usuario)
                .build();
    }

    public CategoriaResponseDto toResponseDto(Categoria categoria) {
        if (categoria == null) {
            return null;
        }

        return CategoriaResponseDto.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .color(categoria.getColor())
                .build();
    }

    public CategoriaResumenDto toResumenDto(Categoria categoria) {
        if (categoria == null) {
            return null;
        }

        return CategoriaResumenDto.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .color(categoria.getColor())
                .build();
    }

    public void updateEntity(Categoria categoria, CategoriaRequestDto dto) {
        if (categoria == null || dto == null) {
            return;
        }

        categoria.setNombre(dto.getNombre().trim());
        categoria.setColor(dto.getColor());
    }
}
