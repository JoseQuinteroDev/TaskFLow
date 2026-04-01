import { Component, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

function passwordStrength(control: AbstractControl): { [key: string]: boolean } | null {
    const val: string = control.value ?? '';
    if (!val) return null;

    const hasUpper = /[A-Z]/.test(val);
    const hasNum = /[0-9]/.test(val);

    if (!hasUpper || !hasNum) return { weak: true };
    return null;
}

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink],
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
    form = this.fb.nonNullable.group({
        nombre: ['', [Validators.required, Validators.minLength(2)]],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(8), passwordStrength]],
    });

    loading = signal(false);
    showPwd = signal(false);
    errorMsg = signal('');
    strengthPct = 0;

    features = [
        'Cuenta gratuita y sin límites',
        'Tus datos siempre privados',
        'Acceso desde cualquier dispositivo',
    ];

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router,
        private toast: ToastService,
    ) {}

    f(name: string) {
        return this.form.get(name)!;
    }

    strengthLevel(): 'weak' | 'medium' | 'strong' {
        if (this.strengthPct >= 80) return 'strong';
        if (this.strengthPct >= 40) return 'medium';
        return 'weak';
    }

    strengthLabel(): string {
        const map = {
            weak: 'Débil',
            medium: 'Media',
            strong: 'Fuerte'
        };
        return map[this.strengthLevel()];
    }

    updateStrength(): void {
        const val: string = this.f('password').value ?? '';
        let score = 0;

        if (val.length >= 8) score += 25;
        if (val.length >= 12) score += 15;
        if (/[A-Z]/.test(val)) score += 20;
        if (/[0-9]/.test(val)) score += 20;
        if (/[^A-Za-z0-9]/.test(val)) score += 20;

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
                this.toast.success('¡Cuenta creada!', 'Ya puedes empezar a usar TaskFlow');
                this.router.navigate(['/dashboard']);
            },
            error: (err: HttpErrorResponse) => {
                this.loading.set(false);
                this.errorMsg.set(
                    err.status === 409
                        ? 'Este email ya está registrado'
                        : 'Error al crear la cuenta. Inténtalo de nuevo.'
                );
            }
        });
    }
}