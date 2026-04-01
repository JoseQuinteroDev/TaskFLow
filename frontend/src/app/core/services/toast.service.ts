import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
    id: number;
    type: ToastType;
    title: string;
    message?: string;
    leaving?: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class ToastService {
    private counter = 0;
    readonly toasts = signal<Toast[]>([]);

    success(title: string, message?: string): void {
        this.show('success', title, message);
    }

    error(title: string, message?: string): void {
        this.show('error', title, message);
    }

    warning(title: string, message?: string): void {
        this.show('warning', title, message);
    }

    info(title: string, message?: string): void {
        this.show('info', title, message);
    }

    dismiss(id: number): void {
        this.toasts.update(items =>
            items.map(t => t.id === id ? { ...t, leaving: true } : t)
        );

        setTimeout(() => {
            this.toasts.update(items => items.filter(t => t.id !== id));
        }, 220);
    }

    private show(type: ToastType, title: string, message?: string): void {
        const toast: Toast = {
            id: ++this.counter,
            type,
            title,
            message
        };

        this.toasts.update(items => [...items, toast]);

        setTimeout(() => this.dismiss(toast.id), 3500);
    }
}