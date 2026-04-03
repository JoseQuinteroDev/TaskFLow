package com.josequintero.taskflow.service;

import com.josequintero.taskflow.dto.auth.AuthResponseDto;
import com.josequintero.taskflow.dto.auth.CurrentUserResponseDto;
import com.josequintero.taskflow.dto.auth.LoginRequestDto;
import com.josequintero.taskflow.dto.auth.RegisterRequestDto;
import com.josequintero.taskflow.dto.auth.UpdateTimezoneRequestDto;
import com.josequintero.taskflow.exception.BusinessException;
import com.josequintero.taskflow.model.Rol;
import com.josequintero.taskflow.model.Usuario;
import com.josequintero.taskflow.model.enums.NombreRol;
import com.josequintero.taskflow.repositories.RolRepository;
import com.josequintero.taskflow.repositories.UsuarioRepository;
import com.josequintero.taskflow.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TareaTemporalService tareaTemporalService;
    private final CurrentUserService currentUserService;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            TareaTemporalService tareaTemporalService,
            CurrentUserService currentUserService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tareaTemporalService = tareaTemporalService;
        this.currentUserService = currentUserService;
    }

    @Override
    public AuthResponseDto register(RegisterRequestDto request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        String normalizedTimezone = tareaTemporalService.normalizeTimezone(request.getTimezone(), "Europe/Madrid");

        if (usuarioRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException("El correo electrónico ya está registrado");
        }

        Rol rolUser = rolRepository.findByNombre(NombreRol.ROLE_USER)
                .orElseThrow(() -> new BusinessException("No existe el rol ROLE_USER"));

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .email(normalizedEmail)
                .timezone(normalizedTimezone)
                .password(passwordEncoder.encode(request.getPassword()))
                .activo(true)
                .roles(Set.of(rolUser))
                .build();

        usuarioRepository.save(usuario);

        String token = jwtService.generateTokenByEmail(usuario.getEmail());
        return buildAuthResponse(usuario, token);
    }

    @Override
    public AuthResponseDto login(LoginRequestDto request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        String normalizedTimezone = tareaTemporalService.normalizeTimezone(request.getTimezone(), "Europe/Madrid");

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        normalizedEmail,
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));

        if (!normalizedTimezone.equals(usuario.getTimezone())) {
            usuario.setTimezone(normalizedTimezone);
            usuario = usuarioRepository.save(usuario);
        }

        String token = jwtService.generateToken(userDetails);
        return buildAuthResponse(usuario, token);
    }

    @Override
    public CurrentUserResponseDto currentUser() {
        Usuario usuario = currentUserService.getCurrentUser();
        usuario = ensurePersistedTimezone(usuario);
        return buildCurrentUserResponse(usuario);
    }

    @Override
    public CurrentUserResponseDto updateTimezone(UpdateTimezoneRequestDto request) {
        Usuario usuario = currentUserService.getCurrentUser();
        String normalizedTimezone = tareaTemporalService.normalizeTimezone(request.getTimezone(), "Europe/Madrid");

        if (!normalizedTimezone.equals(usuario.getTimezone())) {
            usuario.setTimezone(normalizedTimezone);
            usuario = usuarioRepository.save(usuario);
        }

        return buildCurrentUserResponse(usuario);
    }

    private AuthResponseDto buildAuthResponse(Usuario usuario, String token) {
        return AuthResponseDto.builder()
                .token(token)
                .tipoToken("Bearer")
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .timezone(usuario.getTimezone())
                .roles(extractRoles(usuario))
                .build();
    }

    private CurrentUserResponseDto buildCurrentUserResponse(Usuario usuario) {
        return CurrentUserResponseDto.builder()
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .timezone(usuario.getTimezone())
                .roles(extractRoles(usuario))
                .build();
    }

    private Set<String> extractRoles(Usuario usuario) {
        return usuario.getRoles()
                .stream()
                .map(rol -> rol.getNombre().name())
                .collect(Collectors.toSet());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private Usuario ensurePersistedTimezone(Usuario usuario) {
        String sanitizedTimezone = sanitizePersistedTimezone(usuario.getTimezone());

        if (!sanitizedTimezone.equals(usuario.getTimezone())) {
            usuario.setTimezone(sanitizedTimezone);
            return usuarioRepository.save(usuario);
        }

        return usuario;
    }

    private String sanitizePersistedTimezone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return tareaTemporalService.normalizeTimezone(null, "Europe/Madrid");
        }

        try {
            return tareaTemporalService.normalizeTimezone(timezone, "Europe/Madrid");
        } catch (BusinessException ex) {
            return tareaTemporalService.normalizeTimezone(null, "Europe/Madrid");
        }
    }
}
