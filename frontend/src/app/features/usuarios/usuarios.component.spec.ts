import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { UsuariosComponent } from './usuarios.component';
import { UsuarioAdminService } from './usuario.service';
import { UsuarioResponse } from './usuario.model';

describe('UsuariosComponent', () => {
  let mockService: jasmine.SpyObj<UsuarioAdminService>;
  let mockDialog: jasmine.SpyObj<MatDialog>;

  const usuario: UsuarioResponse = { id: 1, nome: 'Joao', email: 'joao@teste.com', role: 'MECANICO' };

  beforeEach(() => {
    mockService = jasmine.createSpyObj('UsuarioAdminService', ['listar']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);

    TestBed.configureTestingModule({
      providers: [
        { provide: UsuarioAdminService, useValue: mockService },
        { provide: MatDialog, useValue: mockDialog },
      ],
    });
  });

  function criarComponente(): UsuariosComponent {
    return TestBed.runInInjectionContext(() => new UsuariosComponent());
  }

  it('deve carregar os usuarios ao inicializar', () => {
    mockService.listar.and.returnValue(of([usuario]));
    const component = criarComponente();

    component.ngOnInit();

    expect(component.usuarios()).toEqual([usuario]);
    expect(component.loading()).toBeFalse();
  });

  it('deve setar mensagem de erro quando falha ao carregar', () => {
    mockService.listar.and.returnValue(throwError(() => ({})));
    const component = criarComponente();

    component.carregar();

    expect(component.erroCarregamento()).toBe('Não foi possível carregar os usuários.');
    expect(component.loading()).toBeFalse();
  });

  it('corRole deve retornar a cor mapeada ou o padrao', () => {
    mockService.listar.and.returnValue(of([]));
    const component = criarComponente();

    expect(component.corRole('ADMIN')).toEqual({ bg: '#e3f2fd', text: '#1565c0' });
    expect(component.corRole('DESCONHECIDA')).toEqual({ bg: '#f5f5f5', text: '#616161' });
  });

  it('labelRole deve retornar o rotulo mapeado ou a propria role', () => {
    mockService.listar.and.returnValue(of([]));
    const component = criarComponente();

    expect(component.labelRole('ADMIN')).toBe('Administrador');
    expect(component.labelRole('DESCONHECIDA')).toBe('DESCONHECIDA');
  });

  it('abrirFormulario deve recarregar a lista quando o dialog fecha com sucesso', () => {
    mockService.listar.and.returnValue(of([usuario]));
    mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
    const component = criarComponente();

    component.abrirFormulario();

    expect(mockService.listar).toHaveBeenCalledTimes(1);
  });

  it('abrirFormulario nao deve recarregar quando o dialog e cancelado', () => {
    mockService.listar.and.returnValue(of([usuario]));
    mockDialog.open.and.returnValue({ afterClosed: () => of(false) } as any);
    const component = criarComponente();

    component.abrirFormulario();

    expect(mockService.listar).not.toHaveBeenCalled();
  });
});
