import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AcompanhamentoOrdemServicoResponse, ClienteLogadoResponse } from './minha-conta.model';

@Injectable({ providedIn: 'root' })
export class MinhaContaService {
  private readonly http = inject(HttpClient);

  buscarPerfil(): Observable<ClienteLogadoResponse> {
    return this.http.get<ClienteLogadoResponse>(`${environment.apiUrl}/clientes/me`);
  }

  listarMinhasOrdens(): Observable<AcompanhamentoOrdemServicoResponse[]> {
    return this.http.get<AcompanhamentoOrdemServicoResponse[]>(`${environment.apiUrl}/clientes/me/ordens-servico`);
  }

  buscarMinhaOrdem(numeroOs: string): Observable<AcompanhamentoOrdemServicoResponse | null> {
    return this.listarMinhasOrdens().pipe(
      map(ordens => ordens.find(ordem => ordem.numeroOs === numeroOs) ?? null),
    );
  }
}
