import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UsuarioResponse {
  id: number;
  nome: string;
  email: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/auth`;

  listarMecanicos(): Observable<UsuarioResponse[]> {
    return this.http.get<UsuarioResponse[]>(`${this.base}/mecanicos`);
  }
}
