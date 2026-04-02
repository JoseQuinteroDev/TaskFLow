package com.josequintero.taskflow.service;

import com.josequintero.taskflow.exception.BusinessException;
import com.josequintero.taskflow.model.Usuario;
import com.josequintero.taskflow.repositories.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    private final UsuarioRepository usuarioRepository;

    public CurrentUserServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new BusinessException("No existe un usuario autenticado en el contexto actual");
        }

        return usuarioRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new BusinessException("No se ha podido resolver el usuario autenticado"));
    }
}
