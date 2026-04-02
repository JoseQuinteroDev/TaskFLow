package com.josequintero.taskflow.dto.categoria;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaResponseDto {

    private Long id;
    private String nombre;
    private String color;
}