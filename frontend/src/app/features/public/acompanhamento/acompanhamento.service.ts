import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { StatusOrdemServico } from '../../ordens-servico/ordem-servico.model';

export interface AcompanhamentoPublico {
  numeroOs: string;
  status: StatusOrdemServico;
  dataAbertura: string;
  execucaoIniciadaEm: string | null;
  finalizadaEm: string | null;
  entregueEm: string | null;
  orcamentoId?: number | null;
}

@Injectable({ providedIn: 'root' })
export class AcompanhamentoService {
  constructor(private readonly http: HttpClient) {}

  consultar(token: string): Observable<AcompanhamentoPublico> {
    return this.http.get<AcompanhamentoPublico>(
      `${environment.apiUrl}/public/ordens-servico/acompanhamento`,
      { params: { token } }
    );
  }

  baixarOrcamento(orcamentoId: number, token: string): Observable<Blob> {
    return this.http.get(
      `${environment.apiUrl}/public/orcamentos/${orcamentoId}/pdf/acompanhamento`,
      { params: { token }, responseType: 'blob' }
    );
  }

  aprovarOrcamento(orcamentoId: number, token: string): Observable<unknown> {
    return this.http.post(
      `${environment.apiUrl}/public/orcamentos/${orcamentoId}/aprovar/acompanhamento`,
      {},
      { params: { token } }
    );
  }
}
