package com.josequintero.taskflow.repositories;

import com.josequintero.taskflow.model.Usuario;
import com.josequintero.taskflow.model.enums.NombreRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByActivoTrue();

    long countByActivoFalse();

    @Query("""
           SELECT COUNT(DISTINCT u.id)
           FROM Usuario u
           JOIN u.roles r
           WHERE r.nombre = :rol
           """)
    long countDistinctByRol(@Param("rol") NombreRol rol);

    @Query("""
           SELECT COUNT(DISTINCT u.id)
           FROM Usuario u
           JOIN u.roles r
           WHERE r.nombre = :rol
             AND u.activo = true
           """)
    long countDistinctActivosByRol(@Param("rol") NombreRol rol);

    @Query(
            value = """
                    SELECT DISTINCT u
                    FROM Usuario u
                    LEFT JOIN u.roles r
                    WHERE (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
                      AND (:nombre IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
                      AND (:activo IS NULL OR u.activo = :activo)
                      AND (
                          :rol IS NULL
                          OR (:rol = com.josequintero.taskflow.model.enums.NombreRol.ROLE_ADMIN
                              AND EXISTS (
                                  SELECT 1
                                  FROM Usuario ux
                                  JOIN ux.roles rx
                                  WHERE ux.id = u.id
                                    AND rx.nombre = com.josequintero.taskflow.model.enums.NombreRol.ROLE_ADMIN
                              ))
                          OR (:rol = com.josequintero.taskflow.model.enums.NombreRol.ROLE_USER
                              AND NOT EXISTS (
                                  SELECT 1
                                  FROM Usuario ux
                                  JOIN ux.roles rx
                                  WHERE ux.id = u.id
                                    AND rx.nombre = com.josequintero.taskflow.model.enums.NombreRol.ROLE_ADMIN
                              ))
                      )
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT u.id)
                    FROM Usuario u
                    LEFT JOIN u.roles r
                    WHERE (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
                      AND (:nombre IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
                      AND (:activo IS NULL OR u.activo = :activo)
                      AND (
                          :rol IS NULL
                          OR (:rol = com.josequintero.taskflow.model.enums.NombreRol.ROLE_ADMIN
                              AND EXISTS (
                                  SELECT 1
                                  FROM Usuario ux
                                  JOIN ux.roles rx
                                  WHERE ux.id = u.id
                                    AND rx.nombre = com.josequintero.taskflow.model.enums.NombreRol.ROLE_ADMIN
                              ))
                          OR (:rol = com.josequintero.taskflow.model.enums.NombreRol.ROLE_USER
                              AND NOT EXISTS (
                                  SELECT 1
                                  FROM Usuario ux
                                  JOIN ux.roles rx
                                  WHERE ux.id = u.id
                                    AND rx.nombre = com.josequintero.taskflow.model.enums.NombreRol.ROLE_ADMIN
                              ))
                      )
                    """
    )
    Page<Usuario> searchAdminUsers(
            @Param("email") String email,
            @Param("nombre") String nombre,
            @Param("rol") NombreRol rol,
            @Param("activo") Boolean activo,
            Pageable pageable
    );
}
