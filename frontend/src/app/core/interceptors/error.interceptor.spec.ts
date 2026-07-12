import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  HttpContext,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../services/auth.service';
import { errorInterceptor, SUPPRESS_GLOBAL_ERROR_SNACKBAR } from './error.interceptor';

describe('errorInterceptor', () => {
  let httpClient: HttpClient;
  let httpTesting: HttpTestingController;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
  let router: Router;

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['logout']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: mockAuthService },
        { provide: MatSnackBar, useValue: mockSnackBar },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
  });

  afterEach(() => httpTesting.verify());

  it('deve repassar o erro sem tratamento especial em requisicoes de /auth/login', () => {
    let erroCapturado: any;

    httpClient.post('/auth/login', {}).subscribe({
      error: (err) => (erroCapturado = err),
    });

    httpTesting.expectOne('/auth/login').flush('Credenciais invalidas', { status: 401, statusText: 'Unauthorized' });

    expect(erroCapturado.status).toBe(401);
    expect(mockAuthService.logout).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('deve fazer logout e redirecionar para /login em erro 401', () => {
    httpClient.get('/api/ordens-servico').subscribe({ error: () => {} });

    httpTesting.expectOne('/api/ordens-servico').flush('Nao autorizado', { status: 401, statusText: 'Unauthorized' });

    expect(mockAuthService.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('deve exibir snackbar de permissao em erro 403 quando nao suprimido', () => {
    httpClient.get('/api/ordens-servico').subscribe({ error: () => {} });

    httpTesting.expectOne('/api/ordens-servico').flush('Proibido', { status: 403, statusText: 'Forbidden' });

    expect(mockSnackBar.open).toHaveBeenCalledWith(
      'Sem permissão para acessar este recurso.',
      'Fechar',
      { duration: 4000 }
    );
  });

  it('nao deve exibir snackbar em erro 403 quando contexto de supressao esta ativo', () => {
    const context = new HttpContext().set(SUPPRESS_GLOBAL_ERROR_SNACKBAR, true);

    httpClient.get('/api/ordens-servico', { context }).subscribe({ error: () => {} });

    httpTesting.expectOne('/api/ordens-servico').flush('Proibido', { status: 403, statusText: 'Forbidden' });

    expect(mockSnackBar.open).not.toHaveBeenCalled();
  });

  it('deve exibir snackbar de erro interno em respostas 5xx', () => {
    httpClient.get('/api/ordens-servico').subscribe({ error: () => {} });

    httpTesting.expectOne('/api/ordens-servico').flush('Erro', { status: 500, statusText: 'Internal Server Error' });

    expect(mockSnackBar.open).toHaveBeenCalledWith(
      'Erro interno do servidor. Tente novamente.',
      'Fechar',
      { duration: 4000 }
    );
  });

  it('deve exibir snackbar de erro interno quando status e 0 (falha de rede)', () => {
    httpClient.get('/api/ordens-servico').subscribe({ error: () => {} });

    httpTesting.expectOne('/api/ordens-servico').error(new ProgressEvent('erro de rede'), { status: 0 });

    expect(mockSnackBar.open).toHaveBeenCalledWith(
      'Erro interno do servidor. Tente novamente.',
      'Fechar',
      { duration: 4000 }
    );
  });

  it('nao deve exibir snackbar para outros codigos de erro (ex: 404)', () => {
    httpClient.get('/api/ordens-servico').subscribe({ error: () => {} });

    httpTesting.expectOne('/api/ordens-servico').flush('Nao encontrado', { status: 404, statusText: 'Not Found' });

    expect(mockSnackBar.open).not.toHaveBeenCalled();
    expect(mockAuthService.logout).not.toHaveBeenCalled();
  });
});