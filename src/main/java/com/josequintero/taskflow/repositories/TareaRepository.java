package com.josequintero.taskflow.repositories;

import com.josequintero.taskflow.model.Tarea;
import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.model.enums.PrioridadTarea;
import com.josequintero.taskflow.repositories.custom.TareaRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TareaRepository extends JpaRepository<Tarea, Long>, TareaRepositoryCustom {

    List<Tarea> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    Optional<Tarea> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Tarea> findByUsuarioIdAndEstadoOrderByFechaCreacionDesc(Long usuarioId, EstadoTarea estado);

    List<Tarea> findByUsuarioIdAndPrioridadOrderByFechaCreacionDesc(Long usuarioId, PrioridadTarea prioridad);

    List<Tarea> findByUsuarioIdAndCategoriaIdOrderByFechaCreacionDesc(Long usuarioId, Long categoriaId);

    List<Tarea> findByUsuarioIdAndTituloContainingIgnoreCaseOrderByFechaCreacionDesc(Long usuarioId, String texto);

    long countByUsuarioId(Long usuarioId);

    long countByUsuarioIdAndEstado(Long usuarioId, EstadoTarea estado);

    @Query("""
           SELECT t
           FROM Tarea t
           WHERE t.usuario.id = :usuarioId
             AND t.fechaLimite IS NOT NULL
             AND t.fechaLimite < CURRENT_DATE
             AND t.estado <> com.josequintero.taskflow.model.enums.EstadoTarea.COMPLETADA
           ORDER BY t.fechaLimite ASC
           """)
    List<Tarea> obtenerTareasVencidas(Long usuarioId);

    @Query("""
           SELECT COUNT(t)
           FROM Tarea t
           WHERE t.usuario.id = :usuarioId
             AND t.fechaLimite IS NOT NULL
             AND t.fechaLimite < CURRENT_DATE
             AND t.estado <> com.josequintero.taskflow.model.enums.EstadoTarea.COMPLETADA
           """)
    long contarTareasVencidas(Long usuarioId);

    @Query("""
           SELECT t
           FROM Tarea t
           WHERE t.usuario.id = :usuarioId
             AND t.fechaLimite = :fecha
           ORDER BY t.prioridad DESC, t.fechaCreacion DESC
           """)
    List<Tarea> obtenerTareasPorFechaLimite(Long usuarioId, LocalDate fecha);

    @Query("""
           SELECT t
           FROM Tarea t
           WHERE t.usuario.id = :usuarioId
             AND (
                 LOWER(t.titulo) LIKE LOWER(CONCAT('%', :texto, '%'))
                 OR LOWER(t.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))
             )
           ORDER BY t.fechaCreacion DESC
           """)
    List<Tarea> buscarPorTextoEnTituloYDescripcion(Long usuarioId, String texto);
}