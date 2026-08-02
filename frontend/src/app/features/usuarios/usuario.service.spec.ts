import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { UsuarioAdminService } from './usuario.service';
import { UsuarioRequest, UsuarioResponse } from './usuario.model';
import { environment } from '../../../environments/environment';

const BASE = `${environment.apiUrl}/usuarios`;

describe('UsuarioAdminService', () => {
  let service: UsuarioAdminService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(UsuarioAdminService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('deve criar o servico', () => {
    expect(service).toBeTruthy();
  });

  it('listar deve chamar GET em /usuarios', () => {
    const resposta: UsuarioResponse[] = [{ id: 1, nome: 'Joao', email: 'joao@teste.com', role: 'ADMIN' }];

    let resultado: UsuarioResponse[] | undefined;
    service.listar().subscribe((r) => (resultado = r));

    const req = httpTesting.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush(resposta);

    expect(resultado).toEqual(resposta);
  });

  it('cadastrar deve chamar POST em /usuarios com o body correto', () => {
    const body: UsuarioRequest = { nome: 'Joao', email: 'joao@teste.com', senha: '123456', role: 'MECANICO' };
    const resposta: UsuarioResponse = { id: 1, nome: 'Joao', email: 'joao@teste.com', role: 'MECANICO' };

    let resultado: UsuarioResponse | undefined;
    service.cadastrar(body).subscribe((r) => (resultado = r));

    const req = httpTesting.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush(resposta);

    expect(resultado).toEqual(resposta);
  });
});
