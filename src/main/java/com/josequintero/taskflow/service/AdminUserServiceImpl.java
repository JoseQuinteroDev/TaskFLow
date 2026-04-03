package com.josequintero.taskflow.service;

import com.josequintero.taskflow.dto.admin.AdminUserCreateRequestDto;
import com.josequintero.taskflow.dto.admin.AdminUserFilterRequestDto;
import com.josequintero.taskflow.dto.admin.AdminUserResponseDto;
import com.josequintero.taskflow.dto.admin.PageResponseDto;
import com.josequintero.taskflow.exception.BusinessException;
import com.josequintero.taskflow.exception.ResourceNotFoundException;
import com.josequintero.taskflow.mapper.AdminUserMapper;
import com.josequintero.taskflow.model.Rol;
import com.josequintero.taskflow.model.Usuario;
import com.josequintero.taskflow.model.enums.NombreRol;
import com.josequintero.taskflow.repositories.CategoriaRepository;
import com.josequintero.taskflow.repositories.RecordatorioTareaRepository;
import com.josequintero.taskflow.repositories.RolRepository;
import com.josequintero.taskflow.repositories.TareaRepository;
import com.josequintero.taskflow.repositories.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final TareaRepository tareaRepository;
    private final CategoriaRepository categoriaRepository;
    private final RecordatorioTareaRepository recordatorioTareaRepository;
    private final PasswordEncoder passwordEncoder;
    private final TareaTemporalService tareaTemporalService;
    private final CurrentUserService currentUserService;
    private final AdminUserMapper adminUserMapper;

    public AdminUserServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            TareaRepository tareaRepository,
            CategoriaRepository categoriaRepository,
            RecordatorioTareaRepository recordatorioTareaRepository,
            PasswordEncoder passwordEncoder,
            TareaTemporalService tareaTemporalService,
            CurrentUserService currentUserService,
            AdminUserMapper adminUserMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.tareaRepository = tareaRepository;
        this.categoriaRepository = categoriaRepository;
        this.recordatorioTareaRepository = recordatorioTareaRepository;
        this.passwordEncoder = passwordEncoder;
        this.tareaTemporalService = tareaTemporalService;
        this.currentUserService = currentUserService;
        this.adminUserMapper = adminUserMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<AdminUserResponseDto> getUsers(AdminUserFilterRequestDto filters) {
        AdminUserFilterRequestDto safeFilters = filters == null ? new AdminUserFilterRequestDto() : filters;

        int page = safePage(safeFilters.getPage());
        int size = safeSize(safeFilters.getSize());

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
        var resultPage = usuarioRepository.searchAdminUsers(
                normalizeFilter(safeFilters.getEmail()),
                normalizeFilter(safeFilters.getNombre()),
                safeFilters.getRol(),
                safeFilters.getActivo(),
                pageable
        ).map(adminUserMapper::toResponseDto);

        return PageResponseDto.from(resultPage);
    }

    @Override
    @Transactional
    public AdminUserResponseDto createUser(AdminUserCreateRequestDto request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (usuarioRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException("Ya existe un usuario con ese correo electrónico");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .email(normalizedEmail)
                .timezone(tareaTemporalService.normalizeTimezone(request.getTimezone(), "Europe/Madrid"))
                .password(passwordEncoder.encode(request.getPassword()))
                .activo(request.getActivo() == null || request.getActivo())
                .roles(resolveRoles(Boolean.TRUE.equals(request.getAdmin())))
                .build();

        return adminUserMapper.toResponseDto(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public AdminUserResponseDto updateStatus(Long userId, boolean activo) {
        Usuario targetUser = getManagedUser(userId);
        Usuario currentUser = currentUserService.getCurrentUser();

        if (currentUser.getId().equals(targetUser.getId()) && !activo) {
            throw new BusinessException("No puedes desactivar tu propia cuenta desde el panel de administración");
        }

        if (!activo) {
            ensureActiveAdminRemains(targetUser);
        }

        targetUser.setActivo(activo);
        return adminUserMapper.toResponseDto(targetUser);
    }

    @Override
    @Transactional
    public AdminUserResponseDto updateAdminRole(Long userId, boolean admin) {
        Usuario targetUser = getManagedUser(userId);
        Usuario currentUser = currentUserService.getCurrentUser();

        if (currentUser.getId().equals(targetUser.getId()) && !admin) {
            throw new BusinessException("No puedes quitarte a ti mismo el rol de administrador desde este panel");
        }

        if (!admin) {
            ensureAdminRoleCanBeRemoved(targetUser);
        }

        targetUser.setRoles(resolveRoles(admin));
        return adminUserMapper.toResponseDto(targetUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        Usuario currentUser = currentUserService.getCurrentUser();
        Usuario targetUser = getManagedUser(userId);

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new BusinessException("No puedes eliminar tu propia cuenta desde el panel de administración");
        }

        ensureAdminRoleCanBeRemoved(targetUser);
        ensureActiveAdminRemains(targetUser);

        recordatorioTareaRepository.deleteByUsuarioId(targetUser.getId());
        tareaRepository.deleteByUsuarioId(targetUser.getId());
        categoriaRepository.deleteByUsuarioId(targetUser.getId());
        usuarioRepository.delete(targetUser);
    }

    private Usuario getManagedUser(Long userId) {
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Set<Rol> resolveRoles(boolean admin) {
        Set<Rol> roles = new LinkedHashSet<>();
        roles.add(findRole(NombreRol.ROLE_USER));

        if (admin) {
            roles.add(findRole(NombreRol.ROLE_ADMIN));
        }

        return roles;
    }

    private Rol findRole(NombreRol roleName) {
        return rolRepository.findByNombre(roleName)
                .orElseThrow(() -> new BusinessException("No existe el rol " + roleName.name()));
    }

    private void ensureAdminRoleCanBeRemoved(Usuario targetUser) {
        if (!hasAdminRole(targetUser)) {
            return;
        }

        long totalAdmins = usuarioRepository.countDistinctByRol(NombreRol.ROLE_ADMIN);
        if (totalAdmins <= 1) {
            throw new BusinessException("Debe existir al menos un administrador en el sistema");
        }
    }

    private void ensureActiveAdminRemains(Usuario targetUser) {
        if (!Boolean.TRUE.equals(targetUser.getActivo()) || !hasAdminRole(targetUser)) {
            return;
        }

        long activeAdmins = usuarioRepository.countDistinctActivosByRol(NombreRol.ROLE_ADMIN);
        if (activeAdmins <= 1) {
            throw new BusinessException("Debe existir al menos un administrador activo en el sistema");
        }
    }

    private boolean hasAdminRole(Usuario usuario) {
        return usuario.getRoles()
                .stream()
                .anyMatch(rol -> rol.getNombre() == NombreRol.ROLE_ADMIN);
    }

    private int safePage(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    private int safeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_PAGE_SIZE;
        }

        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
