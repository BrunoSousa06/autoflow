import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { OrcamentoFiltro, OrcamentoResponse, RecusarOrcamentoRequest } from './orcamento.model';

@Injectable({ providedIn: 'root' })
export class OrcamentoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/orcamentos`;

  listar(filtro: OrcamentoFiltro = {}): Observable<OrcamentoResponse[]> {
    let params = new HttpParams();

    Object.entries(filtro).forEach(([chave, valor]) => {
      const texto = typeof valor === 'string' ? valor.trim() : valor;
      if (texto) {
        params = params.set(chave, texto);
      }
    });

    return this.http.get<OrcamentoResponse[]>(this.baseUrl, { params });
  }

  buscarPorId(orcamentoId: number): Observable<OrcamentoResponse> {
    return this.http.get<OrcamentoResponse>(`${this.baseUrl}/${orcamentoId}`);
  }

  aprovar(orcamentoId: number): Observable<OrcamentoResponse> {
    return this.http.post<OrcamentoResponse>(`${this.baseUrl}/${orcamentoId}/aprovar`, {});
  }

  recusar(orcamentoId: number, motivo?: string | null): Observable<OrcamentoResponse> {
    const body: RecusarOrcamentoRequest = motivo?.trim() ? { motivo: motivo.trim() } : {};
    return this.http.post<OrcamentoResponse>(`${this.baseUrl}/${orcamentoId}/recusar`, body);
  }
}
