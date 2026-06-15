import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CriarOrdemServicoRequest, OrdemServicoFiltro, OrdemServicoResponse, Page } from './ordem-servico.model';

@Injectable({ providedIn: 'root' })
export class OrdemServicoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/ordens-servico`;

  listar(filtro: OrdemServicoFiltro = {}): Observable<Page<OrdemServicoResponse>> {
    let params = new HttpParams();
    if (filtro.cliente) params = params.set('cliente', filtro.cliente);
    if (filtro.numeroOs) params = params.set('numeroOs', filtro.numeroOs);
    if (filtro.status) params = params.set('status', filtro.status);
    if (filtro.page !== undefined) params = params.set('page', String(filtro.page));
    if (filtro.size !== undefined) params = params.set('size', String(filtro.size));
    return this.http.get<Page<OrdemServicoResponse>>(this.base, { params });
  }

  criar(req: CriarOrdemServicoRequest): Observable<OrdemServicoResponse> {
    return this.http.post<OrdemServicoResponse>(this.base, req);
  }
}
