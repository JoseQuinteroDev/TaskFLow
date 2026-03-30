package com.josequintero.taskflow.repositories.impl;

import com.josequintero.taskflow.model.Tarea;
import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.model.enums.PrioridadTarea;
import com.josequintero.taskflow.repositories.custom.TareaRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TareaRepositoryImpl implements TareaRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Tarea> buscarConFiltros(
            Long usuarioId,
            String texto,
            EstadoTarea estado,
            PrioridadTarea prioridad,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Long categoriaId
    ) {
        StringBuilder jpql = new StringBuilder("""
                SELECT t
                FROM Tarea t
                WHERE t.usuario.id = :usuarioId
                """);

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("usuarioId", usuarioId);

        if (StringUtils.hasText(texto)) {
            jpql.append("""
                    
                    AND (
                        LOWER(t.titulo) LIKE LOWER(CONCAT('%', :texto, '%'))
                        OR LOWER(t.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))
                    )
                    """);
            parametros.put("texto", texto);
        }

        if (estado != null) {
            jpql.append(" AND t.estado = :estado");
            parametros.put("estado", estado);
        }

        if (prioridad != null) {
            jpql.append(" AND t.prioridad = :prioridad");
            parametros.put("prioridad", prioridad);
        }

        if (fechaDesde != null) {
            jpql.append(" AND t.fechaLimite >= :fechaDesde");
            parametros.put("fechaDesde", fechaDesde);
        }

        if (fechaHasta != null) {
            jpql.append(" AND t.fechaLimite <= :fechaHasta");
            parametros.put("fechaHasta", fechaHasta);
        }

        if (categoriaId != null) {
            jpql.append(" AND t.categoria.id = :categoriaId");
            parametros.put("categoriaId", categoriaId);
        }

        jpql.append(" ORDER BY t.fechaCreacion DESC");

        TypedQuery<Tarea> query = entityManager.createQuery(jpql.toString(), Tarea.class);
        parametros.forEach(query::setParameter);

        return query.getResultList();
    }
}