import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { CategoriaService } from '../../../core/services/categoria.service';
import { ToastService } from '../../../core/services/toast.service';
import { TareaService } from '../../../core/services/tarea.service';

import { CategoriaResponse } from '../../../core/models/categoria.model';
import { EstadoTarea, TareaFiltros, TareaResponse } from '../../../core/models/tarea.model';

import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { LoadingComponent } from '../../../shared/components/loading/loading.component';
import { TareaCardComponent } from '../tarea-card/tarea-card.component';
import { TareaFiltrosComponent } from '../tarea-filtros/tarea-filtros.component';

type ViewMode = 'grid' | 'list';
type SortField = 'fechaInicio' | 'fechaLimite' | 'fechaCreacion' | 'prioridad' | 'titulo';

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
  styleUrl: './tarea-list.component.scss'
})
export class TareaListComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  loading = signal(true);
  allTareas = signal<TareaResponse[]>([]);
  categorias = signal<CategoriaResponse[]>([]);
  viewMode = signal<ViewMode>('grid');
  activeTab = signal<EstadoTarea | 'TODAS'>('TODAS');
  deleteId = signal<number | null>(null);

  private filtros: TareaFiltros = {};
  sortField: SortField = 'fechaInicio';

  estadoTabs = [
    { value: 'TODAS' as const, label: 'Todas' },
    { value: 'PENDIENTE' as const, label: 'Pendientes' },
    { value: 'EN_PROCESO' as const, label: 'En proceso' },
    { value: 'COMPLETADA' as const, label: 'Completadas' }
  ];

  visibleTareas = computed(() => {
    let list = [...this.allTareas()];
    const tab = this.activeTab();

    if (tab !== 'TODAS') {
      list = list.filter(tarea => tarea.estado === tab);
    }

    return this.sort(list);
  });

  totalLabel = computed(() => {
    const count = this.visibleTareas().length;
    return `${count} tarea${count !== 1 ? 's' : ''}`;
  });

  constructor(
    private tareaService: TareaService,
    private categoriaService: CategoriaService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.load();
    this.categoriaService.getAll().subscribe(categorias => this.categorias.set(categorias));
    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
      const estado = params.get('estado') as EstadoTarea | null;
      const isKnown = this.estadoTabs.some(tab => tab.value === estado);
      this.activeTab.set(isKnown && estado ? estado : 'TODAS');
    });
  }

  load(): void {
    this.loading.set(true);

    const request = Object.keys(this.filtros).length
      ? this.tareaService.filtrar(this.filtros)
      : this.tareaService.getAll();

    request.subscribe({
      next: tareas => {
        this.allTareas.set(tareas);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  onFiltrosChange(filtros: TareaFiltros): void {
    this.filtros = filtros;
    this.load();
  }

  setTab(tab: EstadoTarea | 'TODAS'): void {
    this.activeTab.set(tab);
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { estado: tab === 'TODAS' ? null : tab },
      queryParamsHandling: 'merge',
      replaceUrl: true
    });
  }

  countByEstado(tab: EstadoTarea | 'TODAS'): number {
    if (tab === 'TODAS') {
      return this.allTareas().length;
    }

    return this.allTareas().filter(tarea => tarea.estado === tab).length;
  }

  onSort(event: Event): void {
    this.sortField = (event.target as HTMLSelectElement).value as SortField;
  }

  onCompletar(id: number): void {
    this.tareaService.completar(id).subscribe({
      next: () => {
        this.toast.success('Tarea completada');
        this.load();
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
        this.load();
      },
      error: () => {
        this.toast.error('Error', 'No hemos podido eliminar la tarea.');
        this.deleteId.set(null);
      }
    });
  }

  emptyTitle(): string {
    return Object.keys(this.filtros).length ? 'No hay resultados para estos filtros' : 'Aún no has creado tareas';
  }

  emptyDesc(): string {
    return Object.keys(this.filtros).length
      ? 'Prueba a ampliar la búsqueda o ajustar los filtros activos.'
      : 'Crea la primera tarea para empezar a organizar el trabajo.';
  }

  private sort(list: TareaResponse[]): TareaResponse[] {
    return list.sort((a, b) => {
      switch (this.sortField) {
        case 'fechaInicio':
          return a.fechaInicio.localeCompare(b.fechaInicio);
        case 'fechaLimite':
          if (!a.fechaLimite) return 1;
          if (!b.fechaLimite) return -1;
          return a.fechaLimite.localeCompare(b.fechaLimite);
        case 'prioridad':
          return { ALTA: 0, MEDIA: 1, BAJA: 2 }[a.prioridad] - { ALTA: 0, MEDIA: 1, BAJA: 2 }[b.prioridad];
        case 'titulo':
          return a.titulo.localeCompare(b.titulo);
        default:
          return b.fechaCreacion.localeCompare(a.fechaCreacion);
      }
    });
  }
}
