package com.josequintero.taskflow.model;

import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.model.enums.PrioridadTarea;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "tareas",
        indexes = {
                @Index(name = "idx_tareas_usuario", columnList = "usuario_id"),
                @Index(name = "idx_tareas_usuario_estado", columnList = "usuario_id, estado"),
                @Index(name = "idx_tareas_usuario_prioridad", columnList = "usuario_id, prioridad"),
                @Index(name = "idx_tareas_fecha_limite", columnList = "fecha_limite")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoTarea estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 20)
    private PrioridadTarea prioridad;

    @Column(name = "fecha_limite")
    private Instant fechaLimite;

    @Column(name = "recordatorio_activo", nullable = false)
    private Boolean recordatorioActivo;

    @Column(name = "recordatorio_minutos_antes")
    private Integer recordatorioMinutosAntes;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = EstadoTarea.PENDIENTE;
        }
        if (prioridad == null) {
            prioridad = PrioridadTarea.MEDIA;
        }
        if (recordatorioActivo == null) {
            recordatorioActivo = false;
        }
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public boolean estaCompletada() {
        return estado == EstadoTarea.COMPLETADA;
    }

    public boolean tieneRecordatorioActivo() {
        return Boolean.TRUE.equals(recordatorioActivo) && recordatorioMinutosAntes != null;
    }

    public boolean estaVencida(Instant referencia) {
        return fechaLimite != null
                && referencia != null
                && fechaLimite.isBefore(referencia)
                && !estaCompletada();
    }
}
