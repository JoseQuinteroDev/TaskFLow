import { EventEmitter, Input, Output, inject } from '@angular/core';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import {
  EstadoTarea,
  ESTADO_LABELS,
  PRIORIDAD_LABELS,
  RECORDATORIO_OPTIONS,
  TareaResponse
} from '../../../core/models/tarea.model';
import { TimezoneService } from '../../../core/services/timezone.service';

@Component({
  selector: 'app-tarea-card',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './tarea-card.component.html',
  styleUrl: './tarea-card.component.scss'
})
export class TareaCardComponent {
  private timezoneService = inject(TimezoneService);

  @Input() tarea!: TareaResponse;
  @Output() completar = new EventEmitter<number>();
  @Output() eliminar = new EventEmitter<number>();

  estadoLabel(estado: EstadoTarea): string {
    return ESTADO_LABELS[estado];
  }

  prioridadLabel(): string {
    return PRIORIDAD_LABELS[this.tarea.prioridad];
  }

  reminderLabel(): string {
    return RECORDATORIO_OPTIONS.find(option => option.value === this.tarea.recordatorioMinutosAntes)?.label ?? 'Recordatorio activo';
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
    return this.timezoneService.format(fecha, {
      day: 'numeric',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
