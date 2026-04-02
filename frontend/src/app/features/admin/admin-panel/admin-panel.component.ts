import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';

import {
  AdminCreateUserRequest,
  AdminDashboardSummary,
  AdminReminderFailure,
  AdminRoleFilter,
  AdminUser,
  AdminUserFilters,
  PageResponse
} from '../../../core/models/admin.model';
import { AdminService } from '../../../core/services/admin.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { LoadingComponent } from '../../../shared/components/loading/loading.component';

interface AdminStatCard {
  label: string;
  value: number;
  hint: string;
  tone: 'accent' | 'blue' | 'amber' | 'red';
}

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    LoadingComponent,
    EmptyStateComponent,
    ConfirmDialogComponent
  ],
  templateUrl: './admin-panel.component.html',
  styleUrl: './admin-panel.component.scss'
})
export class AdminPanelComponent implements OnInit {
  private fb = inject(FormBuilder);
  private adminService = inject(AdminService);
  private authService = inject(AuthService);
  private toast = inject(ToastService);

  loading = signal(true);
  usersLoading = signal(false);
  failuresLoading = signal(false);
  creating = signal(false);
  actionUserId = signal<number | null>(null);
  showCreateForm = signal(false);

  summary = signal<AdminDashboardSummary | null>(null);
  usersPage = signal<PageResponse<AdminUser> | null>(null);
  reminderFailures = signal<AdminReminderFailure[]>([]);
  deleteTarget = signal<AdminUser | null>(null);

  readonly currentUserEmail = computed(() => this.authService.currentUser()?.email ?? '');
  readonly users = computed(() => this.usersPage()?.content ?? []);
  readonly pageLabel = computed(() => {
    const page = this.usersPage();
    if (!page || page.totalElements === 0) {
      return 'Sin resultados';
    }

    const from = page.page * page.size + 1;
    const to = Math.min((page.page + 1) * page.size, page.totalElements);
    return `${from}-${to} de ${page.totalElements} usuarios`;
  });

  readonly statCards = computed<AdminStatCard[]>(() => {
    const summary = this.summary();

    if (!summary) {
      return [];
    }

    return [
      {
        label: 'Usuarios',
        value: summary.totalUsuarios,
        hint: 'Cuentas registradas en la plataforma',
        tone: 'blue'
      },
      {
        label: 'Activos',
        value: summary.usuariosActivos,
        hint: 'Usuarios con acceso operativo',
        tone: 'accent'
      },
      {
        label: 'Inactivos',
        value: summary.usuariosInactivos,
        hint: 'Cuentas pausadas o bloqueadas',
        tone: 'amber'
      },
      {
        label: 'Tareas activas',
        value: summary.tareasActivas,
        hint: 'Trabajo vivo en el sistema',
        tone: 'blue'
      },
      {
        label: 'Tareas vencidas',
        value: summary.tareasVencidas,
        hint: 'Puntos de riesgo a revisar',
        tone: 'red'
      },
      {
        label: 'Fallos de aviso',
        value: summary.recordatoriosFallidos,
        hint: 'Notificaciones que requieren atencion',
        tone: 'amber'
      }
    ];
  });

  readonly filtersForm = this.fb.nonNullable.group({
    email: [''],
    nombre: [''],
    rol: ['' as '' | AdminRoleFilter],
    activo: ['' as '' | 'true' | 'false']
  });

