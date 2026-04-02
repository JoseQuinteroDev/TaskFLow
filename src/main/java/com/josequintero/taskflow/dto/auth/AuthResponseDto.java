package com.josequintero.taskflow.dto.auth;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {

    private String token;
    private String tipoToken;
    private String email;
    private Set<String> roles;
}