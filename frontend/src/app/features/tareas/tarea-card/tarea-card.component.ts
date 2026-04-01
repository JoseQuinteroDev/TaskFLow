import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import {
    TareaResponse,
    EstadoTarea,
    ESTADO_LABELS,
    PRIORIDAD_LABELS
} from '../../../core/models/tarea.model';

@Component({
    selector: 'app-tarea-card',
    standalone: true,
    imports: [RouterLink],
    templateUrl: './tarea-card.component.html',
    styleUrls: ['./tarea-card.component.scss']
})
export class TareaCardComponent {
    @Input() tarea!: TareaResponse;
    @Output() completar = new EventEmitter<number>();
    @Output() eliminar = new EventEmitter<number>();

    confirmDelete = signal(false);

    estadoLabel(e: EstadoTarea): string {
        return ESTADO_LABELS[e];
    }

    prioridadLabel(): string {
        return PRIORIDAD_LABELS[this.tarea.prioridad];
    }

    estadoBadge(estado: EstadoTarea): string {
        const map: Record<EstadoTarea, string> = {
            PENDIENTE: 'badge-warning',
            EN_PROCESO: 'badge-info',
            COMPLETADA: 'badge-success'
        };
        return map[estado];
    }

    prioridadBadge(): string {
        const map = {
            ALTA: 'badge-danger',
            MEDIA: 'badge-warning',
            BAJA: 'badge-neutral'
        };
        return map[this.tarea.prioridad];
    }

    formatDate(fecha: string): string {
        return new Date(fecha).toLocaleDateString('es-ES', {
            day: 'numeric',
            month: 'short'
        });
    }

    onEliminar(): void {
        this.confirmDelete.set(true);
    }
}