import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Component, inject, OnInit, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

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

function scheduleValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const fechaInicio = control.get('fechaInicio')?.value as string | null | undefined;
    const fechaLimite = control.get('fechaLimite')?.value as string | null | undefined;
    const recordatorioActivo = control.get('recordatorioActivo')?.value as boolean | null | undefined;

    if (recordatorioActivo && !fechaInicio) {
      return { reminderWithoutStart: true };
    }

    if (!fechaInicio || !fechaLimite) {
      return null;
    }

    const inicio = new Date(fechaInicio);
    const limite = new Date(fechaLimite);

    if (Number.isNaN(inicio.getTime()) || Number.isNaN(limite.getTime())) {
      return null;
    }

    return limite.getTime() < inicio.getTime() ? { deadlineBeforeStart: true } : null;
  };
}

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
    descripcion: this.fb.nonNullable.control('', Validators.maxLength(1000)),
    prioridad: this.fb.nonNullable.control<PrioridadTarea>('MEDIA', Validators.required),
    estado: this.fb.nonNullable.control<EstadoTarea>('PENDIENTE'),
    fechaInicio: this.fb.nonNullable.control('', Validators.required),
    fechaLimite: this.fb.nonNullable.control(''),
    categoriaId: this.fb.control<number | null>(null),
    recordatorioActivo: this.fb.nonNullable.control(false),
    recordatorioMinutosAntes: this.fb.control<number | null>(60)
  }, { validators: scheduleValidator() });

  get fechaInicioControl() {
    return this.form.controls['fechaInicio'];
  }

  get fechaLimiteControl() {
    return this.form.controls['fechaLimite'];
  }

  get recordatorioActivoControl() {
    return this.form.controls['recordatorioActivo'];
  }

  get recordatorioMinutosAntesControl() {
    return this.form.controls['recordatorioMinutosAntes'];
  }

  ngOnInit(): void {
    this.categoriaService.getAll().subscribe(categorias => this.categorias.set(categorias));

    this.recordatorioActivoControl.valueChanges.subscribe(active => {
      if (!active) {
        this.recordatorioMinutosAntesControl.setValue(null);
        return;
      }

      if (!this.fechaInicioControl.value) {
        this.recordatorioActivoControl.setValue(false, { emitEvent: false });
        return;
      }

      if (!this.recordatorioMinutosAntesControl.value) {
        this.recordatorioMinutosAntesControl.setValue(60);
      }
    });

    this.fechaInicioControl.valueChanges.subscribe(value => {
      if (!value && this.recordatorioActivoControl.value) {
        this.recordatorioActivoControl.setValue(false);
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
    return this.categorias().find(item => item.id === categoryId)?.nombre ?? 'Sin categoría';
  }

  selectedReminderLabel(): string {
    if (!this.recordatorioActivoControl.value) {
      return 'Sin recordatorio';
    }

    const value = this.recordatorioMinutosAntesControl.value;
    const label = this.reminderOptions.find(option => option.value === value)?.label ?? 'Recordatorio activo';
    return `${label} del inicio`;
  }

  formattedStart(): string {
    return this.formatLocalDateTime(this.fechaInicioControl.value, 'Sin fecha de inicio');
  }

  formattedDeadline(): string {
    return this.formatLocalDateTime(this.fechaLimiteControl.value, 'Sin fecha límite');
  }

  formattedReminderSchedule(): string {
    if (!this.recordatorioActivoControl.value) {
      return 'Sin recordatorio';
    }

    const fechaInicioUtc = this.timezoneService.toUtcIso(this.fechaInicioControl.value);
    const minutosAntes = this.recordatorioMinutosAntesControl.value;

    if (!fechaInicioUtc || minutosAntes == null) {
      return 'Define una fecha de inicio para calcular el envío.';
    }

    const fechaEnvio = new Date(fechaInicioUtc);
    fechaEnvio.setMinutes(fechaEnvio.getMinutes() - minutosAntes);

    return this.timezoneService.format(fechaEnvio.toISOString(), {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  reminderHelperText(): string {
    if (!this.fechaInicioControl.value) {
      return 'El recordatorio se calcula siempre antes de la fecha de inicio.';
    }

    if (!this.recordatorioActivoControl.value) {
      return 'Actívalo si quieres recibir un aviso antes de empezar la tarea.';
    }

    return `Se enviará el ${this.formattedReminderSchedule()} en tu hora local (${this.currentTimezone}).`;
  }

  deadlineMinValue(): string | null {
    const fechaInicio = this.fechaInicioControl.value;
    if (fechaInicio) {
      return fechaInicio;
    }

    return this.isEditing() ? null : this.minDateTime;
  }

  hasScheduleError(errorKey: string): boolean {
    return !!this.form.errors?.[errorKey] && (this.form.touched || this.form.dirty);
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
          fechaInicio: this.timezoneService.fromUtcIsoToLocalInput(tarea.fechaInicio),
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
    const fechaInicioUtc = this.timezoneService.toUtcIso(value.fechaInicio);
    const fechaLimiteUtc = this.timezoneService.toUtcIso(value.fechaLimite);
    const recordatorioActivo = !!value.recordatorioActivo && !!fechaInicioUtc;

    if (!fechaInicioUtc) {
      this.loading.set(false);
      this.toast.error('Error', 'La fecha de inicio no tiene un formato válido.');
      return;
    }

    const payload = {
      titulo: value.titulo,
      descripcion: value.descripcion || undefined,
      prioridad: value.prioridad,
      estado: value.estado,
      fechaInicio: fechaInicioUtc,
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
          fechaInicio: payload.fechaInicio,
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

  private formatLocalDateTime(value: string, fallback: string): string {
    const utcValue = this.timezoneService.toUtcIso(value);
    if (!utcValue) {
      return fallback;
    }

    return this.timezoneService.format(utcValue, {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
