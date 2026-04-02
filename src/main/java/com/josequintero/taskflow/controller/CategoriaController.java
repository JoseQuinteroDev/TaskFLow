package com.josequintero.taskflow.controller;

import com.josequintero.taskflow.dto.categoria.CategoriaRequestDto;
import com.josequintero.taskflow.dto.categoria.CategoriaResponseDto;
import com.josequintero.taskflow.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaResponseDto> getAll() {
        return categoriaService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponseDto create(@Valid @RequestBody CategoriaRequestDto request) {
        return categoriaService.create(request);
    }

    @PutMapping("/{id}")
    public CategoriaResponseDto update(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDto request) {
        return categoriaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoriaService.delete(id);
    }
}
