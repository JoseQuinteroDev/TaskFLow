import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { TareaService } from '../../core/services/tarea.service';
import { AuthService } from '../../core/services/auth.service';
import { TareaResumen, TareaResponse } from '../../core/models/tarea.model';

import { LoadingComponent } from '../../shared/components/loading/loading.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { TareaCardComponent } from '../tareas/tarea-card/tarea-card.component';

interface StatCard {
    label: string;
    value: number;
    icon: string;
    color: 'accent' | 'amber' | 'red' | 'blue';
}

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [RouterLink, LoadingComponent, EmptyStateComponent, TareaCardComponent],
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
    loading = signal(true);
    resumen = signal<TareaResumen | null>(null);
    recentTareas = signal<TareaResponse[]>([]);
    statCards = signal<StatCard[]>([]);

    readonly today = new Date().toLocaleDateString('es-ES', {
        weekday: 'long',
        day: 'numeric',
        month: 'long'
    });

    constructor(
        private tareaService: TareaService,
        private authService: AuthService
    ) {}

    ngOnInit(): void {
        this.loadData();
    }

    greeting(): string {
        const h = new Date().getHours();
        if (h < 13) return 'Buenos días';
        if (h < 20) return 'Buenas tardes';
        return 'Buenas noches';
    }

    firstName(): string {
        const email = this.authService.currentUser()?.email ?? '';
        return email.split('@')[0];
    }

    loadData(): void {
        this.tareaService.getResumen().subscribe({
            next: (r) => {
                this.resumen.set(r);
                this.statCards.set([
                    { label: 'Total', value: r.total, color: 'blue', icon: '' },
                    { label: 'Pendientes', value: r.pendientes, color: 'amber', icon: '' },
                    { label: 'Completadas', value: r.completadas, color: 'accent', icon: '' },
                    { label: 'Vencidas', value: r.vencidas, color: 'red', icon: '' },
                ]);
            }
        });

        this.tareaService.getAll().subscribe({
            next: (tareas) => {
                this.recentTareas.set(tareas.slice(0, 6));
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    onCompletar(id: number): void {
        this.tareaService.completar(id).subscribe(() => this.loadData());
    }

    onEliminar(id: number): void {
        this.tareaService.delete(id).subscribe(() => this.loadData());
    }
}