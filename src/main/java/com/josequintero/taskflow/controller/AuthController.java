package com.josequintero.taskflow.controller;

import com.josequintero.taskflow.dto.auth.AuthResponseDto;
import com.josequintero.taskflow.dto.auth.CurrentUserResponseDto;
import com.josequintero.taskflow.dto.auth.LoginRequestDto;
import com.josequintero.taskflow.dto.auth.RegisterRequestDto;
import com.josequintero.taskflow.dto.auth.UpdateTimezoneRequestDto;
import com.josequintero.taskflow.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public CurrentUserResponseDto currentUser() {
        return authService.currentUser();
    }

    @PatchMapping("/timezone")
    @PreAuthorize("isAuthenticated()")
    public CurrentUserResponseDto updateTimezone(@Valid @RequestBody UpdateTimezoneRequestDto request) {
        return authService.updateTimezone(request);
    }
}
