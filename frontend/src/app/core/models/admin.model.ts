export type AdminRoleFilter = 'ROLE_ADMIN' | 'ROLE_USER';

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface AdminUser {
  id: number;
  nombre: string;
  email: string;
  activo: boolean;
  timezone: string;
  roles: string[];
  admin: boolean;
  fechaCreacion: string;
}

export interface AdminUserFilters {
  email?: string;
  nombre?: string;
  rol?: AdminRoleFilter;
  activo?: boolean;
  page?: number;
  size?: number;
}

export interface AdminCreateUserRequest {
  nombre: string;
  email: string;
  password: string;
  admin?: boolean;
  activo?: boolean;
}

export interface AdminDashboardSummary {
  totalUsuarios: number;
  usuariosActivos: number;
  usuariosInactivos: number;
  tareasActivas: number;
  tareasVencidas: number;
  recordatoriosFallidos: number;
}

export interface AdminReminderFailure {
  id: number;
  tareaId: number;
  tareaTitulo: string;
  destinatario: string;
  usuarioEmail: string;
  tipo: 'PROXIMO_VENCIMIENTO' | 'VENCIDA';
  canal: 'EMAIL';
  error: string;
  fechaProgramada: string;
  fechaCreacion: string;
}
