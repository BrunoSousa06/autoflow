import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export interface VeiculoRequest {
  cpfCnpj: string;
  marca: string;
  ano: number;
  placa: string;
  modelo: string;
}

export interface VeiculoUpdateRequest {
  marca: string;
  ano: number;
  placa: string;
  modelo: string;
}

export interface ClienteResumo {
  id: number;
  nome: string;
  cpfCnpj: string;
  telefone: string;
  email: string;
}

export interface VeiculoResponse {
  id: number;
  marca: string;
  ano: number;
  placa: string;
  modelo: string;
  cliente: ClienteResumo;
}

export interface VeiculoFiltros {
  placa?: string;
  marca?: string;
  modelo?: string;
  ano?: number | null;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export function placaValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const v = (control.value ?? '').toUpperCase().replace(/[^A-Z0-9]/g, '');
    if (!v) return null;
    const valido =
      /^[A-Z]{3}\d{4}$/.test(v) || /^[A-Z]{3}\d[A-Z]\d{2}$/.test(v);
    return valido ? null : { placa: 'Use o formato ABC1234 ou ABC1D23' };
  };
}

export function normalizarPlaca(placa: string): string {
  return (placa ?? '').toUpperCase().replace(/[^A-Z0-9]/g, '');
}
