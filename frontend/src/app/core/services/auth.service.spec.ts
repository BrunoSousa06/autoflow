import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

function criarTokenJwt(payload: object): string {
  const encoded = btoa(JSON.stringify(payload))
    .replaceAll('+', '-')
    .replaceAll('/', '_')
    .replace(/=+$/, '');
  return `header.${encoded}.signature`;
}

describe('AuthService', () => {
  let service: AuthService;
  let httpTesting: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    service = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('deve criar o servico', () => {
    expect(service).toBeTruthy();
  });

  it('getToken deve retornar null quando nao ha token armazenado', () => {
    expect(service.getToken()).toBeNull();
  });

  it('isLoggedIn deve retornar false quando nao ha sessao ativa', () => {
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('getRole deve retornar null quando nao ha sessao ativa', () => {
    expect(service.getRole()).toBeNull();
  });

  it('getUsuarioLogado deve retornar null quando sem token', () => {
    expect(service.getUsuarioLogado()).toBeNull();
  });

  it('login deve chamar POST para /auth/login e armazenar o token recebido', () => {
    const token = criarTokenJwt({ sub: 'admin@autoflow.com', role: 'ADMIN', iat: 0, exp: 9999999999 });

    service.login('admin@autoflow.com', 'senha123').subscribe();

    const req = httpTesting.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'admin@autoflow.com', senha: 'senha123' });
    req.flush({ token });

    expect(service.getToken()).toBe(token);
  });

  it('login deve tornar isLoggedIn verdadeiro apos receber token valido', () => {
    const token = criarTokenJwt({ sub: 'mecanico@autoflow.com', role: 'MECANICO', iat: 0, exp: 9999999999 });

    service.login('mecanico@autoflow.com', 'senha').subscribe();
    httpTesting.expectOne(`${environment.apiUrl}/auth/login`).flush({ token });

    expect(service.isLoggedIn()).toBeTrue();
  });

  it('getUsuarioLogado deve decodificar email e role do token valido', () => {
    const token = criarTokenJwt({ sub: 'atendente@autoflow.com', role: 'ATENDENTE', iat: 0, exp: 9999999999 });

    service.login('atendente@autoflow.com', 'senha').subscribe();
    httpTesting.expectOne(`${environment.apiUrl}/auth/login`).flush({ token });

    const usuario = service.getUsuarioLogado();
    expect(usuario?.email).toBe('atendente@autoflow.com');
    expect(usuario?.role).toBe('ATENDENTE');
  });

  it('getRole deve retornar o role do token quando logado', () => {
    const token = criarTokenJwt({ sub: 'admin@autoflow.com', role: 'ADMIN', iat: 0, exp: 9999999999 });

    service.login('admin@autoflow.com', 'senha').subscribe();
    httpTesting.expectOne(`${environment.apiUrl}/auth/login`).flush({ token });

    expect(service.getRole()).toBe('ADMIN');
  });

  it('logout deve remover o token e navegar para /login', () => {
    const token = criarTokenJwt({ sub: 'user@autoflow.com', role: 'CLIENTE', iat: 0, exp: 9999999999 });
    const navigateSpy = spyOn(router, 'navigate');

    service.login('user@autoflow.com', 'senha').subscribe();
    httpTesting.expectOne(`${environment.apiUrl}/auth/login`).flush({ token });

    service.logout();

    expect(service.getToken()).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('getUsuarioLogado deve retornar null e chamar logout quando token expirado', () => {
    const tokenExpirado = criarTokenJwt({ sub: 'user@autoflow.com', role: 'ADMIN', iat: 0, exp: 1 });
    const navigateSpy = spyOn(router, 'navigate');

    service.login('user@autoflow.com', 'senha').subscribe();
    httpTesting.expectOne(`${environment.apiUrl}/auth/login`).flush({ token: tokenExpirado });

    const usuario = service.getUsuarioLogado();
    expect(usuario).toBeNull();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
