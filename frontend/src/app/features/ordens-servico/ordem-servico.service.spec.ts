import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { OrdemServicoService } from './ordem-servico.service';
import { CriarOrdemServicoRequest, IncluirMecanicoRequest, ItensNecessariosRequest, RegistrarLaudoRequest } from './ordem-servico.model';
import { environment } from '../../../environments/environment';

const BASE = `${environment.apiUrl}/ordens-servico`;

describe('OrdemServicoService', () => {
  let service: OrdemServicoService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(OrdemServicoService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('deve criar o servico', () => {
    expect(service).toBeTruthy();
  });

  it('listar sem filtro deve chamar GET em /ordens-servico sem params extras', () => {
    service.listar().subscribe();

    const req = httpTesting.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], page: { totalElements: 0, totalPages: 0, number: 0, size: 10 } });
  });

  it('listar com filtro deve incluir os query params fornecidos', () => {
    service.listar({ status: 'RECEBIDA', page: 0, size: 20 }).subscribe();

    const req = httpTesting.expectOne(r => r.url === BASE);
    expect(req.request.params.get('status')).toBe('RECEBIDA');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('20');
    req.flush({ content: [], page: { totalElements: 0, totalPages: 0, number: 0, size: 20 } });
  });

  it('buscarPorNumeroOs deve chamar GET em /ordens-servico/{numeroOs}', () => {
    service.buscarPorNumeroOs('OS-001').subscribe();

    const req = httpTesting.expectOne(`${BASE}/OS-001`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 1, numeroOs: 'OS-001' });
  });

  it('criar deve chamar POST em /ordens-servico com o body correto', () => {
    const body: CriarOrdemServicoRequest = {
      cpfCnpj: '123.456.789-00',
      veiculo: { placa: 'ABC1D23' },
      servicosSolicitados: [{ servicoId: 1 }]
    };

    service.criar(body).subscribe();

    const req = httpTesting.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({ id: 1, numeroOs: 'OS-001' });
  });

  it('atribuirMecanico deve chamar PATCH em /ordens-servico/{numeroOs}/mecanico', () => {
    const body: IncluirMecanicoRequest = { mecanicoId: 5 };

    service.atribuirMecanico('OS-001', body).subscribe();

    const req = httpTesting.expectOne(`${BASE}/OS-001/mecanico`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('entregar deve chamar PATCH em /ordens-servico/{numeroOs}/entregar', () => {
    service.entregar('OS-002').subscribe();

    const req = httpTesting.expectOne(`${BASE}/OS-002/entregar`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('iniciarDiagnostico deve chamar PATCH em /ordens-servico/{numeroOs}/diagnostico/iniciar', () => {
    service.iniciarDiagnostico('OS-003').subscribe();

    const req = httpTesting.expectOne(`${BASE}/OS-003/diagnostico/iniciar`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('registrarLaudo deve chamar PATCH em /ordens-servico/{numeroOs}/diagnostico/laudo', () => {
    const body: RegistrarLaudoRequest = { laudo: 'Correia dentada desgastada' };

    service.registrarLaudo('OS-003', body).subscribe();

    const req = httpTesting.expectOne(`${BASE}/OS-003/diagnostico/laudo`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('finalizarDiagnostico deve chamar PATCH em /ordens-servico/{numeroOs}/diagnostico/finalizar', () => {
    service.finalizarDiagnostico('OS-003').subscribe();

    const req = httpTesting.expectOne(`${BASE}/OS-003/diagnostico/finalizar`);
    expect(req.request.method).toBe('PATCH');
    req.flush({ ordemServico: {}, orcamentoId: 1, publicUrl: 'http://example.com' });
  });

  it('incluirServicos deve chamar POST em /ordens-servico/{numeroOs}/servicos', () => {
    service.incluirServicos('OS-001', [{ servicoId: 2 }]).subscribe();

    const req = httpTesting.expectOne(`${BASE}/OS-001/servicos`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual([{ servicoId: 2 }]);
    req.flush({});
  });

  it('registrarItensServico deve chamar PATCH em /ordens-servico/{numeroOs}/servicos/{servicoId}/itens-necessarios', () => {
    const itens: ItensNecessariosRequest[] = [{ pecaInsumoId: 10, quantidade: 2 }];

    service.registrarItensServico('OS-001', 3, itens).subscribe();

    const req = httpTesting.expectOne(`${BASE}/OS-001/servicos/3/itens-necessarios`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(itens);
    req.flush({});
  });
});
