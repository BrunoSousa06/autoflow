import { TestBed } from '@angular/core/testing';
import { AuthService, UsuarioLogado } from '../../core/services/auth.service';
import { ShellComponent } from './shell.component';

describe('ShellComponent', () => {
  let mockAuth: jasmine.SpyObj<AuthService>;

  function criarComponente(usuario: UsuarioLogado | null): ShellComponent {
    mockAuth.getUsuarioLogado.and.returnValue(usuario);
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: mockAuth }],
    });
    return TestBed.runInInjectionContext(() => new ShellComponent());
  }

  beforeEach(() => {
    mockAuth = jasmine.createSpyObj('AuthService', ['getUsuarioLogado', 'logout']);
  });

  it('deve filtrar os itens de navegacao de acordo com a role do usuario ADMIN', () => {
    const component = criarComponente({ email: 'admin@teste.com', role: 'ADMIN' });

    const rotas = component.navItems.map(i => i.route);
    expect(rotas).toContain('/dashboard');
    expect(rotas).toContain('/clientes');
    expect(rotas).not.toContain('/minha-conta');
  });

  it('deve filtrar os itens de navegacao de acordo com a role do usuario CLIENTE', () => {
    const component = criarComponente({ email: 'cliente@teste.com', role: 'CLIENTE' });

    const rotas = component.navItems.map(i => i.route);
    expect(rotas).toEqual(['/veiculos', '/minha-conta', '/minha-conta/minhas-ordens']);
  });

  it('nao deve exibir nenhum item quando nao ha usuario logado', () => {
    const component = criarComponente(null);

    expect(component.navItems.length).toBe(0);
    expect(component.roleLabel).toBe('');
  });

  it('roleLabel deve mapear a role para o rotulo em portugues', () => {
    const component = criarComponente({ email: 'meca@teste.com', role: 'MECANICO' });

    expect(component.roleLabel).toBe('Mecânico');
  });

  it('roleLabel deve retornar a propria role quando nao mapeada', () => {
    const component = criarComponente({ email: 'x@teste.com', role: 'DESCONHECIDA' });

    expect(component.roleLabel).toBe('DESCONHECIDA');
  });

  it('logout deve chamar AuthService.logout', () => {
    const component = criarComponente({ email: 'admin@teste.com', role: 'ADMIN' });

    component.logout();

    expect(mockAuth.logout).toHaveBeenCalled();
  });
});
