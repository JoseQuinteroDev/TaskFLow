import { Component, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';

import { TareaService } from '../../../core/services/tarea.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { ToastService } from '../../../core/services/toast.service';

import { CategoriaResponse } from '../../../core/models/categoria.model';
import { PrioridadTarea, EstadoTarea, TareaResponse } from '../../../core/models/tarea.model';

import { LoadingComponent } from '../../../shared/components/loading/loading.component';

@Component({
    selector: 'app-tarea-form',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink, LoadingComponent],
    templateUrl: './tarea-form.component.html',
    styleUrls: ['./tarea-form.component.scss']
})
export class TareaFormComponent implements OnInit {
    loading = signal(false);
    loadingData = signal(false);
    categorias = signal<CategoriaResponse[]>([]);
    tareaId = signal<number | null>(null);

    readonly today = new Date().toISOString().split('T')[0];
    readonly isEditing = () => !!this.tareaId();

    prioridades: { value: PrioridadTarea; label: string }[] = [
        { value: 'ALTA', label: 'Alta' },
        { value: 'MEDIA', label: 'Media' },
        { value: 'BAJA', label: 'Baja' },
    ];

    form = this.fb.nonNullable.group({
        titulo: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
        descripcion: ['', Validators.maxLength(500)],
        prioridad: ['MEDIA' as PrioridadTarea, Validators.required],
        estado: ['PENDIENTE' as EstadoTarea],
        fechaLimite: [''],
        categoriaId: [null as number | null],
    });

    constructor(
        private fb: FormBuilder,
        private tareaService: TareaService,
        private categoriaService: CategoriaService,
        private router: Router,
        private route: ActivatedRoute,
        private toast: ToastService,
    ) {}

    ngOnInit(): void {
        this.categoriaService.getAll().subscribe(c => this.categorias.set(c));

        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.tareaId.set(+id);
            this.loadTarea(+id);
        }
    }

    f(name: string) {
        return this.form.get(name)!;
    }

    loadTarea(id: number): void {
        this.loadingData.set(true);

        this.tareaService.getById(id).subscribe({
            next: (t: TareaResponse) => {
                this.form.patchValue({
                    titulo: t.titulo,
                    descripcion: t.descripcion ?? '',
                    prioridad: t.prioridad,
                    estado: t.estado,
                    fechaLimite: t.fechaLimite ? t.fechaLimite.split('T')[0] : '',
                    categoriaId: t.categoria?.id ?? null,
                });
                this.loadingData.set(false);
            },
            error: () => {
                this.toast.error('Tarea no encontrada');
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
        const val = this.form.getRawValue();

        const payload = {
            titulo: val.titulo,
            descripcion: val.descripcion || undefined,
            prioridad: val.prioridad,
            estado: val.estado,
            fechaLimite: val.fechaLimite || undefined,
            categoriaId: val.categoriaId ? +val.categoriaId : undefined,
        };

        const obs = this.isEditing()
            ? this.tareaService.update(this.tareaId()!, payload)
            : this.tareaService.create({
                titulo: payload.titulo,
                descripcion: payload.descripcion,
                prioridad: payload.prioridad,
                fechaLimite: payload.fechaLimite,
                categoriaId: payload.categoriaId
            });

        obs.subscribe({
            next: () => {
                this.toast.success(
                    this.isEditing() ? 'Tarea actualizada' : 'Tarea creada',
                    this.isEditing() ? 'Los cambios se guardaron correctamente' : 'Tu nueva tarea está lista'
                );
                this.router.navigate(['/tareas']);
            },
            error: (err) => {
                this.loading.set(false);
                const msg = err.error?.mensaje ?? 'Error al guardar la tarea';
                this.toast.error('Error', msg);
            }
        });
    }
}