import { Component, OnInit, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';

import { TareaService } from '../../../core/services/tarea.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { ToastService } from '../../../core/services/toast.service';

import { TareaResponse, TareaFiltros, EstadoTarea } from '../../../core/models/tarea.model';
import { CategoriaResponse } from '../../../core/models/categoria.model';

import { TareaCardComponent } from '../tarea-card/tarea-card.component';
import { TareaFiltrosComponent } from '../tarea-filtros/tarea-filtros.component';
import { LoadingComponent } from '../../../shared/components/loading/loading.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

type ViewMode = 'grid' | 'list';
type SortField = 'fechaCreacion' | 'fechaLimite' | 'prioridad' | 'titulo';

@Component({
    selector: 'app-tarea-list',
    standalone: true,
    imports: [
        RouterLink,
        TareaCardComponent,
        TareaFiltrosComponent,
        LoadingComponent,
        EmptyStateComponent,
        ConfirmDialogComponent
    ],
    templateUrl: './tarea-list.component.html',
    styleUrls: ['./tarea-list.component.scss']
})
export class TareaListComponent implements OnInit {
    loading = signal(true);
    allTareas = signal<TareaResponse[]>([]);
    categorias = signal<CategoriaResponse[]>([]);
    viewMode = signal<ViewMode>('grid');
    activeTab = signal<EstadoTarea | 'TODAS'>('TODAS');
    deleteId = signal<number | null>(null);

    private filtros: TareaFiltros = {};
    private sortField: SortField = 'fechaCreacion';

    estadoTabs = [
        { value: 'TODAS' as const, label: 'Todas' },
        { value: 'PENDIENTE' as const, label: 'Pendientes' },
        { value: 'EN_PROCESO' as const, label: 'En proceso' },
        { value: 'COMPLETADA' as const, label: 'Completadas' },
    ];

    visibleTareas = computed(() => {
        let list = [...this.allTareas()];
        const tab = this.activeTab();

        if (tab !== 'TODAS') {
            list = list.filter(t => t.estado === tab);
        }

        return this.sort(list);
    });

    totalLabel = computed(() => {
        const n = this.visibleTareas().length;
        return `${n} tarea${n !== 1 ? 's' : ''}`;
    });

    constructor(
        private tareaService: TareaService,
        private categoriaService: CategoriaService,
        private toast: ToastService,
    ) {}

    ngOnInit(): void {
        this.load();
        this.categoriaService.getAll().subscribe(c => this.categorias.set(c));
    }

    load(): void {
        this.loading.set(true);

        const obs = Object.keys(this.filtros).length
            ? this.tareaService.filtrar(this.filtros)
            : this.tareaService.getAll();

        obs.subscribe({
            next: (t) => {
                this.allTareas.set(t);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    onFiltrosChange(f: TareaFiltros): void {
        this.filtros = f;
        this.load();
    }

    setTab(tab: EstadoTarea | 'TODAS'): void {
        this.activeTab.set(tab);
    }

    countByEstado(tab: EstadoTarea | 'TODAS'): number {
        if (tab === 'TODAS') return this.allTareas().length;
        return this.allTareas().filter(t => t.estado === tab).length;
    }

    onSort(event: Event): void {
        this.sortField = (event.target as HTMLSelectElement).value as SortField;
    }

    onCompletar(id: number): void {
        this.tareaService.completar(id).subscribe({
            next: () => {
                this.toast.success('¡Tarea completada!');
                this.load();
            },
            error: () => this.toast.error('Error al completar la tarea')
        });
    }

    onEliminar(id: number): void {
        this.deleteId.set(id);
    }

    confirmDelete(): void {
        const id = this.deleteId();
        if (!id) return;

        this.tareaService.delete(id).subscribe({
            next: () => {
                this.toast.success('Tarea eliminada');
                this.deleteId.set(null);
                this.load();
            },
            error: () => {
                this.toast.error('Error al eliminar');
                this.deleteId.set(null);
            }
        });
    }

    emptyTitle(): string {
        return Object.keys(this.filtros).length ? 'Sin resultados' : 'Sin tareas todavía';
    }

    emptyDesc(): string {
        return Object.keys(this.filtros).length
            ? 'Prueba a cambiar los filtros de búsqueda'
            : 'Crea tu primera tarea y empieza a organizarte';
    }

    private sort(list: TareaResponse[]): TareaResponse[] {
        return list.sort((a, b) => {
            switch (this.sortField) {
                case 'fechaLimite':
                    if (!a.fechaLimite) return 1;
                    if (!b.fechaLimite) return -1;
                    return a.fechaLimite.localeCompare(b.fechaLimite);
                case 'prioridad':
                    const order = { ALTA: 0, MEDIA: 1, BAJA: 2 };
                    return order[a.prioridad] - order[b.prioridad];
                case 'titulo':
                    return a.titulo.localeCompare(b.titulo);
                default:
                    return b.fechaCreacion.localeCompare(a.fechaCreacion);
            }
        });
    }
}