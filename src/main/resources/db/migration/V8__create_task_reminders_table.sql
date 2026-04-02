CREATE TABLE recordatorios_tarea (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tarea_id BIGINT NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    canal VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    destinatario VARCHAR(120) NOT NULL,
    fecha_programada DATETIME NOT NULL,
    fecha_envio DATETIME NULL,
    fecha_creacion DATETIME NOT NULL,
    error VARCHAR(500) NULL,
    CONSTRAINT pk_recordatorios_tarea PRIMARY KEY (id),
    CONSTRAINT fk_recordatorios_tarea_tarea FOREIGN KEY (tarea_id) REFERENCES tareas(id),
    CONSTRAINT uk_recordatorio_tarea_tipo_canal UNIQUE (tarea_id, tipo, canal)
);

CREATE INDEX idx_recordatorio_estado ON recordatorios_tarea(estado);
CREATE INDEX idx_recordatorio_tarea ON recordatorios_tarea(tarea_id);
