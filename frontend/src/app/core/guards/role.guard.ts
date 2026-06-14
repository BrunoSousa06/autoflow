import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

const ROLE_HOME: Record<string, string> = {
  CLIENTE: '/minha-conta/minhas-ordens',
  ADMIN: '/dashboard',
  ATENDENTE: '/dashboard',
  MECANICO: '/dashboard',
};

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const role = auth.getRole();

    if (role && allowedRoles.includes(role)) return true;

    const home = role ? (ROLE_HOME[role] ?? '/login') : '/login';
    return router.createUrlTree([home]);
  };
};
