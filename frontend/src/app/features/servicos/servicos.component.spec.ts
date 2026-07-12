import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { ServicosComponent } from './servicos.component';
import { ServicoService } from './servico.service';
import { AuthService } from '../../core/services/auth.service';
import { ServicoResponse } from './servico.model';

describe('ServicosComponent', () => {
  let mockService: jasmine.SpyObj<ServicoService>;
  let mockAuth: jasmine.SpyObj<AuthService>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;

  const servico: ServicoResponse = { id: 1, nome: 'Troca de oleo', descricao: 'Troca de oleo do motor', valor: 100, ativo: true };
  const pageVazia = { content: [] as ServicoResponse[], page: { totalElements: 0, totalPages: 0, number: 0, size: 20 } };

  function criarComponente(role: string | null = 'ADMIN'): ServicosComponent {
    mockAuth.getRole.and.returnValue(role);
    TestBed.configureTestingModule({
      providers: [
        { provide: ServicoService, useValue: mockService },
        { provide: AuthService, useValue: mockAuth },
        { provide: MatDialog, useValue: mockDialog },
        { provide: MatSnackBar, useValue: mockSnackBar },
      ],
    });
    return TestBed.runInInjectionContext(() => new ServicosComponent());
  }

  beforeEach(() => {
    mockService = jasmine.createSpyObj('ServicoService', ['listar', 'deletar']);
    mockAuth = jasmine.createSpyObj('AuthService', ['getRole']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
  });

  it('isAdmin deve ser true apenas para ADMIN', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    expect(criarComponente('ADMIN').isAdmin).toBeTrue();
  });

  it('isAdmin deve ser false para roles diferentes de ADMIN', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    expect(criarComponente('MECANICO').isAdmin).toBeFalse();
  });

  it('podeGerenciar deve ser true para MECANICO', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    expect(criarComponente('MECANICO').podeGerenciar).toBeTrue();
  });

  it('podeGerenciar deve ser false para ATENDENTE', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    expect(criarComponente('ATENDENTE').podeGerenciar).toBeFalse();
  });

  it('deve carregar os servicos ao inicializar', () => {
    mockService.listar.and.returnValue(of({ content: [servico], page: { totalElements: 1, totalPages: 1, number: 0, size: 20 } }));
    const component = criarComponente();

    component.ngOnInit();

    expect(mockService.listar).toHaveBeenCalledWith(0, 20);
    expect(component.servicos()).toEqual([servico]);
    expect(component.totalElements()).toBe(1);
    expect(component.loading()).toBeFalse();
  });

  it('deve setar mensagem de erro quando falha ao carregar', () => {
    mockService.listar.and.returnValue(throwError(() => ({})));
    const component = criarComponente();

    component.carregar();

    expect(component.erroCarregamento()).toBe('Não foi possível carregar os serviços.');
    expect(component.loading()).toBeFalse();
  });

  it('onPage deve recarregar com pageIndex e pageSize do evento', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente();

    component.onPage({ pageIndex: 2, pageSize: 50, length: 0 } as any);

    expect(mockService.listar).toHaveBeenCalledWith(2, 50);
  });

  it('abrirFormulario deve recarregar quando o dialog fecha com sucesso', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
    const component = criarComponente();

    component.abrirFormulario();

    expect(mockDialog.open).toHaveBeenCalled();
    expect(mockService.listar).toHaveBeenCalled();
  });

  it('abrirFormulario nao deve recarregar quando o dialog e cancelado', () => {
    mockDialog.open.and.returnValue({ afterClosed: () => of(false) } as any);
    const component = criarComponente();

    component.abrirFormulario(servico);

    expect(mockService.listar).not.toHaveBeenCalled();
  });

  it('confirmarInativacao deve inativar o servico quando confirmado', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
    mockService.deletar.and.returnValue(of('Servico inativado'));
    const component = criarComponente();

    component.confirmarInativacao(servico);

    expect(mockService.deletar).toHaveBeenCalledWith(1);
    expect(mockSnackBar.open).toHaveBeenCalledWith('Serviço inativado com sucesso.', 'Fechar', { duration: 3000 });
  });

  it('confirmarInativacao nao deve chamar o servico quando o dialog e cancelado', () => {
    mockDialog.open.and.returnValue({ afterClosed: () => of(false) } as any);
    const component = criarComponente();

    component.confirmarInativacao(servico);

    expect(mockService.deletar).not.toHaveBeenCalled();
  });

  it('confirmarInativacao deve exibir mensagem de erro quando falha', () => {
    mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
    mockService.deletar.and.returnValue(throwError(() => ({ error: { erro: 'Servico em uso' } })));
    const component = criarComponente();

    component.confirmarInativacao(servico);

    expect(mockSnackBar.open).toHaveBeenCalledWith('Servico em uso', 'Fechar', { duration: 4000 });
  });
});
