import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { UsuarioService, UsuarioResponse } from './usuario.service';
import { environment } from '../../../environments/environment';

const BASE = `${environment.apiUrl}/auth`;

describe('UsuarioService', () => {
  let service: UsuarioService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(UsuarioService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('deve criar o servico', () => {
    expect(service).toBeTruthy();
  });

  it('listarMecanicos deve chamar GET em /auth/mecanicos', () => {
    const resposta: UsuarioResponse[] = [{ id: 1, nome: 'Joao', email: 'joao@teste.com', role: 'MECANICO' }];

    let resultado: UsuarioResponse[] | undefined;
    service.listarMecanicos().subscribe((r) => (resultado = r));

    const req = httpTesting.expectOne(`${BASE}/mecanicos`);
    expect(req.request.method).toBe('GET');
    req.flush(resposta);

    expect(resultado).toEqual(resposta);
  });
});
