import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CategoriaRequest, CategoriaResponse } from '../models/categoria.model';

@Injectable({
    providedIn: 'root'
})
export class CategoriaService {
    private readonly apiUrl = 'http://localhost:8080/api/categorias';

    constructor(private http: HttpClient) {}

    getAll(): Observable<CategoriaResponse[]> {
        return this.http.get<CategoriaResponse[]>(this.apiUrl);
    }

    create(payload: CategoriaRequest): Observable<CategoriaResponse> {
        return this.http.post<CategoriaResponse>(this.apiUrl, payload);
    }

    update(id: number, payload: CategoriaRequest): Observable<CategoriaResponse> {
        return this.http.put<CategoriaResponse>(`${this.apiUrl}/${id}`, payload);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}