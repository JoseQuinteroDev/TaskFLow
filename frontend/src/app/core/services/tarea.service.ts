import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
    TareaCreateRequest,
    TareaFiltros,
    TareaResponse,
    TareaResumen,
    TareaUpdateRequest
} from '../models/tarea.model';

@Injectable({
    providedIn: 'root'
})
export class TareaService {
    private readonly apiUrl = 'http://localhost:8080/api/tareas';

    constructor(private http: HttpClient) {}

    getAll(): Observable<TareaResponse[]> {
        return this.http.get<TareaResponse[]>(this.apiUrl);
    }

    getById(id: number): Observable<TareaResponse> {
        return this.http.get<TareaResponse>(`${this.apiUrl}/${id}`);
    }

    create(payload: TareaCreateRequest): Observable<TareaResponse> {
        return this.http.post<TareaResponse>(this.apiUrl, payload);
    }

    update(id: number, payload: TareaUpdateRequest): Observable<TareaResponse> {
        return this.http.put<TareaResponse>(`${this.apiUrl}/${id}`, payload);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    completar(id: number): Observable<TareaResponse> {
        return this.http.patch<TareaResponse>(`${this.apiUrl}/${id}/completar`, {});
    }

    getResumen(): Observable<TareaResumen> {
        return this.http.get<TareaResumen>(`${this.apiUrl}/resumen`);
    }

    filtrar(filtros: TareaFiltros): Observable<TareaResponse[]> {
        let params = new HttpParams();

        Object.entries(filtros).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                params = params.set(key, String(value));
            }
        });

        return this.http.get<TareaResponse[]>(`${this.apiUrl}/filtro`, { params });
    }
}