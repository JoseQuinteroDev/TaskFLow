CREATE TABLE tareas (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        titulo VARCHAR(100) NOT NULL,
                        descripcion VARCHAR(1000) NULL,
                        estado VARCHAR(20) NOT NULL,
                        prioridad VARCHAR(20) NOT NULL,
                        fecha_limite DATE NULL,
                        fecha_creacion DATETIME NOT NULL,
                        fecha_actualizacion DATETIME NOT NULL,
                        usuario_id BIGINT NOT NULL,
                        categoria_id BIGINT NULL,
                        CONSTRAINT pk_tareas PRIMARY KEY (id),
                        CONSTRAINT fk_tareas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
                        CONSTRAINT fk_tareas_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);

CREATE INDEX idx_tareas_usuario ON tareas(usuario_id);
CREATE INDEX idx_tareas_usuario_estado ON tareas(usuario_id, estado);
CREATE INDEX idx_tareas_usuario_prioridad ON tareas(usuario_id, prioridad);
CREATE INDEX idx_tareas_fecha_limite ON tareas(fecha_limite);