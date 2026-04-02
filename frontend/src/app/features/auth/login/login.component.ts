import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastService);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  loading = signal(false);
  showPassword = signal(false);
  errorMsg = signal('');

  features = [
    { text: 'Organiza tareas por prioridad y categoría' },
    { text: 'Haz seguimiento de progreso sin perder contexto' },
    { text: 'Encuentra lo importante con filtros claros y rápidos' }
  ];

  togglePassword(): void {
    this.showPassword.update(value => !value);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMsg.set('');

    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.toast.success('Bienvenido', 'Tu sesión se ha iniciado correctamente');
        this.router.navigate(['/dashboard']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMsg.set(
          err.status === 401
            ? 'El email o la contraseña no coinciden.'
            : 'No hemos podido iniciar sesión. Inténtalo de nuevo.'
        );
      }
    });
  }
}
