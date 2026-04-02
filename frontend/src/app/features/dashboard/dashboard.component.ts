import { computed, Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { TareaService } from '../../core/services/tarea.service';
import { ToastService } from '../../core/services/toast.service';
import { TareaResponse, TareaResumen } from '../../core/models/tarea.model';

import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { LoadingComponent } from '../../shared/components/loading/loading.component';
import { TareaCardComponent } from '../tareas/tarea-card/tarea-card.component';

interface StatCard {
  label: string;
  value: number;
  hint: string;
  icon: string;
  color: 'accent' | 'amber' | 'red' | 'blue';
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    RouterLink,
    LoadingComponent,
    EmptyStateComponent,
    TareaCardComponent,
    ConfirmDialogComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  private static readonly RECENT_TASK_LIMIT = 6;

  loading = signal(true);
  resumen = signal<TareaResumen | null>(null);
  recentTareas = signal<TareaResponse[]>([]);
  statCards = signal<StatCard[]>([]);
  deleteId = signal<number | null>(null);

  readonly today = new Intl.DateTimeFormat('es-ES', {
    weekday: 'long',
    day: 'numeric',
    month: 'long'
  }).format(new Date());

  readonly completionRate = computed(() => {
    const resumen = this.resumen();
    if (!resumen?.total) {
      return 0;
    }

    return Math.round((resumen.completadas / resumen.total) * 100);
  });
  readonly progressWidth = computed(() => `${this.completionRate()}%`);

  readonly focusTitle = computed(() => {
    const resumen = this.resumen();

    if (!resumen?.total) {
      return 'Empieza creando tu primer flujo de trabajo';
    }

    if (resumen.vencidas > 0) {
      return 'Hay tareas vencidas que necesitan atencion';
    }

    if (resumen.pendientes > 0) {
      return 'Tu bandeja esta bajo control';
    }

    return 'Todo el trabajo del dia esta en orden';
  });

  readonly focusText = computed(() => {
    const resumen = this.resumen();

    if (!resumen?.total) {
      return 'Crea una tarea, asignale prioridad y empieza a construir tu ritmo de trabajo.';
    }

    if (resumen.vencidas > 0) {
      return `Tienes ${resumen.vencidas} tarea${resumen.vencidas > 1 ? 's' : ''} fuera de plazo. Reprioriza cuanto antes.`;
    }

    if (resumen.pendientes > 0) {
      return `Quedan ${resumen.pendientes} tarea${resumen.pendientes > 1 ? 's' : ''} pendientes. Buen momento para avanzar.`;
    }

    return 'Buen cierre: todas las tareas activas estan resueltas o en buen estado.';
  });

  constructor(
    private tareaService: TareaService,
    private authService: AuthService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  greeting(): string {
    const hour = new Date().getHours();
    if (hour < 13) return 'Buenos dias';
    if (hour < 20) return 'Buenas tardes';
    return 'Buenas noches';
  }

  firstName(): string {
    const nombre = this.authService.currentUser()?.nombre?.trim();
    if (!nombre) {
      return 'Usuario';
    }

    return nombre.split(/\s+/)[0];
  }

  loadData(): void {
    this.loading.set(true);

    forkJoin({
      resumen: this.tareaService.getResumen(),
      tareas: this.tareaService.getAll()
    }).subscribe({
      next: ({ resumen, tareas }) => {
        this.resumen.set(resumen);
        this.statCards.set([
          {
            label: 'Total',
            value: resumen.total,
            hint: 'Tareas registradas',
            color: 'blue',
            icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>`
          },
          {
            label: 'Pendientes',
            value: resumen.pendientes,
            hint: 'Por resolver',
            color: 'amber',
            icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>`
          },
          {
            label: 'Completadas',
            value: resumen.completadas,
            hint: 'Trabajo finalizado',
            color: 'accent',
            icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>`
          },
          {
            label: 'Vencidas',
            value: resumen.vencidas,
            hint: 'Necesitan reaccion',
            color: 'red',
            icon: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/><line x1="12" y1="7" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>`
          }
        ]);
        this.recentTareas.set(
          [...tareas]
            .sort((a, b) => b.fechaCreacion.localeCompare(a.fechaCreacion))
            .slice(0, DashboardComponent.RECENT_TASK_LIMIT)
        );
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Error', 'No hemos podido cargar el dashboard.');
      }
    });
  }

  onCompletar(id: number): void {
    this.tareaService.completar(id).subscribe({
      next: () => {
        this.toast.success('Tarea completada');
        this.loadData();
      },
      error: () => this.toast.error('Error', 'No hemos podido completar la tarea.')
    });
  }

  onEliminar(id: number): void {
    this.deleteId.set(id);
  }

  confirmDelete(): void {
    const id = this.deleteId();
    if (!id) {
      return;
    }

    this.tareaService.delete(id).subscribe({
      next: () => {
        this.toast.success('Tarea eliminada');
        this.deleteId.set(null);
        this.loadData();
      },
      error: () => {
        this.toast.error('Error', 'No hemos podido eliminar la tarea.');
        this.deleteId.set(null);
      }
    });
  }
}
