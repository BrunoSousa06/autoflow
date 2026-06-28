import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, map, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

interface LoginResponse {
  token: string;
}

interface JwtPayload {
  sub: string;
  role: string;
  iat: number;
  exp: number;
}

export interface UsuarioLogado {
  email: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'autoflow_token';
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly tokenSubject = new BehaviorSubject<string | null>(
    localStorage.getItem(this.TOKEN_KEY)
  );

  readonly token$ = this.tokenSubject.asObservable();
  readonly isLoggedIn$ = this.token$.pipe(map(t => !!t));

  login(email: string, senha: string): Observable<void> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, { email, senha })
      .pipe(
        tap(res => this.salvarToken(res.token)),
        map(() => void 0)
      );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.tokenSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.tokenSubject.value;
  }

  getUsuarioLogado(): UsuarioLogado | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = this.decodificarToken(token);
      if (Date.now() >= payload.exp * 1000) {
        this.logout();
        return null;
      }
      return { email: payload.sub, role: payload.role };
    } catch {
      return null;
    }
  }

  getRole(): string | null {
    return this.getUsuarioLogado()?.role ?? null;
  }

  isLoggedIn(): boolean {
    return this.getUsuarioLogado() !== null;
  }

  private salvarToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    this.tokenSubject.next(token);
  }

  private decodificarToken(token: string): JwtPayload {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload.replaceAll('-', '+').replaceAll('_', '/')));
  }
}
