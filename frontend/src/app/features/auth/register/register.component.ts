import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

function passwordStrength(control: AbstractControl): { [key: string]: boolean } | null {
  const value: string = control.value ?? '';
  if (!value) {
    return null;
  }

  const hasUpper = /[A-Z]/.test(value);
  const hasNumber = /[0-9]/.test(value);

  return hasUpper && hasNumber ? null : { weak: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8), passwordStrength]]
  });

  loading = signal(false);
  showPwd = signal(false);
  errorMsg = signal('');
  strengthPct = 0;

  features = [
    'Crea un espacio de trabajo serio y limpio desde el primer minuto.',
    'Centraliza tareas, prioridades y categorias en una sola interfaz.',
    'Manten contexto sin perder tiempo entre vistas o formularios.'
  ];

  f(name: string) {
    return this.form.get(name)!;
  }

  togglePassword(): void {
    this.showPwd.update(value => !value);
  }

  strengthLevel(): 'weak' | 'medium' | 'strong' {
    if (this.strengthPct >= 80) return 'strong';
    if (this.strengthPct >= 40) return 'medium';
    return 'weak';
  }

  strengthLabel(): string {
    const map = {
      weak: 'Debil',
      medium: 'Media',
      strong: 'Fuerte'
    };

    return map[this.strengthLevel()];
  }

  updateStrength(): void {
    const value: string = this.f('password').value ?? '';
    let score = 0;

    if (value.length >= 8) score += 25;
    if (value.length >= 12) score += 15;
    if (/[A-Z]/.test(value)) score += 20;
    if (/[0-9]/.test(value)) score += 20;
    if (/[^A-Za-z0-9]/.test(value)) score += 20;

    this.strengthPct = Math.min(100, score);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMsg.set('');

    this.authService.register(this.form.getRawValue()).subscribe({
      next: () => {
        this.toast.success('Cuenta creada', 'Tu workspace esta listo para empezar');
        this.router.navigate(['/dashboard']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMsg.set(
          err.status === 409
            ? 'Ese email ya esta registrado.'
            : 'No hemos podido crear tu cuenta. Intentalo de nuevo.'
        );
      }
    });
  }
}
