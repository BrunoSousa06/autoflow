import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CriarOrdemServicoRequest,
  FinalizarDiagnosticoResponse,
  IncluirMecanicoRequest,
  ItensNecessariosRequest,
  OrdemServicoDetalheResponse,
  OrdemServicoFiltro,
  OrdemServicoResponse,
  Page,
  RegistrarLaudoRequest,
  ServicoSolicitadoRequest,
} from './ordem-servico.model';
import { SUPPRESS_GLOBAL_ERROR_SNACKBAR } from '../../core/interceptors/error.interceptor';

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

  buscarPorNumeroOs(numeroOs: string): Observable<OrdemServicoDetalheResponse> {
    return this.http.get<OrdemServicoDetalheResponse>(`${this.base}/${numeroOs}`);
  }

  atribuirMecanico(numeroOs: string, req: IncluirMecanicoRequest): Observable<OrdemServicoResponse> {
    return this.http.patch<OrdemServicoResponse>(`${this.base}/${numeroOs}/mecanico`, req);
  }

  entregar(numeroOs: string): Observable<OrdemServicoResponse> {
    return this.http.patch<OrdemServicoResponse>(`${this.base}/${numeroOs}/entregar`, {});
  }

  iniciarDiagnostico(numeroOs: string): Observable<OrdemServicoResponse> {
    return this.http.patch<OrdemServicoResponse>(
      `${this.base}/${numeroOs}/diagnostico/iniciar`,
      {},
      { context: this.suppressGlobalErrorSnackbarContext() },
    );
  }

  registrarLaudo(numeroOs: string, req: RegistrarLaudoRequest): Observable<OrdemServicoResponse> {
    return this.http.patch<OrdemServicoResponse>(
      `${this.base}/${numeroOs}/diagnostico/laudo`,
      req,
      { context: this.suppressGlobalErrorSnackbarContext() },
    );
  }

  registrarItensServico(
    numeroOs: string,
    servicoId: number,
    itens: ItensNecessariosRequest[],
  ): Observable<OrdemServicoResponse> {
    return this.http.patch<OrdemServicoResponse>(
      `${this.base}/${numeroOs}/servicos/${servicoId}/itens-necessarios`,
      itens,
      { context: this.suppressGlobalErrorSnackbarContext() },
    );
  }

  finalizarDiagnostico(numeroOs: string): Observable<FinalizarDiagnosticoResponse> {
    return this.http.patch<FinalizarDiagnosticoResponse>(
      `${this.base}/${numeroOs}/diagnostico/finalizar`,
      {},
      { context: this.suppressGlobalErrorSnackbarContext() },
    );
  }

  incluirServicos(numeroOs: string, servicos: ServicoSolicitadoRequest[]): Observable<OrdemServicoResponse> {
    return this.http.post<OrdemServicoResponse>(
      `${this.base}/${numeroOs}/servicos`,
      servicos,
      { context: this.suppressGlobalErrorSnackbarContext() },
    );
  }

  criar(req: CriarOrdemServicoRequest): Observable<OrdemServicoResponse> {
    return this.http.post<OrdemServicoResponse>(this.base, req);
  }

  private suppressGlobalErrorSnackbarContext(): HttpContext {
    return new HttpContext().set(SUPPRESS_GLOBAL_ERROR_SNACKBAR, true);
  }
}
