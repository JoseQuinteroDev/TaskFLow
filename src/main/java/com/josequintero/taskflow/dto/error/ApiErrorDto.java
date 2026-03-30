package com.josequintero.taskflow.dto.error;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorDto {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String mensaje;
    private String path;
    private Map<String, String> details;
}