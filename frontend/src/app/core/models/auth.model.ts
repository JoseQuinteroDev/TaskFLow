export interface LoginRequest {
    email: string;
    password: string;
    timezone: string;
}

export interface RegisterRequest {
    nombre: string;
    email: string;
    password: string;
    timezone: string;
}

export interface AuthResponse {
    token: string;
    tipoToken: string;
    nombre: string;
    email: string;
    timezone: string;
    roles: string[];
}
