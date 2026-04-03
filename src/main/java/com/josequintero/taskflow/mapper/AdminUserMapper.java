package com.josequintero.taskflow.mapper;

import com.josequintero.taskflow.dto.admin.AdminUserResponseDto;
import com.josequintero.taskflow.model.Usuario;
import com.josequintero.taskflow.model.enums.NombreRol;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AdminUserMapper {

    public AdminUserResponseDto toResponseDto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        Set<String> roles = usuario.getRoles()
                .stream()
                .map(rol -> rol.getNombre().name())
                .collect(Collectors.toSet());

        return AdminUserResponseDto.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .activo(usuario.getActivo())
                .timezone(usuario.getTimezone())
                .roles(roles)
                .admin(roles.contains(NombreRol.ROLE_ADMIN.name()))
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }
}
