package com.josequintero.taskflow.controller;

import com.josequintero.taskflow.dto.admin.AdminUserCreateRequestDto;
import com.josequintero.taskflow.dto.admin.AdminUserFilterRequestDto;
import com.josequintero.taskflow.dto.admin.AdminUserResponseDto;
import com.josequintero.taskflow.dto.admin.AdminUserRoleUpdateRequestDto;
import com.josequintero.taskflow.dto.admin.AdminUserStatusUpdateRequestDto;
import com.josequintero.taskflow.dto.admin.PageResponseDto;
import com.josequintero.taskflow.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public PageResponseDto<AdminUserResponseDto> getUsers(@Valid @ModelAttribute AdminUserFilterRequestDto filters) {
        return adminUserService.getUsers(filters);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponseDto createUser(@Valid @RequestBody AdminUserCreateRequestDto request) {
        return adminUserService.createUser(request);
    }

    @PatchMapping("/{id}/status")
    public AdminUserResponseDto updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserStatusUpdateRequestDto request
    ) {
        return adminUserService.updateStatus(id, request.getActivo());
    }

    @PatchMapping("/{id}/roles")
    public AdminUserResponseDto updateRoles(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserRoleUpdateRequestDto request
    ) {
        return adminUserService.updateAdminRole(id, request.getAdmin());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
    }
}
