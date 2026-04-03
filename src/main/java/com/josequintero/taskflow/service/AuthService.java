package com.josequintero.taskflow.service;

import com.josequintero.taskflow.dto.auth.AuthResponseDto;
import com.josequintero.taskflow.dto.auth.CurrentUserResponseDto;
import com.josequintero.taskflow.dto.auth.LoginRequestDto;
import com.josequintero.taskflow.dto.auth.RegisterRequestDto;
import com.josequintero.taskflow.dto.auth.UpdateTimezoneRequestDto;

public interface AuthService {

    AuthResponseDto register(RegisterRequestDto request);

    AuthResponseDto login(LoginRequestDto request);

    CurrentUserResponseDto currentUser();

    CurrentUserResponseDto updateTimezone(UpdateTimezoneRequestDto request);
}
