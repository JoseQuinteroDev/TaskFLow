import { CategoriaResponse } from './categoria.model';

export type EstadoTarea = 'PENDIENTE' | 'EN_PROCESO' | 'COMPLETADA';
export type PrioridadTarea = 'BAJA' | 'MEDIA' | 'ALTA';

export interface TareaCreateRequest {
    titulo: string;
    descripcion?: string;
    prioridad: PrioridadTarea;
    fechaInicio: string;
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
    fechaInicio: string;
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
    fechaInicio: string;
    fechaLimite?: string;
    vencida: boolean;
    completada: boolean;
    recordatorioActivo: boolean;
    recordatorioMinutosAntes?: number;
    fechaCreacion: string;
    fechaActualizacion: string;
    categoria?: CategoriaResponse;
}

export interface TareaResumen {
    total: number;
    pendientes: number;
    enProceso: number;
    completadas: number;
    vencidas: number;
}

export interface TareaFiltros {
    texto?: string;
    estado?: EstadoTarea;
    prioridad?: PrioridadTarea;
    inicioDesde?: string;
    inicioHasta?: string;
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
    { value: 720, label: '12 horas antes' },
    { value: 1440, label: '24 horas antes' },
    { value: 2880, label: '2 días antes' },
    { value: 4320, label: '3 días antes' },
    { value: 10080, label: '7 días antes' }
] as const;
