package com.josequintero.taskflow.controller;

import com.josequintero.taskflow.dto.tarea.CambiarEstadoRequestDto;
import com.josequintero.taskflow.dto.tarea.TareaCreateRequestDto;
import com.josequintero.taskflow.dto.tarea.TareaFiltroRequestDto;
import com.josequintero.taskflow.dto.tarea.TareaResponseDto;
import com.josequintero.taskflow.dto.tarea.TareaResumenDto;
import com.josequintero.taskflow.dto.tarea.TareaUpdateRequestDto;
import com.josequintero.taskflow.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    @GetMapping
    public List<TareaResponseDto> getAll() {
        return tareaService.getAll();
    }

    @GetMapping("/{id}")
    public TareaResponseDto getById(@PathVariable Long id) {
        return tareaService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TareaResponseDto create(@Valid @RequestBody TareaCreateRequestDto request) {
        return tareaService.create(request);
    }

    @PutMapping("/{id}")
    public TareaResponseDto update(@PathVariable Long id, @Valid @RequestBody TareaUpdateRequestDto request) {
        return tareaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tareaService.delete(id);
    }

    @PatchMapping("/{id}/completar")
    public TareaResponseDto completar(@PathVariable Long id) {
        return tareaService.completar(id);
    }

    @PatchMapping("/{id}/estado")
    public TareaResponseDto cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequestDto request) {
        return tareaService.cambiarEstado(id, request);
    }

    @GetMapping("/resumen")
    public TareaResumenDto resumen() {
        return tareaService.getResumen();
    }

    @GetMapping("/filtro")
    public List<TareaResponseDto> filtrar(@ModelAttribute TareaFiltroRequestDto filtros) {
        return tareaService.filtrar(filtros);
    }
}
