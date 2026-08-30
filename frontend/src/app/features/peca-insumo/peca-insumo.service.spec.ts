import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PecaInsumoService } from './peca-insumo.service';
import { PecaInsumoRequest, PecaInsumoResponse } from './peca-insumo.model';
import { environment } from '../../../environments/environment';

const BASE = `${environment.apiUrl}/peca-insumo`;

describe('PecaInsumoService', () => {
  let service: PecaInsumoService;
  let httpTesting: HttpTestingController;

  const peca: PecaInsumoResponse = { id: 1, nome: 'Filtro', valor: 20, quantidade: 10, tipo: 'PECA' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(PecaInsumoService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('deve criar o servico', () => {
    expect(service).toBeTruthy();
  });

  it('listar sem filtros deve incluir apenas page e size', () => {
    service.listar().subscribe();

    const req = httpTesting.expectOne((r) => r.url === BASE);
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.params.has('nome')).toBeFalse();
    expect(req.request.params.has('tipo')).toBeFalse();
    req.flush({ content: [peca], page: { totalElements: 1, totalPages: 1, number: 0, size: 10 } });
  });

  it('listar com nome e tipo deve incluir os query params', () => {
    service.listar(1, 5, ' Filtro ', 'PECA').subscribe();

    const req = httpTesting.expectOne((r) => r.url === BASE);
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('5');
    expect(req.request.params.get('nome')).toBe('Filtro');
    expect(req.request.params.get('tipo')).toBe('PECA');
    req.flush({ content: [], page: { totalElements: 0, totalPages: 0, number: 1, size: 5 } });
  });

  it('buscarPorId deve chamar GET em /peca-insumo/{id}', () => {
    service.buscarPorId(1).subscribe();

    const req = httpTesting.expectOne(`${BASE}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(peca);
  });

  it('cadastrar deve chamar POST em /peca-insumo com o body correto', () => {
    const body: PecaInsumoRequest = { nome: 'Filtro', valor: 20, quantidade: 10, tipo: 'PECA' };

    service.cadastrar(body).subscribe();

    const req = httpTesting.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush(peca);
  });

  it('atualizar deve chamar PATCH em /peca-insumo/{id}/atualizacao', () => {
    const body: PecaInsumoRequest = { nome: 'Filtro', valor: 25, quantidade: 8, tipo: 'PECA' };

    service.atualizar(1, body).subscribe();

    const req = httpTesting.expectOne(`${BASE}/1/atualizacao`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(body);
    req.flush(peca);
  });
});
