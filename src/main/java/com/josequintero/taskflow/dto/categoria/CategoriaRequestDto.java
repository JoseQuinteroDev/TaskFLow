package com.josequintero.taskflow.dto.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaRequestDto {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
    private String nombre;

    @Pattern(
            regexp = "^#([A-Fa-f0-9]{6})$",
            message = "El color debe tener formato hexadecimal, por ejemplo #3B82F6"
    )
    private String color;
}