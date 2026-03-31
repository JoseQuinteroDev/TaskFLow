package com.josequintero.taskflow.repositories;

import com.josequintero.taskflow.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// UsuarioRepository.java
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}
