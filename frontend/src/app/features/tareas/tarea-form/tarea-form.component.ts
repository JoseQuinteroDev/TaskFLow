import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { CategoriaService } from '../../../core/services/categoria.service';
import { TareaService } from '../../../core/services/tarea.service';
import { ToastService } from '../../../core/services/toast.service';
import { TimezoneService } from '../../../core/services/timezone.service';

import { CategoriaResponse } from '../../../core/models/categoria.model';
import {
  EstadoTarea,
  PrioridadTarea,
  RECORDATORIO_OPTIONS,
  TareaResponse
} from '../../../core/models/tarea.model';

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
  private timezoneService = inject(TimezoneService);

  loading = signal(false);
  loadingData = signal(false);
  categorias = signal<CategoriaResponse[]>([]);
  tareaId = signal<number | null>(null);

  readonly currentTimezone = this.timezoneService.detect();
  readonly minDateTime = this.timezoneService.localNowInputValue();
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

  reminderOptions = RECORDATORIO_OPTIONS;

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
    categoriaId: this.fb.control<number | null>(null),
    recordatorioActivo: this.fb.nonNullable.control(false),
    recordatorioMinutosAntes: this.fb.control<number | null>(60)
  });

  ngOnInit(): void {
    this.categoriaService.getAll().subscribe(categorias => this.categorias.set(categorias));

    this.form.controls.recordatorioActivo.valueChanges.subscribe(active => {
      if (!active) {
        this.form.controls.recordatorioMinutosAntes.setValue(null);
        return;
      }

      if (!this.form.controls.recordatorioMinutosAntes.value) {
        this.form.controls.recordatorioMinutosAntes.setValue(60);
      }
    });

    this.form.controls.fechaLimite.valueChanges.subscribe(value => {
      if (!value && this.form.controls.recordatorioActivo.value) {
        this.form.controls.recordatorioActivo.setValue(false);
      }
    });

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
    return this.categorias().find(item => item.id === categoryId)?.nombre ?? 'Sin categoria';
  }

  selectedReminderLabel(): string {
    if (!this.form.controls.recordatorioActivo.value) {
      return 'Sin recordatorio';
    }

    const value = this.form.controls.recordatorioMinutosAntes.value;
    return this.reminderOptions.find(option => option.value === value)?.label ?? 'Recordatorio activo';
  }

  formattedDeadline(): string {
    const value = this.form.controls.fechaLimite.value;
    if (!value) {
      return 'Sin fecha limite';
    }

    const utcValue = this.timezoneService.toUtcIso(value);
    return this.timezoneService.format(utcValue, {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
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
          fechaLimite: this.timezoneService.fromUtcIsoToLocalInput(tarea.fechaLimite),
          categoriaId: tarea.categoria?.id ?? null,
          recordatorioActivo: tarea.recordatorioActivo,
          recordatorioMinutosAntes: tarea.recordatorioMinutosAntes ?? 60
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
    const fechaLimiteUtc = this.timezoneService.toUtcIso(value.fechaLimite);
    const recordatorioActivo = !!value.recordatorioActivo && !!fechaLimiteUtc;

    const payload = {
      titulo: value.titulo,
      descripcion: value.descripcion || undefined,
      prioridad: value.prioridad,
      estado: value.estado,
      fechaLimite: fechaLimiteUtc,
      categoriaId: value.categoriaId ? +value.categoriaId : undefined,
      recordatorioActivo,
      recordatorioMinutosAntes: recordatorioActivo ? value.recordatorioMinutosAntes ?? 60 : undefined
    };

    const request = this.isEditing()
      ? this.tareaService.update(this.tareaId()!, payload)
      : this.tareaService.create({
          titulo: payload.titulo,
          descripcion: payload.descripcion,
          prioridad: payload.prioridad,
          fechaLimite: payload.fechaLimite,
          categoriaId: payload.categoriaId,
          recordatorioActivo: payload.recordatorioActivo,
          recordatorioMinutosAntes: payload.recordatorioMinutosAntes
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
