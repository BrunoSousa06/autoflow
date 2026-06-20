import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  VeiculoRequest,
  VeiculoUpdateRequest,
  VeiculoResponse,
  VeiculoFiltros,
  Page,
} from './veiculo.model';

@Injectable({ providedIn: 'root' })
export class VeiculoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/veiculos`;

  listar(filtros?: VeiculoFiltros, page = 0, size = 20): Observable<Page<VeiculoResponse>> {
    let params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    if (filtros?.placa) params = params.set('placa', filtros.placa);
    if (filtros?.marca) params = params.set('marca', filtros.marca);
    if (filtros?.modelo) params = params.set('modelo', filtros.modelo);
    if (filtros?.ano != null) params = params.set('ano', String(filtros.ano));
    return this.http.get<Page<VeiculoResponse>>(this.base, { params });
  }

  buscarPorId(id: number): Observable<VeiculoResponse> {
    return this.http.get<VeiculoResponse>(`${this.base}/${id}`);
  }

  cadastrar(req: VeiculoRequest): Observable<VeiculoResponse> {
    return this.http.post<VeiculoResponse>(this.base, req);
  }

  atualizar(id: number, req: VeiculoUpdateRequest): Observable<VeiculoResponse> {
    return this.http.patch<VeiculoResponse>(`${this.base}/${id}/atualizacao`, req);
  }

  // Backend retorna plain text
  deletar(id: number): Observable<string> {
    return this.http.delete(`${this.base}/${id}`, { responseType: 'text' });
  }
}
