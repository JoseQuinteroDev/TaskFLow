package com.josequintero.taskflow.dto.categoria;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaResumenDto {

    private Long id;
    private String nombre;
    private String color;
}