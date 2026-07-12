import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from '../services/auth.service';
import { jwtInterceptor } from './jwt.interceptor';

describe('jwtInterceptor', () => {
  let httpClient: HttpClient;
  let httpTesting: HttpTestingController;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['getToken']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: mockAuthService }
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('deve adicionar header Authorization em requisicoes protegidas com token disponivel', () => {
    mockAuthService.getToken.and.returnValue('token-jwt-valido');

    httpClient.get('/api/ordens-servico').subscribe();

    const req = httpTesting.expectOne('/api/ordens-servico');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-jwt-valido');
    req.flush({});
  });

  it('nao deve adicionar header em rota publica /auth/login', () => {
    mockAuthService.getToken.and.returnValue('token-jwt-valido');

    httpClient.get('/auth/login').subscribe();

    const req = httpTesting.expectOne('/auth/login');
    expect(req.request.headers.get('Authorization')).toBeNull();
    req.flush({});
  });

  it('nao deve adicionar header em rota publica /auth/cadastro', () => {
    mockAuthService.getToken.and.returnValue('token-jwt-valido');

    httpClient.post('/auth/cadastro', {}).subscribe();

    const req = httpTesting.expectOne('/auth/cadastro');
    expect(req.request.headers.get('Authorization')).toBeNull();
    req.flush({});
  });

  it('nao deve adicionar header em rota publica /public/', () => {
    mockAuthService.getToken.and.returnValue('token-jwt-valido');

    httpClient.get('/public/acompanhamento/OS-001').subscribe();

    const req = httpTesting.expectOne('/public/acompanhamento/OS-001');
    expect(req.request.headers.get('Authorization')).toBeNull();
    req.flush({});
  });

  it('nao deve adicionar header quando nao ha token armazenado', () => {
    mockAuthService.getToken.and.returnValue(null);

    httpClient.get('/api/ordens-servico').subscribe();

    const req = httpTesting.expectOne('/api/ordens-servico');
    expect(req.request.headers.get('Authorization')).toBeNull();
    req.flush({});
  });
});
