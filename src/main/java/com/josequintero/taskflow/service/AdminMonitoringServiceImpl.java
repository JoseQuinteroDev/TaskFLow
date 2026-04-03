package com.josequintero.taskflow.service;

import com.josequintero.taskflow.dto.admin.AdminDashboardSummaryDto;
import com.josequintero.taskflow.dto.admin.AdminReminderFailureResponseDto;
import com.josequintero.taskflow.mapper.AdminReminderFailureMapper;
import com.josequintero.taskflow.model.enums.EstadoEnvioNotificacion;
import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.repositories.RecordatorioTareaRepository;
import com.josequintero.taskflow.repositories.TareaRepository;
import com.josequintero.taskflow.repositories.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminMonitoringServiceImpl implements AdminMonitoringService {

    private final UsuarioRepository usuarioRepository;
    private final TareaRepository tareaRepository;
    private final RecordatorioTareaRepository recordatorioTareaRepository;
    private final TareaTemporalService tareaTemporalService;
    private final AdminReminderFailureMapper adminReminderFailureMapper;

    public AdminMonitoringServiceImpl(
            UsuarioRepository usuarioRepository,
            TareaRepository tareaRepository,
            RecordatorioTareaRepository recordatorioTareaRepository,
            TareaTemporalService tareaTemporalService,
            AdminReminderFailureMapper adminReminderFailureMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.tareaRepository = tareaRepository;
        this.recordatorioTareaRepository = recordatorioTareaRepository;
        this.tareaTemporalService = tareaTemporalService;
        this.adminReminderFailureMapper = adminReminderFailureMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardSummaryDto getDashboardSummary() {
        return AdminDashboardSummaryDto.builder()
                .totalUsuarios(usuarioRepository.count())
                .usuariosActivos(usuarioRepository.countByActivoTrue())
                .usuariosInactivos(usuarioRepository.countByActivoFalse())
                .tareasActivas(tareaRepository.countByEstadoNot(EstadoTarea.COMPLETADA))
                .tareasVencidas(tareaRepository.contarTareasVencidasGlobal(tareaTemporalService.ahora()))
                .recordatoriosFallidos(recordatorioTareaRepository.countByEstado(EstadoEnvioNotificacion.FALLIDO))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminReminderFailureResponseDto> getRecentReminderFailures(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));

        return recordatorioTareaRepository.findByEstadoOrderByFechaProgramadaDesc(
                        EstadoEnvioNotificacion.FALLIDO,
                        PageRequest.of(0, safeLimit)
                ).stream()
                .map(adminReminderFailureMapper::toResponseDto)
                .toList();
    }
}
