import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  AdminCreateUserRequest,
  AdminDashboardSummary,
  AdminReminderFailure,
  AdminUser,
  AdminUserFilters,
  PageResponse
} from '../models/admin.model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  getUsers(filters: AdminUserFilters): Observable<PageResponse<AdminUser>> {
    let params = new HttpParams();

    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    });

    return this.http.get<PageResponse<AdminUser>>(`${this.apiUrl}/users`, { params });
  }

  createUser(payload: AdminCreateUserRequest): Observable<AdminUser> {
    return this.http.post<AdminUser>(`${this.apiUrl}/users`, payload);
  }

  updateUserStatus(userId: number, activo: boolean): Observable<AdminUser> {
    return this.http.patch<AdminUser>(`${this.apiUrl}/users/${userId}/status`, { activo });
  }

  updateUserRole(userId: number, admin: boolean): Observable<AdminUser> {
    return this.http.patch<AdminUser>(`${this.apiUrl}/users/${userId}/roles`, { admin });
  }

  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${userId}`);
  }

  getDashboardSummary(): Observable<AdminDashboardSummary> {
    return this.http.get<AdminDashboardSummary>(`${this.apiUrl}/dashboard/summary`);
  }

  getReminderFailures(limit = 8): Observable<AdminReminderFailure[]> {
    return this.http.get<AdminReminderFailure[]>(`${this.apiUrl}/reminders/failures`, {
      params: new HttpParams().set('limit', String(limit))
    });
  }
}
