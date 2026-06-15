import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page, PecaInsumoRequest, PecaInsumoResponse } from './peca-insumo.model';

@Injectable({ providedIn: 'root' })
export class PecaInsumoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/peca-insumo`;

  listar(page = 0, size = 10): Observable<Page<PecaInsumoResponse>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<Page<PecaInsumoResponse>>(this.base, { params });
  }

  buscarPorId(id: number): Observable<PecaInsumoResponse> {
    return this.http.get<PecaInsumoResponse>(`${this.base}/${id}`);
  }

  cadastrar(req: PecaInsumoRequest): Observable<PecaInsumoResponse> {
    return this.http.post<PecaInsumoResponse>(this.base, req);
  }

  atualizar(id: number, req: PecaInsumoRequest): Observable<PecaInsumoResponse> {
    return this.http.patch<PecaInsumoResponse>(`${this.base}/${id}/atualizacao`, req);
  }
}
