package com.josequintero.taskflow.repositories;

import com.josequintero.taskflow.model.RecordatorioTarea;
import com.josequintero.taskflow.model.enums.CanalNotificacion;
import com.josequintero.taskflow.model.enums.EstadoEnvioNotificacion;
import com.josequintero.taskflow.model.enums.TipoRecordatorioTarea;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RecordatorioTareaRepository extends JpaRepository<RecordatorioTarea, Long> {

    Optional<RecordatorioTarea> findByTareaIdAndTipoAndCanal(
            Long tareaId,
            TipoRecordatorioTarea tipo,
            CanalNotificacion canal
    );

    void deleteByTareaId(Long tareaId);

    long countByEstado(EstadoEnvioNotificacion estado);

    @EntityGraph(attributePaths = {"tarea", "tarea.usuario"})
    List<RecordatorioTarea> findByEstadoOrderByFechaProgramadaDesc(
            EstadoEnvioNotificacion estado,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           DELETE FROM RecordatorioTarea r
           WHERE r.tarea.usuario.id = :usuarioId
           """)
    int deleteByUsuarioId(Long usuarioId);
}
