package com.josequintero.taskflow.repositories;

import com.josequintero.taskflow.model.RecordatorioTarea;
import com.josequintero.taskflow.model.enums.CanalNotificacion;
import com.josequintero.taskflow.model.enums.TipoRecordatorioTarea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecordatorioTareaRepository extends JpaRepository<RecordatorioTarea, Long> {

    Optional<RecordatorioTarea> findByTareaIdAndTipoAndCanal(
            Long tareaId,
            TipoRecordatorioTarea tipo,
            CanalNotificacion canal
    );

    void deleteByTareaId(Long tareaId);
}