  readonly createForm = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    password: [
      '',
      [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(100),
        Validators.pattern(/^(?=.*[A-Z])(?=.*\d).{8,100}$/)
      ]
    ],
    admin: [false],
    activo: [true]
  });

  ngOnInit(): void {
    this.loadInitialData();
  }

  toggleCreateForm(): void {
    this.showCreateForm.update(value => !value);

    if (!this.showCreateForm()) {
      this.createForm.reset({
        nombre: '',
        email: '',
        password: '',
        admin: false,
        activo: true
      });
    }
  }

  applyFilters(): void {
    this.loadUsers(0);
  }

  clearFilters(): void {
    this.filtersForm.reset({
      email: '',
      nombre: '',
      rol: '',
      activo: ''
    });
    this.loadUsers(0);
  }

  goToPreviousPage(): void {
    const page = this.usersPage();
    if (!page || page.first) {
      return;
    }

    this.loadUsers(page.page - 1);
  }

  goToNextPage(): void {
    const page = this.usersPage();
    if (!page || page.last) {
      return;
    }

    this.loadUsers(page.page + 1);
  }

  onCreateUser(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    this.creating.set(true);

    const raw = this.createForm.getRawValue();
    const payload: AdminCreateUserRequest = {
      nombre: raw.nombre.trim(),
      email: raw.email.trim(),
      password: raw.password,
      admin: raw.admin,
      activo: raw.activo
    };

    this.adminService.createUser(payload).subscribe({
      next: createdUser => {
        this.creating.set(false);
        this.toast.success('Usuario creado', `${createdUser.nombre} ya esta disponible en el workspace.`);
        this.toggleCreateForm();
        this.refreshUsersAfterMutation();
        this.loadSummary();
      },
      error: err => {
        this.creating.set(false);
        this.toast.error('Error', this.resolveError(err, 'No hemos podido crear el usuario.'));
      }
    });
  }

  toggleStatus(user: AdminUser): void {
    this.actionUserId.set(user.id);

    this.adminService.updateUserStatus(user.id, !user.activo).subscribe({
      next: updated => {
        this.actionUserId.set(null);
        this.toast.success(
          updated.activo ? 'Usuario activado' : 'Usuario desactivado',
          `${updated.nombre} ha quedado ${updated.activo ? 'activo' : 'inactivo'}.`
        );
        this.refreshUsersAfterMutation();
        this.loadSummary();
      },
      error: err => {
        this.actionUserId.set(null);
        this.toast.error('Error', this.resolveError(err, 'No hemos podido actualizar el estado del usuario.'));
      }
    });
  }

  toggleAdminRole(user: AdminUser): void {
    this.actionUserId.set(user.id);

    this.adminService.updateUserRole(user.id, !user.admin).subscribe({
      next: updated => {
        this.actionUserId.set(null);
        this.toast.success(
          updated.admin ? 'Rol admin concedido' : 'Rol admin retirado',
          `${updated.nombre} ahora ${updated.admin ? 'puede administrar la plataforma' : 'mantiene acceso de usuario base'}.`
        );
        this.refreshUsersAfterMutation();
      },
      error: err => {
        this.actionUserId.set(null);
        this.toast.error('Error', this.resolveError(err, 'No hemos podido actualizar los roles.'));
      }
    });
  }

  confirmDelete(): void {
    const target = this.deleteTarget();
    if (!target) {
      return;
    }

    this.actionUserId.set(target.id);
    this.adminService.deleteUser(target.id).subscribe({
      next: () => {
        this.actionUserId.set(null);
        this.deleteTarget.set(null);
        this.toast.success('Usuario eliminado', `${target.nombre} ha sido eliminado del sistema.`);
        this.refreshUsersAfterDelete();
        this.loadSummary();
        this.loadReminderFailures();
      },
      error: err => {
        this.actionUserId.set(null);
        this.deleteTarget.set(null);
        this.toast.error('Error', this.resolveError(err, 'No hemos podido eliminar el usuario.'));
      }
    });
  }

  requestDelete(user: AdminUser): void {
    this.deleteTarget.set(user);
  }

  roleBadgeClass(user: AdminUser): string {
    return user.admin ? 'badge badge-info' : 'badge badge-neutral';
  }

  statusBadgeClass(user: AdminUser): string {
    return user.activo ? 'badge badge-success' : 'badge badge-danger';
  }

  roleLabel(user: AdminUser): string {
    return user.admin ? 'Administrador' : 'Miembro';
  }

  isSelf(user: AdminUser): boolean {
    return user.email === this.currentUserEmail();
  }

  isBusy(user: AdminUser): boolean {
    return this.actionUserId() === user.id;
  }

  formattedDate(value: string | null | undefined): string {
    if (!value) {
      return 'Sin datos';
    }

    const compact = value.replace('T', ' / ');
    return compact.length >= 16 ? compact.slice(0, 16) : compact;
  }

  failureTypeLabel(failure: AdminReminderFailure): string {
    return failure.tipo === 'VENCIDA' ? 'Tarea vencida' : 'Vence pronto';
  }

  private loadInitialData(): void {
    this.loading.set(true);

    forkJoin({
      summary: this.adminService.getDashboardSummary(),
      users: this.adminService.getUsers(this.buildFilters(0)),
      failures: this.adminService.getReminderFailures(8)
    }).subscribe({
      next: ({ summary, users, failures }) => {
        this.summary.set(summary);
        this.usersPage.set(users);
        this.reminderFailures.set(failures);
        this.loading.set(false);
      },
      error: err => {
        this.loading.set(false);
        this.toast.error('Error', this.resolveError(err, 'No hemos podido cargar el panel de administracion.'));
      }
    });
  }

  private loadSummary(): void {
    this.adminService.getDashboardSummary().subscribe({
      next: summary => this.summary.set(summary),
      error: () => undefined
    });
  }

  private loadUsers(page: number): void {
    this.usersLoading.set(true);

    this.adminService.getUsers(this.buildFilters(page)).subscribe({
      next: users => {
        this.usersPage.set(users);
        this.usersLoading.set(false);
      },
      error: err => {
        this.usersLoading.set(false);
        this.toast.error('Error', this.resolveError(err, 'No hemos podido cargar los usuarios.'));
      }
    });
  }

  private loadReminderFailures(): void {
    this.failuresLoading.set(true);

    this.adminService.getReminderFailures(8).subscribe({
      next: failures => {
        this.reminderFailures.set(failures);
        this.failuresLoading.set(false);
      },
      error: () => {
        this.failuresLoading.set(false);
      }
    });
  }

  private buildFilters(page: number): AdminUserFilters {
    const raw = this.filtersForm.getRawValue();

    return {
      email: raw.email.trim() || undefined,
      nombre: raw.nombre.trim() || undefined,
      rol: raw.rol || undefined,
      activo: raw.activo === '' ? undefined : raw.activo === 'true',
      page,
      size: 10
    };
  }

  private refreshUsersAfterMutation(): void {
    const page = this.usersPage()?.page ?? 0;
    this.loadUsers(page);
  }

  private refreshUsersAfterDelete(): void {
    const currentPage = this.usersPage();
    const shouldGoBack =
      !!currentPage &&
      currentPage.page > 0 &&
      currentPage.content.length === 1;

    this.loadUsers(shouldGoBack ? currentPage.page - 1 : currentPage?.page ?? 0);
  }

  private resolveError(err: { error?: { mensaje?: string } }, fallback: string): string {
    return err.error?.mensaje ?? fallback;
  }
}
