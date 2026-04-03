package com.josequintero.taskflow.service;

import com.josequintero.taskflow.dto.admin.AdminUserCreateRequestDto;
import com.josequintero.taskflow.dto.admin.AdminUserFilterRequestDto;
import com.josequintero.taskflow.dto.admin.AdminUserResponseDto;
import com.josequintero.taskflow.dto.admin.PageResponseDto;

public interface AdminUserService {

    PageResponseDto<AdminUserResponseDto> getUsers(AdminUserFilterRequestDto filters);

    AdminUserResponseDto createUser(AdminUserCreateRequestDto request);

    AdminUserResponseDto updateStatus(Long userId, boolean activo);

    AdminUserResponseDto updateAdminRole(Long userId, boolean admin);

    void deleteUser(Long userId);
}
