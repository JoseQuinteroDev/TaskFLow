CREATE TABLE usuarios_roles (
                                usuario_id BIGINT NOT NULL,
                                rol_id BIGINT NOT NULL,
                                CONSTRAINT pk_usuarios_roles PRIMARY KEY (usuario_id, rol_id),
                                CONSTRAINT fk_usuarios_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
                                CONSTRAINT fk_usuarios_roles_rol FOREIGN KEY (rol_id) REFERENCES roles(id)
);