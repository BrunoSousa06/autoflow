import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DashboardService, TempoMedioOsResponse, TempoMedioServicoResponse } from './dashboard.service';

const API = 'http://localhost:8080';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(DashboardService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('deve criar o servico', () => {
    expect(service).toBeTruthy();
  });

  it('getTempoMedioOs deve chamar GET em /ordens-servico/metricas/tempo-medio', () => {
    const resposta: TempoMedioOsResponse = {
      quantidadeOrdensFinalizadas: 10,
      tempoMedioSegundos: 3600,
      tempoMedioMinutos: 60,
      tempoMedioHoras: 1,
    };

    let resultado: TempoMedioOsResponse | undefined;
    service.getTempoMedioOs().subscribe((r) => (resultado = r));

    const req = httpTesting.expectOne(`${API}/ordens-servico/metricas/tempo-medio`);
    expect(req.request.method).toBe('GET');
    req.flush(resposta);

    expect(resultado).toEqual(resposta);
  });

  it('getTempoMedioPorServico deve chamar GET em /servicos/metricas/tempo-medio', () => {
    const resposta: TempoMedioServicoResponse[] = [{
      servicoId: 1, nomeServico: 'Troca de oleo', quantidadeExecucoes: 5,
      tempoMedioSegundos: 1800, tempoMedioMinutos: 30, tempoMedioHoras: 0.5,
    }];

    let resultado: TempoMedioServicoResponse[] | undefined;
    service.getTempoMedioPorServico().subscribe((r) => (resultado = r));

    const req = httpTesting.expectOne(`${API}/servicos/metricas/tempo-medio`);
    expect(req.request.method).toBe('GET');
    req.flush(resposta);

    expect(resultado).toEqual(resposta);
  });
});
