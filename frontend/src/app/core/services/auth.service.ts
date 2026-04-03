import { computed, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

import { AuthResponse, CurrentUserResponse, LoginRequest, RegisterRequest, UpdateTimezoneRequest } from '../models/auth.model';
import { TimezoneService } from './timezone.service';

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
    private router: Router,
    private timezoneService: TimezoneService
  ) {}

  login(payload: Omit<LoginRequest, 'timezone'>): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, {
      ...payload,
      timezone: this.timezoneService.detect()
    }).pipe(
      tap(response => this.setSession(response))
    );
  }

  register(payload: Omit<RegisterRequest, 'timezone'>): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, {
      ...payload,
      timezone: this.timezoneService.detect()
    }).pipe(
      tap(response => this.setSession(response))
    );
  }

  logout(): void {
    if (this.hasStorage()) {
      localStorage.removeItem(this.storageKey);
    }

    this.authState.set(null);
    this.router.navigate(['/login']);
  }

  token(): string | null {
    return this.authState()?.token ?? null;
  }

  currentUser(): { nombre: string; email: string; timezone: string; roles: string[] } | null {
    const auth = this.authState();
    if (!auth) {
      return null;
    }

    return {
      nombre: this.normalizeDisplayName(auth.nombre, auth.email),
      email: auth.email,
      timezone: auth.timezone || this.timezoneService.detect(),
      roles: auth.roles
    };
  }

  hydrateSession(): void {
    const auth = this.authState();
    if (!auth?.token) {
      return;
    }

    const detectedTimezone = this.timezoneService.detect();
    const profileRequest = auth.timezone !== detectedTimezone
      ? this.http.patch<CurrentUserResponse>(`${this.apiUrl}/timezone`, {
          timezone: detectedTimezone
        } satisfies UpdateTimezoneRequest)
      : this.http.get<CurrentUserResponse>(`${this.apiUrl}/me`);

    profileRequest.subscribe({
      next: profile => {
        this.setSession({
          ...auth,
          nombre: this.normalizeDisplayName(profile.nombre, profile.email),
          email: profile.email,
          timezone: profile.timezone || this.timezoneService.detect(),
          roles: profile.roles?.length ? profile.roles : auth.roles
        });
      },
      error: () => {
        const normalizedName = this.normalizeDisplayName(auth.nombre, auth.email);
        if (normalizedName !== auth.nombre || auth.timezone !== detectedTimezone) {
          this.setSession({
            ...auth,
            nombre: normalizedName,
            timezone: detectedTimezone
          });
        }
      }
    });
  }

  private hasStorage(): boolean {
    return typeof localStorage !== 'undefined';
  }

  private setSession(auth: AuthResponse): void {
    const normalizedAuth = this.normalizeAuth(auth);

    if (this.hasStorage()) {
      localStorage.setItem(this.storageKey, JSON.stringify(normalizedAuth));
    }

    this.authState.set(normalizedAuth);
  }

  private loadAuth(): AuthResponse | null {
    if (!this.hasStorage()) {
      return null;
    }

    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return null;
    }

    try {
      return this.normalizeAuth(JSON.parse(raw) as AuthResponse);
    } catch {
      localStorage.removeItem(this.storageKey);
      return null;
    }
  }

  private normalizeAuth(auth: AuthResponse): AuthResponse {
    return {
      ...auth,
      nombre: this.normalizeDisplayName(auth.nombre, auth.email),
      timezone: auth.timezone || this.timezoneService.detect(),
      roles: auth.roles ?? []
    };
  }

  private normalizeDisplayName(nombre: string | null | undefined, email: string | null | undefined): string {
    const cleanName = nombre?.trim();
    if (cleanName) {
      return cleanName;
    }

    const localPart = email?.split('@')[0]?.trim();
    if (!localPart) {
      return 'Usuario';
    }

    const normalized = localPart
      .replace(/[._-]+/g, ' ')
      .replace(/\s+/g, ' ')
      .trim();

    if (!normalized) {
      return 'Usuario';
    }

    return normalized.replace(/\b\p{L}/gu, letter => letter.toUpperCase());
  }
}
