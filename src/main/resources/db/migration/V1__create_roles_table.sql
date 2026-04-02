CREATE TABLE roles (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       nombre VARCHAR(30) NOT NULL,
                       CONSTRAINT pk_roles PRIMARY KEY (id),
                       CONSTRAINT uk_roles_nombre UNIQUE (nombre)
);