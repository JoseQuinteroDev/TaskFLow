package com.josequintero.taskflow.service;

import com.josequintero.taskflow.dto.categoria.CategoriaRequestDto;
import com.josequintero.taskflow.dto.categoria.CategoriaResponseDto;

import java.util.List;

public interface CategoriaService {

    List<CategoriaResponseDto> getAll();

    CategoriaResponseDto create(CategoriaRequestDto request);

    CategoriaResponseDto update(Long id, CategoriaRequestDto request);

    void delete(Long id);
}
