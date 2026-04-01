import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly apiUrl = 'http://localhost:8080/api/auth';
    private readonly storageKey = 'taskflow_auth';

    private readonly authState = signal<AuthResponse | null>(this.loadAuth());

    readonly auth = this.authState.asReadonly();
    readonly isAuthenticated = computed(() => !!this.authState()?.token);
    readonly isAdmin = computed(() => this.authState()?.roles?.includes('ROLE_ADMIN') ?? false);

    constructor(
        private http: HttpClient,
        private router: Router
    ) {}

    login(payload: LoginRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/login`, payload).pipe(
            tap((response) => this.setSession(response))
        );
    }

    register(payload: RegisterRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.apiUrl}/register`, payload).pipe(
            tap((response) => this.setSession(response))
        );
    }

    logout(): void {
        localStorage.removeItem(this.storageKey);
        this.authState.set(null);
        this.router.navigate(['/login']);
    }

    token(): string | null {
        return this.authState()?.token ?? null;
    }

    currentUser(): { email: string; roles: string[] } | null {
        const auth = this.authState();
        if (!auth) return null;

        return {
            email: auth.email,
            roles: auth.roles
        };
    }

    private setSession(auth: AuthResponse): void {
        localStorage.setItem(this.storageKey, JSON.stringify(auth));
        this.authState.set(auth);
    }

    private loadAuth(): AuthResponse | null {
        const raw = localStorage.getItem(this.storageKey);
        if (!raw) return null;

        try {
            return JSON.parse(raw) as AuthResponse;
        } catch {
            localStorage.removeItem(this.storageKey);
            return null;
        }
    }
}