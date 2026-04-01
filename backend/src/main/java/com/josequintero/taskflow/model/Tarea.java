package com.josequintero.taskflow.model;

import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.model.enums.PrioridadTarea;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "tareas",
        indexes = {
                @Index(name = "idx_tareas_usuario", columnList = "usuario_id"),
                @Index(name = "idx_tareas_usuario_estado", columnList = "usuario_id, estado"),
                @Index(name = "idx_tareas_usuario_prioridad", columnList = "usuario_id, prioridad"),
                @Index(name = "idx_tareas_fecha_limite", columnList = "fecha_limite"),
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
    private LocalDate fechaLimite;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Tarea.java
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
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate()
    {
        fechaActualizacion = LocalDateTime.now();
    }

    @Transient
    public boolean estaVencida()
    {
        return fechaLimite != null
                && fechaLimite.isBefore(LocalDate.now())
                && estado != EstadoTarea.COMPLETADA;
    }

}
