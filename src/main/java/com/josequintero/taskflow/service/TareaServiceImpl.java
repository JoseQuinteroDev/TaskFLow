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
import com.josequintero.taskflow.repositories.RecordatorioTareaRepository;
import com.josequintero.taskflow.repositories.TareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.List;

@Service
public class TareaServiceImpl implements TareaService {

    private final TareaRepository tareaRepository;
    private final CategoriaRepository categoriaRepository;
    private final RecordatorioTareaRepository recordatorioTareaRepository;
    private final TareaMapper tareaMapper;
    private final CurrentUserService currentUserService;
    private final TareaTemporalService tareaTemporalService;

    public TareaServiceImpl(
            TareaRepository tareaRepository,
            CategoriaRepository categoriaRepository,
            RecordatorioTareaRepository recordatorioTareaRepository,
            TareaMapper tareaMapper,
            CurrentUserService currentUserService,
            TareaTemporalService tareaTemporalService
    ) {
        this.tareaRepository = tareaRepository;
        this.categoriaRepository = categoriaRepository;
        this.recordatorioTareaRepository = recordatorioTareaRepository;
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
        Instant fechaLimite = parseDeadlineForCreate(request.getFechaLimite(), currentUser.getTimezone());
        validateReminderConfig(request.getRecordatorioActivo(), request.getRecordatorioMinutosAntes(), fechaLimite);
        Categoria categoria = resolveCategory(request.getCategoriaId(), currentUser.getId());

        Tarea tarea = tareaMapper.toEntity(request, fechaLimite, currentUser, categoria);
        return tareaMapper.toResponseDto(tareaRepository.save(tarea));
    }

    @Override
    @Transactional
    public TareaResponseDto update(Long id, TareaUpdateRequestDto request) {
        Usuario currentUser = currentUserService.getCurrentUser();
        Tarea tarea = findOwnedTask(id, currentUser.getId());
        Instant fechaLimite = tareaTemporalService.parseFechaLimite(request.getFechaLimite(), currentUser.getTimezone());
        validateReminderConfig(request.getRecordatorioActivo(), request.getRecordatorioMinutosAntes(), fechaLimite);
        Categoria categoria = resolveCategory(request.getCategoriaId(), currentUser.getId());
        Instant previousDeadline = tarea.getFechaLimite();
        Boolean previousReminderActive = tarea.getRecordatorioActivo();
        Integer previousReminderMinutes = tarea.getRecordatorioMinutosAntes();
        EstadoTarea previousEstado = tarea.getEstado();

        tareaMapper.updateEntity(tarea, request, fechaLimite, categoria);

        if (reminderStateRequiresReset(
                previousDeadline,
                previousReminderActive,
                previousReminderMinutes,
                previousEstado,
                tarea
        )) {
            recordatorioTareaRepository.deleteByTareaId(tarea.getId());
        }

        return tareaMapper.toResponseDto(tarea);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Usuario currentUser = currentUserService.getCurrentUser();
        Tarea tarea = findOwnedTask(id, currentUser.getId());
        recordatorioTareaRepository.deleteByTareaId(tarea.getId());
        tareaRepository.delete(tarea);
    }

    @Override
    @Transactional
    public TareaResponseDto completar(Long id) {
        Usuario currentUser = currentUserService.getCurrentUser();
        Tarea tarea = findOwnedTask(id, currentUser.getId());
        tarea.setEstado(EstadoTarea.COMPLETADA);
        recordatorioTareaRepository.deleteByTareaId(tarea.getId());
        return tareaMapper.toResponseDto(tarea);
    }

    @Override
    @Transactional
    public TareaResponseDto cambiarEstado(Long id, CambiarEstadoRequestDto request) {
        Usuario currentUser = currentUserService.getCurrentUser();
        Tarea tarea = findOwnedTask(id, currentUser.getId());
        EstadoTarea previousEstado = tarea.getEstado();
        tarea.setEstado(request.getEstado());

        if (previousEstado != request.getEstado()) {
            recordatorioTareaRepository.deleteByTareaId(tarea.getId());
        }

        return tareaMapper.toResponseDto(tarea);
    }

    @Override
    @Transactional(readOnly = true)
    public TareaResumenDto getResumen() {
        Usuario currentUser = currentUserService.getCurrentUser();
        Long usuarioId = currentUser.getId();
        Instant ahora = tareaTemporalService.ahora();

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
                tareaTemporalService.parseFiltroDesde(filtros.getDesde(), currentUser.getTimezone()),
                tareaTemporalService.parseFiltroHasta(filtros.getHasta(), currentUser.getTimezone()),
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
                .orElseThrow(() -> new BusinessException("La categoria indicada no existe o no pertenece al usuario"));
    }

    private Instant parseDeadlineForCreate(String rawValue, String timezone) {
        Instant fechaLimite = tareaTemporalService.parseFechaLimite(rawValue, timezone);

        if (fechaLimite != null && fechaLimite.isBefore(tareaTemporalService.ahora())) {
            throw new BusinessException("La fecha limite debe ser actual o futura");
        }

        return fechaLimite;
    }

    private void validateReminderConfig(Boolean recordatorioActivo, Integer recordatorioMinutosAntes, Instant fechaLimite) {
        if (!Boolean.TRUE.equals(recordatorioActivo)) {
            return;
        }

        if (fechaLimite == null) {
            throw new BusinessException("Debes indicar una fecha limite para activar un recordatorio");
        }

        if (recordatorioMinutosAntes == null) {
            throw new BusinessException("Indica cuantos minutos antes quieres recibir el recordatorio");
        }

        if (recordatorioMinutosAntes < 5 || recordatorioMinutosAntes > 10080) {
            throw new BusinessException("El recordatorio debe estar entre 5 y 10080 minutos antes");
        }
    }

    private boolean reminderStateRequiresReset(
            Instant previousDeadline,
            Boolean previousReminderActive,
            Integer previousReminderMinutes,
            EstadoTarea previousEstado,
            Tarea tarea
    ) {
        return !Objects.equals(previousDeadline, tarea.getFechaLimite())
                || !Objects.equals(previousReminderActive, tarea.getRecordatorioActivo())
                || !Objects.equals(previousReminderMinutes, tarea.getRecordatorioMinutosAntes())
                || previousEstado != tarea.getEstado();
    }
}
