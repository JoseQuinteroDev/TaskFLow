package com.josequintero.taskflow.service;

import com.josequintero.taskflow.dto.categoria.CategoriaRequestDto;
import com.josequintero.taskflow.dto.categoria.CategoriaResponseDto;
import com.josequintero.taskflow.exception.BusinessException;
import com.josequintero.taskflow.exception.ResourceNotFoundException;
import com.josequintero.taskflow.mapper.CategoriaMapper;
import com.josequintero.taskflow.model.Categoria;
import com.josequintero.taskflow.model.Usuario;
import com.josequintero.taskflow.repositories.CategoriaRepository;
import com.josequintero.taskflow.repositories.TareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final TareaRepository tareaRepository;
    private final CategoriaMapper categoriaMapper;
    private final CurrentUserService currentUserService;

    public CategoriaServiceImpl(
            CategoriaRepository categoriaRepository,
            TareaRepository tareaRepository,
            CategoriaMapper categoriaMapper,
            CurrentUserService currentUserService
    ) {
        this.categoriaRepository = categoriaRepository;
        this.tareaRepository = tareaRepository;
        this.categoriaMapper = categoriaMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDto> getAll() {
        Usuario currentUser = currentUserService.getCurrentUser();
        return categoriaRepository.findByUsuarioIdOrderByNombreAsc(currentUser.getId())
                .stream()
                .map(categoriaMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public CategoriaResponseDto create(CategoriaRequestDto request) {
        Usuario currentUser = currentUserService.getCurrentUser();

        if (categoriaRepository.existsByUsuarioIdAndNombreIgnoreCase(currentUser.getId(), request.getNombre())) {
            throw new BusinessException("Ya existe una categoría con ese nombre");
        }

        Categoria categoria = categoriaMapper.toEntity(request, currentUser);
        return categoriaMapper.toResponseDto(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public CategoriaResponseDto update(Long id, CategoriaRequestDto request) {
        Usuario currentUser = currentUserService.getCurrentUser();
        Categoria categoria = categoriaRepository.findByIdAndUsuarioId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        if (categoriaRepository.existsByUsuarioIdAndNombreIgnoreCaseAndIdNot(currentUser.getId(), request.getNombre(), id)) {
            throw new BusinessException("Ya existe otra categoría con ese nombre");
        }

        categoriaMapper.updateEntity(categoria, request);
        return categoriaMapper.toResponseDto(categoria);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Usuario currentUser = currentUserService.getCurrentUser();
        Categoria categoria = categoriaRepository.findByIdAndUsuarioId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        tareaRepository.desvincularCategoria(currentUser.getId(), categoria.getId());
        categoriaRepository.delete(categoria);
    }
}
