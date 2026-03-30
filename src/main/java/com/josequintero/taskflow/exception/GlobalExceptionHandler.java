package com.josequintero.taskflow.exception;

import com.josequintero.taskflow.dto.error.ApiErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> details = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                details.put(error.getField(), error.getDefaultMessage())
        );

        ApiErrorDto apiError = ApiErrorDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .mensaje("Error de validación")
                .path(request.getRequestURI())
                .details(details)
                .build();

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        ApiErrorDto apiError = ApiErrorDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .mensaje(ex.getMessage())
                .path(request.getRequestURI())
                .details(null)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorDto> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        ApiErrorDto apiError = ApiErrorDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .mensaje(ex.getMessage())
                .path(request.getRequestURI())
                .details(null)
                .build();

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<ApiErrorDto> handleUnauthorizedOperationException(
            UnauthorizedOperationException ex,
            HttpServletRequest request
    ) {
        ApiErrorDto apiError = ApiErrorDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .mensaje(ex.getMessage())
                .path(request.getRequestURI())
                .details(null)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorDto> handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        ApiErrorDto apiError = ApiErrorDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .mensaje("Credenciales incorrectas")
                .path(request.getRequestURI())
                .details(null)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDto> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        ApiErrorDto apiError = ApiErrorDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .mensaje("No tienes permisos para acceder a este recurso")
                .path(request.getRequestURI())
                .details(null)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        ApiErrorDto apiError = ApiErrorDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .mensaje("Ha ocurrido un error interno inesperado")
                .path(request.getRequestURI())
                .details(null)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}