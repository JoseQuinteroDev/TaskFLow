import { Component, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';

import { CategoriaService } from '../../../core/services/categoria.service';
import { ToastService } from '../../../core/services/toast.service';

import { CategoriaResponse, CategoriaRequest } from '../../../core/models/categoria.model';

import { LoadingComponent } from '../../../shared/components/loading/loading.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

const PRESET_COLORS = [
    '#10b981', '#3b82f6', '#f59e0b', '#ef4444',
    '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16',
];

@Component({
    selector: 'app-categoria-list',
    standalone: true,
    imports: [ReactiveFormsModule, LoadingComponent, EmptyStateComponent, ConfirmDialogComponent],
    templateUrl: './categoria-list.component.html',
    styleUrls: ['./categoria-list.component.scss']
})
export class CategoriaListComponent implements OnInit {
    loading = signal(true);
    saving = signal(false);
    showForm = signal(false);
    editing = signal<CategoriaResponse | null>(null);
    deleteId = signal<number | null>(null);
    categorias = signal<CategoriaResponse[]>([]);

    presetColors = PRESET_COLORS;

    form = this.fb.nonNullable.group({
        nombre: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
        color: ['#10b981'],
    });

    constructor(
        private fb: FormBuilder,
        private categoriaService: CategoriaService,
        private toast: ToastService,
    ) {}

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.categoriaService.getAll().subscribe({
            next: (c) => {
                this.categorias.set(c);
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    openForm(): void {
        this.editing.set(null);
        this.form.reset({ nombre: '', color: '#10b981' });
        this.showForm.set(true);
    }

    closeForm(): void {
        this.showForm.set(false);
        this.editing.set(null);
    }

    startEdit(cat: CategoriaResponse): void {
        this.editing.set(cat);
        this.form.patchValue({
            nombre: cat.nombre,
            color: cat.color ?? '#10b981'
        });
        this.showForm.set(true);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.saving.set(true);
        const data: CategoriaRequest = this.form.getRawValue();
        const editingCat = this.editing();

        const obs = editingCat
            ? this.categoriaService.update(editingCat.id, data)
            : this.categoriaService.create(data);

        obs.subscribe({
            next: () => {
                this.toast.success(editingCat ? 'Categoría actualizada' : 'Categoría creada');
                this.saving.set(false);
                this.closeForm();
                this.load();
            },
            error: (err) => {
                this.saving.set(false);
                this.toast.error('Error', err.error?.mensaje ?? 'No se pudo guardar la categoría');
            }
        });
    }

    confirmDelete(): void {
        const id = this.deleteId();
        if (!id) return;

        this.categoriaService.delete(id).subscribe({
            next: () => {
                this.toast.success('Categoría eliminada');
                this.deleteId.set(null);
                this.load();
            },
            error: () => {
                this.toast.error('Error al eliminar');
                this.deleteId.set(null);
            }
        });
    }
}