export type RoleEnum = 'ADMIN' | 'ATENDENTE' | 'MECANICO' | 'CLIENTE';

export interface UsuarioRequest {
  nome: string;
  email: string;
  cpfCnpj?: string;
  telefone?: string;
  senha: string;
  role: RoleEnum;
}

export interface UsuarioResponse {
  id: number;
  nome: string;
  email: string;
  role: RoleEnum;
}

export const ROLE_LABELS: Record<string, string> = {
  ADMIN: 'Administrador',
  ATENDENTE: 'Atendente',
  MECANICO: 'Mecânico',
  CLIENTE: 'Cliente',
};

export const ROLE_COLORS: Record<string, { bg: string; text: string }> = {
  ADMIN:     { bg: '#e3f2fd', text: '#1565c0' },
  ATENDENTE: { bg: '#e8f5e9', text: '#2e7d32' },
  MECANICO:  { bg: '#fff3e0', text: '#e65100' },
  CLIENTE:   { bg: '#f5f5f5', text: '#616161' },
};
