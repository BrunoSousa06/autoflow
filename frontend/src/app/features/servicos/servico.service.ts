import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page, ServicoRequest, ServicoResponse } from './servico.model';

@Injectable({ providedIn: 'root' })
export class ServicoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/servicos`;

  listar(page = 0, size = 20): Observable<Page<ServicoResponse>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<Page<ServicoResponse>>(this.base, { params });
  }

  buscarPorId(id: number): Observable<ServicoResponse> {
    return this.http.get<ServicoResponse>(`${this.base}/${id}`);
  }

  cadastrar(req: ServicoRequest): Observable<ServicoResponse> {
    return this.http.post<ServicoResponse>(this.base, req);
  }

  atualizar(id: number, req: ServicoRequest): Observable<ServicoResponse> {
    return this.http.patch<ServicoResponse>(`${this.base}/${id}/atualizacao`, req);
  }

  deletar(id: number): Observable<string> {
    return this.http.delete(`${this.base}/${id}`, { responseType: 'text' });
  }
}
