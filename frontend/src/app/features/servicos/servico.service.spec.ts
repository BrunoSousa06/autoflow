import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ServicoService } from './servico.service';
import { ServicoRequest, ServicoResponse } from './servico.model';
import { environment } from '../../../environments/environment';

const BASE = `${environment.apiUrl}/servicos`;

describe('ServicoService', () => {
  let service: ServicoService;
  let httpTesting: HttpTestingController;

  const servico: ServicoResponse = { id: 1, nome: 'Troca de oleo', descricao: 'Troca de oleo do motor', valor: 100, ativo: true };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(ServicoService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('deve criar o servico', () => {
    expect(service).toBeTruthy();
  });

  it('listar deve incluir page e size como query params', () => {
    service.listar(1, 50).subscribe();

    const req = httpTesting.expectOne((r) => r.url === BASE);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('50');
    req.flush({ content: [servico], page: { totalElements: 1, totalPages: 1, number: 1, size: 50 } });
  });

  it('buscarPorId deve chamar GET em /servicos/{id}', () => {
    service.buscarPorId(1).subscribe();

    const req = httpTesting.expectOne(`${BASE}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(servico);
  });

  it('cadastrar deve chamar POST em /servicos com o body correto', () => {
    const body: ServicoRequest = { nome: 'Troca de oleo', descricao: 'Troca de oleo do motor', valor: 100 };

    service.cadastrar(body).subscribe();

    const req = httpTesting.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush(servico);
  });

  it('atualizar deve chamar PATCH em /servicos/{id}/atualizacao', () => {
    const body: ServicoRequest = { nome: 'Troca de oleo', descricao: 'Atualizado', valor: 120 };

    service.atualizar(1, body).subscribe();

    const req = httpTesting.expectOne(`${BASE}/1/atualizacao`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(body);
    req.flush(servico);
  });

  it('deletar deve chamar DELETE em /servicos/{id} esperando resposta texto', () => {
    let resultado: string | undefined;
    service.deletar(1).subscribe((r) => (resultado = r));

    const req = httpTesting.expectOne(`${BASE}/1`);
    expect(req.request.method).toBe('DELETE');
    expect(req.request.responseType).toBe('text');
    req.flush('Servico removido');

    expect(resultado).toBe('Servico removido');
  });
});
