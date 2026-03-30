CREATE TABLE categorias (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            nombre VARCHAR(80) NOT NULL,
                            color VARCHAR(20) NULL,
                            usuario_id BIGINT NOT NULL,
                            CONSTRAINT pk_categorias PRIMARY KEY (id),
                            CONSTRAINT fk_categorias_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
                            CONSTRAINT uk_categoria_usuario_nombre UNIQUE (usuario_id, nombre)
);

CREATE INDEX idx_categoria_usuario ON categorias(usuario_id);