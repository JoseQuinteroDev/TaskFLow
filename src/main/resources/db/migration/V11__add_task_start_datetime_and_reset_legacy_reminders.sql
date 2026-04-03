ALTER TABLE tareas
    ADD COLUMN fecha_inicio DATETIME NULL AFTER prioridad;

UPDATE tareas
SET fecha_inicio = COALESCE(fecha_limite, fecha_creacion)
WHERE fecha_inicio IS NULL;

ALTER TABLE tareas
    MODIFY COLUMN fecha_inicio DATETIME NOT NULL;

UPDATE tareas
SET recordatorio_activo = 0,
    recordatorio_minutos_antes = NULL
WHERE recordatorio_activo = 1
  AND recordatorio_minutos_antes IS NULL;

DELETE FROM recordatorios_tarea
WHERE estado <> 'ENVIADO';

CREATE INDEX idx_tareas_fecha_inicio ON tareas(fecha_inicio);
