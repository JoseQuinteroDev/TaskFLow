import { CategoriaResponse } from './categoria.model';

export type EstadoTarea = 'PENDIENTE' | 'EN_PROCESO' | 'COMPLETADA';
export type PrioridadTarea = 'BAJA' | 'MEDIA' | 'ALTA';

export interface TareaCreateRequest {
    titulo: string;
    descripcion?: string;
    prioridad: PrioridadTarea;
    fechaLimite?: string;
    categoriaId?: number;
    recordatorioActivo?: boolean;
    recordatorioMinutosAntes?: number;
}

export interface TareaUpdateRequest {
    titulo: string;
    descripcion?: string;
    prioridad: PrioridadTarea;
    estado: EstadoTarea;
    fechaLimite?: string;
    categoriaId?: number;
    recordatorioActivo?: boolean;
    recordatorioMinutosAntes?: number;
}

export interface TareaResponse {
    id: number;
    titulo: string;
    descripcion?: string;
    prioridad: PrioridadTarea;
    estado: EstadoTarea;
    fechaLimite?: string;
    vencida: boolean;
    completada: boolean;
    recordatorioActivo: boolean;
    recordatorioMinutosAntes?: number;
    fechaCreacion: string;
    categoria?: CategoriaResponse;
}

export interface TareaResumen {
    total: number;
    pendientes: number;
    completadas: number;
    vencidas: number;
}

export interface TareaFiltros {
    texto?: string;
    estado?: EstadoTarea;
    prioridad?: PrioridadTarea;
    desde?: string;
    hasta?: string;
    categoriaId?: number;
}

export const ESTADO_LABELS: Record<EstadoTarea, string> = {
    PENDIENTE: 'Pendiente',
    EN_PROCESO: 'En proceso',
    COMPLETADA: 'Completada'
};

export const PRIORIDAD_LABELS: Record<PrioridadTarea, string> = {
    BAJA: 'Baja',
    MEDIA: 'Media',
    ALTA: 'Alta'
};

export const RECORDATORIO_OPTIONS = [
    { value: 15, label: '15 minutos antes' },
    { value: 30, label: '30 minutos antes' },
    { value: 60, label: '1 hora antes' },
    { value: 180, label: '3 horas antes' },
    { value: 1440, label: '24 horas antes' }
] as const;
