package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class V9__migrate_deadlines_to_utc_and_add_user_timezones extends BaseJavaMigration {

    private static final ZoneId LEGACY_ZONE_ID = ZoneId.of("Europe/Madrid");

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            statement.execute("""
                    ALTER TABLE usuarios
                    ADD COLUMN timezone VARCHAR(60) NOT NULL DEFAULT 'Europe/Madrid' AFTER email
                    """);
            statement.execute("""
                    ALTER TABLE tareas
                    ADD COLUMN recordatorio_activo BIT NOT NULL DEFAULT 0 AFTER fecha_limite
                    """);
            statement.execute("""
                    ALTER TABLE tareas
                    ADD COLUMN recordatorio_minutos_antes INT NULL AFTER recordatorio_activo
                    """);
        }

        try (
                Statement selectStatement = context.getConnection().createStatement();
                ResultSet resultSet = selectStatement.executeQuery("SELECT id, fecha_limite FROM tareas WHERE fecha_limite IS NOT NULL");
                PreparedStatement updateStatement = context.getConnection()
                        .prepareStatement("UPDATE tareas SET fecha_limite = ? WHERE id = ?")
        ) {
            while (resultSet.next()) {
                LocalDateTime localDeadline = resultSet.getTimestamp("fecha_limite").toLocalDateTime();
                LocalDateTime utcDeadline = localDeadline
                        .atZone(LEGACY_ZONE_ID)
                        .withZoneSameInstant(ZoneOffset.UTC)
                        .toLocalDateTime();

                updateStatement.setTimestamp(1, Timestamp.valueOf(utcDeadline));
                updateStatement.setLong(2, resultSet.getLong("id"));
                updateStatement.addBatch();
            }

            updateStatement.executeBatch();
        }
    }
}
