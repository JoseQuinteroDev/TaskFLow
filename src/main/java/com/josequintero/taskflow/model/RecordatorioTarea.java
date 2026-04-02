package com.josequintero.taskflow.model;

import com.josequintero.taskflow.model.enums.CanalNotificacion;
import com.josequintero.taskflow.model.enums.EstadoEnvioNotificacion;
import com.josequintero.taskflow.model.enums.TipoRecordatorioTarea;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "recordatorios_tarea",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recordatorio_tarea_tipo_canal",
                        columnNames = {"tarea_id", "tipo", "canal"}
                )
        },
        indexes = {
                @Index(name = "idx_recordatorio_estado", columnList = "estado"),
                @Index(name = "idx_recordatorio_tarea", columnList = "tarea_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordatorioTarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tarea_id", nullable = false)
    private Tarea tarea;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 40)
    private TipoRecordatorioTarea tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 20)
    private CanalNotificacion canal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoEnvioNotificacion estado;

    @Column(name = "destinatario", nullable = false, length = 120)
    private String destinatario;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDateTime fechaProgramada;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "error", length = 500)
    private String error;

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = EstadoEnvioNotificacion.PENDIENTE;
        }
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
