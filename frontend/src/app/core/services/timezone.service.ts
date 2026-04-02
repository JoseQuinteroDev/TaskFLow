import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TimezoneService {
  detect(): string {
    try {
      return Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC';
    } catch {
      return 'UTC';
    }
  }

  toUtcIso(localDateTime: string | null | undefined): string | undefined {
    if (!localDateTime) {
      return undefined;
    }

    const [datePart, timePart] = localDateTime.split('T');
    if (!datePart || !timePart) {
      return undefined;
    }

    const [year, month, day] = datePart.split('-').map(Number);
    const [hours, minutes] = timePart.split(':').map(Number);
    const date = new Date(year, month - 1, day, hours, minutes, 0, 0);

    return Number.isNaN(date.getTime()) ? undefined : date.toISOString();
  }

  fromUtcIsoToLocalInput(utcDateTime: string | null | undefined): string {
    if (!utcDateTime) {
      return '';
    }

    const date = new Date(utcDateTime);
    if (Number.isNaN(date.getTime())) {
      return '';
    }

    const year = date.getFullYear();
    const month = `${date.getMonth() + 1}`.padStart(2, '0');
    const day = `${date.getDate()}`.padStart(2, '0');
    const hours = `${date.getHours()}`.padStart(2, '0');
    const minutes = `${date.getMinutes()}`.padStart(2, '0');

    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  localNowInputValue(): string {
    return this.fromUtcIsoToLocalInput(new Date().toISOString());
  }

  format(
    utcDateTime: string | null | undefined,
    options: Intl.DateTimeFormatOptions
  ): string {
    if (!utcDateTime) {
      return '';
    }

    const date = new Date(utcDateTime);
    if (Number.isNaN(date.getTime())) {
      return '';
    }

    return new Intl.DateTimeFormat('es-ES', options).format(date);
  }
}
