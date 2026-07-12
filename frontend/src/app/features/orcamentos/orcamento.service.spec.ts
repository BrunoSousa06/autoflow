import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { OrcamentoService } from './orcamento.service';

const BASE = 'http://localhost:8080/orcamentos';

describe('OrcamentoService', () => {
  let service: OrcamentoService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(OrcamentoService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('deve criar o servico', () => {
    expect(service).toBeTruthy();
  });

  it('listar sem filtro deve chamar GET em /orcamentos', () => {
    service.listar().subscribe();

    const req = httpTesting.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('listar com filtro deve incluir query params nao vazios', () => {
    service.listar({ status: 'DISPONIVEL' } as any).subscribe();

    const req = httpTesting.expectOne(r => r.url === BASE);
    expect(req.request.params.get('status')).toBe('DISPONIVEL');
    req.flush([]);
  });

  it('buscarPorId deve chamar GET em /orcamentos/{id}', () => {
    service.buscarPorId(42).subscribe();

    const req = httpTesting.expectOne(`${BASE}/42`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 42 });
  });

  it('aprovar deve chamar POST em /orcamentos/{id}/aprovar', () => {
    service.aprovar(10).subscribe();

    const req = httpTesting.expectOne(`${BASE}/10/aprovar`);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 10, status: 'APROVADO' });
  });

  it('recusar com motivo deve chamar POST com body contendo motivo', () => {
    service.recusar(10, 'Preco acima do esperado').subscribe();

    const req = httpTesting.expectOne(`${BASE}/10/recusar`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ motivo: 'Preco acima do esperado' });
    req.flush({ id: 10, status: 'REPROVADO' });
  });

  it('recusar com motivo em branco deve enviar body vazio', () => {
    service.recusar(10, '   ').subscribe();

    const req = httpTesting.expectOne(`${BASE}/10/recusar`);
    expect(req.request.body).toEqual({});
    req.flush({});
  });

  it('recusar com null deve enviar body vazio', () => {
    service.recusar(10, null).subscribe();

    const req = httpTesting.expectOne(`${BASE}/10/recusar`);
    expect(req.request.body).toEqual({});
    req.flush({});
  });

  it('baixarPdf deve chamar GET em /orcamentos/{id}/pdf com responseType blob', () => {
    service.baixarPdf(5).subscribe();

    const req = httpTesting.expectOne(`${BASE}/5/pdf`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['pdf'], { type: 'application/pdf' }));
  });
});