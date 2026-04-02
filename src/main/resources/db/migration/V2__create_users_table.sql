CREATE TABLE usuarios (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          nombre VARCHAR(100) NOT NULL,
                          email VARCHAR(120) NOT NULL,
                          password VARCHAR(255) NOT NULL,
                          activo BIT NOT NULL,
                          fecha_creacion DATETIME NOT NULL,
                          CONSTRAINT pk_usuarios PRIMARY KEY (id),
                          CONSTRAINT uk_usuarios_email UNIQUE (email)
);

CREATE INDEX idx_usuarios_email ON usuarios(email);