import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

export type StatusOrcamentoPublico = 'DISPONIVEL' | 'APROVADO' | 'REPROVADO' | 'SUBSTITUIDO';

export interface OrcamentoPublico {
  id: number;
  numeroOs: string;
  tipo: string;
  versao: number;
  status: StatusOrcamentoPublico;
  totalServicos: number;
  totalItens: number;
  totalGeral: number;
  criadoEm: string;
  disponibilizadoEm: string | null;
}

@Injectable({providedIn: 'root'})
export class OrcamentoPublicoService {
  constructor(private readonly http: HttpClient) {
  }

  consultar(id: number, token: string): Observable<OrcamentoPublico> {
    return this.http.get<OrcamentoPublico>(`${environment.apiUrl}/public/orcamentos/${id}`, {params: {token}});
  }

  baixarPdf(id: number, token: string): Observable<Blob> {
    return this.http.get(`${environment.apiUrl}/public/orcamentos/${id}/pdf`, {
      params: {token},
      responseType: 'blob',
    });
  }

  aprovar(id: number, token: string, nome: string): Observable<OrcamentoPublico> {
    const body = nome.trim() ? {nome: nome.trim()} : {};
    return this.http.post<OrcamentoPublico>(`${environment.apiUrl}/public/orcamentos/${id}/aprovar`, body, {params: {token}});
  }

  recusar(id: number, token: string, motivo: string, nome: string): Observable<OrcamentoPublico> {
    const body: { motivo?: string; nome?: string } = {};
    if (motivo.trim()) body.motivo = motivo.trim();
    if (nome.trim()) body.nome = nome.trim();
    return this.http.post<OrcamentoPublico>(`${environment.apiUrl}/public/orcamentos/${id}/recusar`, body, {params: {token}});
  }
}
