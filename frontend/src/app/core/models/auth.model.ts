export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    nombre: string;
    email: string;
    password: string;
}

export interface AuthResponse {
    token: string;
    tipoToken: string;
    email: string;
    roles: string[];
}