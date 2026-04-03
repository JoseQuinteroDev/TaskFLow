UPDATE usuarios
SET timezone = 'Europe/Madrid'
WHERE timezone IS NULL
   OR TRIM(timezone) = '';

UPDATE tareas
SET recordatorio_activo = 0
WHERE recordatorio_activo IS NULL;

UPDATE tareas
SET recordatorio_minutos_antes = NULL
WHERE recordatorio_activo = 0;

ALTER TABLE usuarios
    MODIFY COLUMN timezone VARCHAR(60) NOT NULL DEFAULT 'Europe/Madrid';

ALTER TABLE tareas
    MODIFY COLUMN recordatorio_activo BIT NOT NULL DEFAULT 0;

ALTER TABLE tareas
    MODIFY COLUMN recordatorio_minutos_antes INT NULL;
