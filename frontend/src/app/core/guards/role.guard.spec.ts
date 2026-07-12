import { TestBed } from '@angular/core/testing';
import { provideRouter, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { roleGuard } from './role.guard';

describe('roleGuard', () => {
  let mockAuthService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['getRole']);

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: mockAuthService }
      ]
    });
  });

  it('deve permitir navegacao quando role do usuario esta na lista permitida', () => {
    mockAuthService.getRole.and.returnValue('ADMIN');

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(['ADMIN', 'ATENDENTE'])({} as any, {} as any)
    );

    expect(result).toBeTrue();
  });

  it('deve redirecionar para home do CLIENTE quando role nao esta na lista permitida', () => {
    mockAuthService.getRole.and.returnValue('CLIENTE');

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(['ADMIN'])({} as any, {} as any)
    );

    expect(result instanceof UrlTree).toBeTrue();
    expect((result as UrlTree).toString()).toBe('/minha-conta/minhas-ordens');
  });

  it('deve redirecionar para /dashboard quando role e ADMIN/ATENDENTE/MECANICO fora da lista permitida', () => {
    mockAuthService.getRole.and.returnValue('MECANICO');

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(['ADMIN'])({} as any, {} as any)
    );

    expect(result instanceof UrlTree).toBeTrue();
    expect((result as UrlTree).toString()).toBe('/dashboard');
  });

  it('deve redirecionar para /login quando role nao existe no mapa de rotas', () => {
    mockAuthService.getRole.and.returnValue('DESCONHECIDA');

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(['ADMIN'])({} as any, {} as any)
    );

    expect(result instanceof UrlTree).toBeTrue();
    expect((result as UrlTree).toString()).toBe('/login');
  });

  it('deve redirecionar para /login quando usuario nao possui role (nao autenticado)', () => {
    mockAuthService.getRole.and.returnValue(null);

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(['ADMIN'])({} as any, {} as any)
    );

    expect(result instanceof UrlTree).toBeTrue();
    expect((result as UrlTree).toString()).toBe('/login');
  });
});