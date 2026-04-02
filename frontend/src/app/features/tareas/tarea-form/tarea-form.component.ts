import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { CategoriaService } from '../../../core/services/categoria.service';
import { TareaService } from '../../../core/services/tarea.service';
import { ToastService } from '../../../core/services/toast.service';

import { CategoriaResponse } from '../../../core/models/categoria.model';
import { EstadoTarea, PrioridadTarea, TareaResponse } from '../../../core/models/tarea.model';

import { LoadingComponent } from '../../../shared/components/loading/loading.component';

@Component({
  selector: 'app-tarea-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, LoadingComponent],
  templateUrl: './tarea-form.component.html',
  styleUrl: './tarea-form.component.scss'
})
export class TareaFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private tareaService = inject(TareaService);
  private categoriaService = inject(CategoriaService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private toast = inject(ToastService);

  loading = signal(false);
  loadingData = signal(false);
  categorias = signal<CategoriaResponse[]>([]);
  tareaId = signal<number | null>(null);

  readonly today = new Date().toISOString().split('T')[0];
  readonly isEditing = () => !!this.tareaId();

  prioridades: { value: PrioridadTarea; label: string }[] = [
    { value: 'ALTA', label: 'Alta' },
    { value: 'MEDIA', label: 'Media' },
    { value: 'BAJA', label: 'Baja' }
  ];

  estados: { value: EstadoTarea; label: string }[] = [
    { value: 'PENDIENTE', label: 'Pendiente' },
    { value: 'EN_PROCESO', label: 'En proceso' },
    { value: 'COMPLETADA', label: 'Completada' }
  ];

  form = this.fb.group({
    titulo: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.minLength(3),
      Validators.maxLength(100)
    ]),
    descripcion: this.fb.nonNullable.control('', Validators.maxLength(500)),
    prioridad: this.fb.nonNullable.control<PrioridadTarea>('MEDIA', Validators.required),
    estado: this.fb.nonNullable.control<EstadoTarea>('PENDIENTE'),
    fechaLimite: this.fb.nonNullable.control(''),
    categoriaId: this.fb.control<number | null>(null)
  });

  ngOnInit(): void {
    this.categoriaService.getAll().subscribe(categorias => this.categorias.set(categorias));

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.tareaId.set(+id);
      this.loadTarea(+id);
    }
  }

  f(name: string) {
    return this.form.get(name)!;
  }

  titleLength(): number {
    return this.f('titulo').value?.length ?? 0;
  }

  descriptionLength(): number {
    return this.f('descripcion').value?.length ?? 0;
  }

  selectedPriorityLabel(): string {
    return this.prioridades.find(item => item.value === this.f('prioridad').value)?.label ?? 'Media';
  }

  selectedEstadoLabel(): string {
    return this.estados.find(item => item.value === this.f('estado').value)?.label ?? 'Pendiente';
  }

  selectedCategoryLabel(): string {
    const categoryId = this.f('categoriaId').value;
    return this.categorias().find(item => item.id === categoryId)?.nombre ?? 'Sin categoría';
  }

  formattedDeadline(): string {
    const value = this.f('fechaLimite').value;
    if (!value) {
      return 'Sin fecha límite';
    }

    return new Intl.DateTimeFormat('es-ES', {
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    }).format(new Date(`${value}T00:00:00`));
  }

  loadTarea(id: number): void {
    this.loadingData.set(true);

    this.tareaService.getById(id).subscribe({
      next: (tarea: TareaResponse) => {
        this.form.patchValue({
          titulo: tarea.titulo,
          descripcion: tarea.descripcion ?? '',
          prioridad: tarea.prioridad,
          estado: tarea.estado,
          fechaLimite: tarea.fechaLimite ? tarea.fechaLimite.split('T')[0] : '',
          categoriaId: tarea.categoria?.id ?? null
        });
        this.loadingData.set(false);
      },
      error: () => {
        this.toast.error('Error', 'No hemos encontrado la tarea solicitada.');
        this.router.navigate(['/tareas']);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    const value = this.form.getRawValue();

    const payload = {
      titulo: value.titulo,
      descripcion: value.descripcion || undefined,
      prioridad: value.prioridad,
      estado: value.estado,
      fechaLimite: value.fechaLimite || undefined,
      categoriaId: value.categoriaId ? +value.categoriaId : undefined
    };

    const request = this.isEditing()
      ? this.tareaService.update(this.tareaId()!, payload)
      : this.tareaService.create({
          titulo: payload.titulo,
          descripcion: payload.descripcion,
          prioridad: payload.prioridad,
          fechaLimite: payload.fechaLimite,
          categoriaId: payload.categoriaId
        });

    request.subscribe({
      next: () => {
        this.toast.success(
          this.isEditing() ? 'Tarea actualizada' : 'Tarea creada',
          this.isEditing()
            ? 'Los cambios se han guardado correctamente.'
            : 'Tu nueva tarea ya forma parte del flujo.'
        );
        this.router.navigate(['/tareas']);
      },
      error: err => {
        this.loading.set(false);
        const message = err.error?.mensaje ?? 'No hemos podido guardar la tarea.';
        this.toast.error('Error', message);
      }
    });
  }
}
