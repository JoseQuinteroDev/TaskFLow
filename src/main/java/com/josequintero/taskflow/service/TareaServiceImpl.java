package com.josequintero.taskflow.service;

import com.josequintero.taskflow.dto.tarea.CambiarEstadoRequestDto;
import com.josequintero.taskflow.dto.tarea.TareaCreateRequestDto;
import com.josequintero.taskflow.dto.tarea.TareaFiltroRequestDto;
import com.josequintero.taskflow.dto.tarea.TareaResponseDto;
import com.josequintero.taskflow.dto.tarea.TareaResumenDto;
import com.josequintero.taskflow.dto.tarea.TareaUpdateRequestDto;
import com.josequintero.taskflow.exception.BusinessException;
import com.josequintero.taskflow.exception.ResourceNotFoundException;
import com.josequintero.taskflow.mapper.TareaMapper;
import com.josequintero.taskflow.model.Categoria;
import com.josequintero.taskflow.model.Tarea;
import com.josequintero.taskflow.model.Usuario;
import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.repositories.CategoriaRepository;
import com.josequintero.taskflow.repositories.TareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TareaServiceImpl implements TareaService {

    private final TareaRepository tareaRepository;
    private final CategoriaRepository categoriaRepository;
    private final TareaMapper tareaMapper;
    private final CurrentUserService currentUserService;
    private final TareaTemporalService tareaTemporalService;

    public TareaServiceImpl(
            TareaRepository tareaRepository,
            CategoriaRepository categoriaRepository,
            TareaMapper tareaMapper,
            CurrentUserService currentUserService,
            TareaTemporalService tareaTemporalService
    ) {
        this.tareaRepository = tareaRepository;
        this.categoriaRepository = categoriaRepository;
        this.tareaMapper = tareaMapper;
        this.currentUserService = currentUserService;
        this.tareaTemporalService = tareaTemporalService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TareaResponseDto> getAll() {
        Usuario currentUser = currentUserService.getCurrentUser();
        return tareaRepository.findByUsuarioIdOrderByFechaCreacionDesc(currentUser.getId())
                .stream()
                .map(tareaMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TareaResponseDto getById(Long id) {
        Usuario currentUser = currentUserService.getCurrentUser();
        return tareaMapper.toResponseDto(findOwnedTask(id, currentUser.getId()));
    }

    @Override
    @Transactional
    public TareaResponseDto create(TareaCreateRequestDto request) {
        Usuario currentUser = currentUserService.getCurrentUser();
        LocalDateTime fechaLimite = parseDeadlineForCreate(request.getFechaLimite());
        Categoria categoria = resolveCategory(request.getCategoriaId(), currentUser.getId());

        Tarea tarea = tareaMapper.toEntity(request, fechaLimite, currentUser, categoria);
        return tareaMapper.toResponseDto(tareaRepository.save(tarea));
    }

    @Override
    @Transactional
    public TareaResponseDto update(Long id, TareaUpdateRequestDto request) {
        Usuario currentUser = currentUserService.getCurrentUser();
        Tarea tarea = findOwnedTask(id, currentUser.getId());
        LocalDateTime fechaLimite = tareaTemporalService.parseFechaLimite(request.getFechaLimite());
        Categoria categoria = resolveCategory(request.getCategoriaId(), currentUser.getId());

        tareaMapper.updateEntity(tarea, request, fechaLimite, categoria);
        return tareaMapper.toResponseDto(tarea);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Usuario currentUser = currentUserService.getCurrentUser();
        tareaRepository.delete(findOwnedTask(id, currentUser.getId()));
    }

    @Override
    @Transactional
    public TareaResponseDto completar(Long id) {
        Usuario currentUser = currentUserService.getCurrentUser();
        Tarea tarea = findOwnedTask(id, currentUser.getId());
        tarea.setEstado(EstadoTarea.COMPLETADA);
        return tareaMapper.toResponseDto(tarea);
    }

    @Override
    @Transactional
    public TareaResponseDto cambiarEstado(Long id, CambiarEstadoRequestDto request) {
        Usuario currentUser = currentUserService.getCurrentUser();
        Tarea tarea = findOwnedTask(id, currentUser.getId());
        tarea.setEstado(request.getEstado());
        return tareaMapper.toResponseDto(tarea);
    }

    @Override
    @Transactional(readOnly = true)
    public TareaResumenDto getResumen() {
        Usuario currentUser = currentUserService.getCurrentUser();
        Long usuarioId = currentUser.getId();
        LocalDateTime ahora = tareaTemporalService.ahora();

        return TareaResumenDto.builder()
                .total(tareaRepository.countByUsuarioId(usuarioId))
                .pendientes(tareaRepository.countByUsuarioIdAndEstado(usuarioId, EstadoTarea.PENDIENTE))
                .enProceso(tareaRepository.countByUsuarioIdAndEstado(usuarioId, EstadoTarea.EN_PROCESO))
                .completadas(tareaRepository.countByUsuarioIdAndEstado(usuarioId, EstadoTarea.COMPLETADA))
                .vencidas(tareaRepository.contarTareasVencidas(usuarioId, ahora))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TareaResponseDto> filtrar(TareaFiltroRequestDto filtros) {
        Usuario currentUser = currentUserService.getCurrentUser();
        List<Tarea> tareas = tareaRepository.buscarConFiltros(
                currentUser.getId(),
                filtros.getTexto(),
                filtros.getEstado(),
                filtros.getPrioridad(),
                tareaTemporalService.parseFiltroDesde(filtros.getDesde()),
                tareaTemporalService.parseFiltroHasta(filtros.getHasta()),
                filtros.getCategoriaId()
        );

        return tareas.stream()
                .map(tareaMapper::toResponseDto)
                .toList();
    }

    private Tarea findOwnedTask(Long id, Long userId) {
        return tareaRepository.findByIdAndUsuarioId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
    }

    private Categoria resolveCategory(Long categoriaId, Long userId) {
        if (categoriaId == null) {
            return null;
        }

        return categoriaRepository.findByIdAndUsuarioId(categoriaId, userId)
                .orElseThrow(() -> new BusinessException("La categoría indicada no existe o no pertenece al usuario"));
    }

    private LocalDateTime parseDeadlineForCreate(String rawValue) {
        LocalDateTime fechaLimite = tareaTemporalService.parseFechaLimite(rawValue);

        if (fechaLimite != null && fechaLimite.isBefore(tareaTemporalService.ahora())) {
            throw new BusinessException("La fecha límite debe ser actual o futura");
        }

        return fechaLimite;
    }
}
