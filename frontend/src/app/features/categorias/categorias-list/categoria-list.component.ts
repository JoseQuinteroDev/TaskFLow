import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { CategoriaRequest, CategoriaResponse } from '../../../core/models/categoria.model';
import { CategoriaService } from '../../../core/services/categoria.service';
import { ToastService } from '../../../core/services/toast.service';

import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { LoadingComponent } from '../../../shared/components/loading/loading.component';

const PRESET_COLORS = [
  '#87f3b0',
  '#7db7ff',
  '#f7c66a',
  '#ff8b8b',
  '#d6b5ff',
  '#7ee7d8',
  '#b7c4d9',
  '#f39cc5'
];

@Component({
  selector: 'app-categoria-list',
  standalone: true,
  imports: [ReactiveFormsModule, LoadingComponent, EmptyStateComponent, ConfirmDialogComponent],
  templateUrl: './categoria-list.component.html',
  styleUrl: './categoria-list.component.scss'
})
export class CategoriaListComponent implements OnInit {
  private fb = inject(FormBuilder);
  private categoriaService = inject(CategoriaService);
  private toast = inject(ToastService);

  loading = signal(true);
  saving = signal(false);
  showForm = signal(false);
  editing = signal<CategoriaResponse | null>(null);
  deleteId = signal<number | null>(null);
  categorias = signal<CategoriaResponse[]>([]);

  presetColors = PRESET_COLORS;

  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    color: ['#87f3b0']
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.categoriaService.getAll().subscribe({
      next: categorias => {
        this.categorias.set(categorias);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  openForm(): void {
    this.editing.set(null);
    this.form.reset({ nombre: '', color: '#87f3b0' });
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editing.set(null);
  }

  startEdit(category: CategoriaResponse): void {
    this.editing.set(category);
    this.form.patchValue({
      nombre: category.nombre,
      color: category.color ?? '#87f3b0'
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
    const editingCategory = this.editing();

    const request = editingCategory
      ? this.categoriaService.update(editingCategory.id, data)
      : this.categoriaService.create(data);

    request.subscribe({
      next: () => {
        this.toast.success(editingCategory ? 'Categoría actualizada' : 'Categoría creada');
        this.saving.set(false);
        this.closeForm();
        this.load();
      },
      error: err => {
        this.saving.set(false);
        this.toast.error('Error', err.error?.mensaje ?? 'No hemos podido guardar la categoría.');
      }
    });
  }

  confirmDelete(): void {
    const id = this.deleteId();
    if (!id) {
      return;
    }

    this.categoriaService.delete(id).subscribe({
      next: () => {
        this.toast.success('Categoría eliminada');
        this.deleteId.set(null);
        this.load();
      },
      error: () => {
        this.toast.error('Error', 'No hemos podido eliminar la categoría.');
        this.deleteId.set(null);
      }
    });
  }
}
