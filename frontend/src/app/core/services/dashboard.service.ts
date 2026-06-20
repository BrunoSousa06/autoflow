import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface TempoMedioOsResponse {
  quantidadeOrdensFinalizadas: number;
  tempoMedioSegundos: number;
  tempoMedioMinutos: number;
  tempoMedioHoras: number;
}

export interface TempoMedioServicoResponse {
  servicoId: number;
  nomeServico: string;
  quantidadeExecucoes: number;
  tempoMedioSegundos: number;
  tempoMedioMinutos: number;
  tempoMedioHoras: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  getTempoMedioOs(): Observable<TempoMedioOsResponse> {
    return this.http.get<TempoMedioOsResponse>(
      `${this.api}/ordens-servico/metricas/tempo-medio`
    );
  }

  getTempoMedioPorServico(): Observable<TempoMedioServicoResponse[]> {
    return this.http.get<TempoMedioServicoResponse[]>(
      `${this.api}/servicos/metricas/tempo-medio`
    );
  }
}
