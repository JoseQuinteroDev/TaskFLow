package com.josequintero.taskflow.repositories;

import com.josequintero.taskflow.model.Tarea;
import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.model.enums.PrioridadTarea;
import com.josequintero.taskflow.repositories.custom.TareaRepositoryCustom;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
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
             AND t.fechaLimite < :referencia
             AND t.estado <> com.josequintero.taskflow.model.enums.EstadoTarea.COMPLETADA
           ORDER BY t.fechaLimite ASC
           """)
    List<Tarea> obtenerTareasVencidas(Long usuarioId, LocalDateTime referencia);

    @Query("""
           SELECT COUNT(t)
           FROM Tarea t
           WHERE t.usuario.id = :usuarioId
             AND t.fechaLimite IS NOT NULL
             AND t.fechaLimite < :referencia
             AND t.estado <> com.josequintero.taskflow.model.enums.EstadoTarea.COMPLETADA
           """)
    long contarTareasVencidas(Long usuarioId, LocalDateTime referencia);

    @Query("""
           SELECT t
           FROM Tarea t
           WHERE t.usuario.id = :usuarioId
             AND t.fechaLimite BETWEEN :desde AND :hasta
           ORDER BY t.prioridad DESC, t.fechaCreacion DESC
           """)
    List<Tarea> obtenerTareasPorFechaLimite(Long usuarioId, LocalDateTime desde, LocalDateTime hasta);

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

    List<Tarea> findByEstadoNotAndFechaLimiteBetweenOrderByFechaLimiteAsc(
            EstadoTarea estado,
            LocalDateTime desde,
            LocalDateTime hasta
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
           UPDATE Tarea t
           SET t.categoria = null
           WHERE t.usuario.id = :usuarioId
             AND t.categoria.id = :categoriaId
           """)
    int desvincularCategoria(Long usuarioId, Long categoriaId);
}
