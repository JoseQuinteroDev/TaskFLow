import { Component, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink],
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent {
    form = this.fb.nonNullable.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', Validators.required],
    });

    loading = signal(false);
    showPassword = signal(false);
    errorMsg = signal('');

    features = [
        { text: 'Organiza tareas por prioridad y categoría' },
        { text: 'Seguimiento de progreso en tiempo real' },
        { text: 'Filtros avanzados para encontrar todo' },
    ];

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router,
        private toast: ToastService,
    ) {}

    onSubmit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.loading.set(true);
        this.errorMsg.set('');

        this.authService.login(this.form.getRawValue()).subscribe({
            next: () => {
                this.toast.success('¡Bienvenido!', 'Sesión iniciada correctamente');
                this.router.navigate(['/dashboard']);
            },
            error: (err: HttpErrorResponse) => {
                this.loading.set(false);
                this.errorMsg.set(
                    err.status === 401
                        ? 'Email o contraseña incorrectos'
                        : 'Error al iniciar sesión. Inténtalo de nuevo.'
                );
            }
        });
    }
}