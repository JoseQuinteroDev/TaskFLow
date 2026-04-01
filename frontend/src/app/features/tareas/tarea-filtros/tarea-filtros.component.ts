import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { TareaFiltros, EstadoTarea, PrioridadTarea } from '../../../core/models/tarea.model';
import { CategoriaResponse } from '../../../core/models/categoria.model';

@Component({
    selector: 'app-tarea-filtros',
    standalone: true,
    imports: [ReactiveFormsModule],
    templateUrl: './tarea-filtros.component.html',
    styleUrls: ['./tarea-filtros.component.scss']
})
export class TareaFiltrosComponent implements OnInit {
    @Input() categorias: CategoriaResponse[] = [];
    @Output() filtrosChange = new EventEmitter<TareaFiltros>();

    form = this.fb.group({
        texto: [''],
        estado: ['' as EstadoTarea | ''],
        prioridad: ['' as PrioridadTarea | ''],
        categoriaId: ['' as number | ''],
        desde: [''],
        hasta: [''],
    });

    constructor(private fb: FormBuilder) {}

    ngOnInit(): void {
        this.form.controls.texto.valueChanges.pipe(
            debounceTime(350),
            distinctUntilChanged()
        ).subscribe(() => this.emit());

        this.form.valueChanges.pipe(
            distinctUntilChanged()
        ).subscribe(() => this.emit());
    }

    hasActiveFilters(): boolean {
        const v = this.form.value;
        return !!(v.texto || v.estado || v.prioridad || v.categoriaId || v.desde || v.hasta);
    }

    clearFilters(): void {
        this.form.reset({
            texto: '',
            estado: '',
            prioridad: '',
            categoriaId: '',
            desde: '',
            hasta: ''
        });
    }

    private emit(): void {
        const v = this.form.value;
        const filtros: TareaFiltros = {};

        if (v.texto) filtros.texto = v.texto;
        if (v.estado) filtros.estado = v.estado as EstadoTarea;
        if (v.prioridad) filtros.prioridad = v.prioridad as PrioridadTarea;
        if (v.categoriaId) filtros.categoriaId = +v.categoriaId;
        if (v.desde) filtros.desde = v.desde;
        if (v.hasta) filtros.hasta = v.hasta;

        this.filtrosChange.emit(filtros);
    }
}