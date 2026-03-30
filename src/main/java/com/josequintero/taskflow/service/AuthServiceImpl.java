package com.josequintero.taskflow.service.impl;

import com.josequintero.taskflow.dto.auth.AuthResponseDto;
import com.josequintero.taskflow.dto.auth.LoginRequestDto;
import com.josequintero.taskflow.dto.auth.RegisterRequestDto;
import com.josequintero.taskflow.exception.BusinessException;
import com.josequintero.taskflow.model.Rol;
import com.josequintero.taskflow.model.Usuario;
import com.josequintero.taskflow.model.enums.NombreRol;
import com.josequintero.taskflow.repositories.RolRepository;
import com.josequintero.taskflow.repositories.UsuarioRepository;
import com.josequintero.taskflow.security.JwtService;
import com.josequintero.taskflow.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponseDto register(RegisterRequestDto request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya está registrado");
        }

        Rol rolUser = rolRepository.findByNombre(NombreRol.ROLE_USER)
                .orElseThrow(() -> new BusinessException("No existe el rol ROLE_USER"));

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .activo(true)
                .roles(Set.of(rolUser))
                .build();

        usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario);

        return AuthResponseDto.builder()
                .token(token)
                .tipoToken("Bearer")
                .email(usuario.getEmail())
                .roles(usuario.getRoles()
                        .stream()
                        .map(rol -> rol.getNombre().name())
                        .collect(Collectors.toSet()))
                .build();
    }

    @Override
    public AuthResponseDto login(LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));

        String token = jwtService.generateToken(userDetails);

        return AuthResponseDto.builder()
                .token(token)
                .tipoToken("Bearer")
                .email(usuario.getEmail())
                .roles(usuario.getRoles()
                        .stream()
                        .map(rol -> rol.getNombre().name())
                        .collect(Collectors.toSet()))
                .build();
    }
}