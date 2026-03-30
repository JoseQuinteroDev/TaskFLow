package com.josequintero.taskflow.repositories;

import com.josequintero.taskflow.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    //implementación de funciones por derivación del nombre del método
    Optional<Usuario> findByEmail(String email);
    boolean existsAllEmail(String email);
}
