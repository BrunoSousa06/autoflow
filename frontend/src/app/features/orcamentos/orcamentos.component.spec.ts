import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { OrcamentosComponent } from './orcamentos.component';
import { OrcamentoService } from './orcamento.service';
import { AuthService } from '../../core/services/auth.service';
import { OrcamentoResponse } from './orcamento.model';

describe('OrcamentosComponent', () => {
  let mockService: jasmine.SpyObj<OrcamentoService>;
  let mockAuth: jasmine.SpyObj<AuthService>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
  let mockRouter: jasmine.SpyObj<Router>;

  const orcamento = (overrides: Partial<OrcamentoResponse> = {}): OrcamentoResponse => ({
    id: 1, ordemServicoId: 1, numeroOs: 'OS-001', tipo: 'PRINCIPAL', versao: 1, status: 'DISPONIVEL',
    totalServicos: 100, totalItens: 0, totalGeral: 100, servicos: [], itens: [],
    criadoEm: '2026-01-01T10:00:00Z', disponibilizadoEm: '2026-01-01T10:00:00Z', ...overrides,
  });

  function criarComponente(role: string | null = 'ADMIN'): OrcamentosComponent {
    mockAuth.getRole.and.returnValue(role);
    TestBed.configureTestingModule({
      providers: [
        { provide: OrcamentoService, useValue: mockService },
        { provide: AuthService, useValue: mockAuth },
        { provide: MatDialog, useValue: mockDialog },
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: Router, useValue: mockRouter },
      ],
    });
    return TestBed.runInInjectionContext(() => new OrcamentosComponent());
  }

  beforeEach(() => {
    mockService = jasmine.createSpyObj('OrcamentoService', ['listar', 'aprovar', 'recusar']);
    mockAuth = jasmine.createSpyObj('AuthService', ['getRole']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
  });

  it('deve carregar orcamentos ao inicializar', () => {
    mockService.listar.and.returnValue(of([orcamento()]));
    const component = criarComponente();

    component.ngOnInit();

    expect(component.orcamentos()).toEqual([orcamento()]);
    expect(component.loading()).toBeFalse();
  });

  it('carregar deve remover filtros de cliente quando o usuario e CLIENTE', () => {
    mockService.listar.and.returnValue(of([]));
    const component = criarComponente('CLIENTE');
    component.filtro = { clienteEmail: 'cliente@teste.com', clienteDocumento: '123' };

    component.carregar();

    expect(component.filtro.clienteEmail).toBeUndefined();
    expect(component.filtro.clienteDocumento).toBeUndefined();
  });

  it('deve exibir erro quando falha ao carregar', () => {
    mockService.listar.and.returnValue(throwError(() => ({ error: { erro: 'Falha' } })));
    const component = criarComponente();

    component.carregar();

    expect(mockSnackBar.open).toHaveBeenCalledWith('Falha', 'Fechar', { duration: 5000 });
  });

  it('limpar deve resetar o filtro e recarregar', () => {
    mockService.listar.and.returnValue(of([]));
    const component = criarComponente();
    component.filtro = { numeroOs: 'OS-001' };

    component.limpar();

    expect(component.filtro).toEqual({});
    expect(mockService.listar).toHaveBeenCalled();
  });

  it('detalhar deve navegar para a rota do orcamento', () => {
    mockService.listar.and.returnValue(of([]));
    const component = criarComponente();

    component.detalhar(10);

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/orcamentos', 10]);
  });

  it('podeAprovarRecusar deve ser true para ADMIN/CLIENTE com orcamento DISPONIVEL', () => {
    mockService.listar.and.returnValue(of([]));
    const component = criarComponente('ADMIN');

    expect(component.podeAprovarRecusar(orcamento({ status: 'DISPONIVEL' }))).toBeTrue();
    expect(component.podeAprovarRecusar(orcamento({ status: 'APROVADO' }))).toBeFalse();
  });

  it('aprovar deve atualizar a lista e exibir snackbar de sucesso', () => {
    mockService.listar.and.returnValue(of([orcamento()]));
    const atualizado = orcamento({ status: 'APROVADO' });
    mockService.aprovar.and.returnValue(of(atualizado));
    const component = criarComponente();
    component.ngOnInit();

    component.aprovar(orcamento());

    expect(mockService.aprovar).toHaveBeenCalledWith(1);
    expect(component.orcamentos()[0]).toEqual(atualizado);
    expect(mockSnackBar.open).toHaveBeenCalledWith('Orçamento aprovado com sucesso.', 'Fechar', { duration: 3000 });
    expect(component.acaoId()).toBeNull();
  });

  it('aprovar deve exibir erro quando falha', () => {
    mockService.listar.and.returnValue(of([]));
    mockService.aprovar.and.returnValue(throwError(() => ({})));
    const component = criarComponente();

    component.aprovar(orcamento());

    expect(mockSnackBar.open).toHaveBeenCalledWith('Não foi possível aprovar o orçamento.', 'Fechar', { duration: 5000 });
  });

  it('recusar deve atualizar a lista quando o dialog retorna um motivo', () => {
    mockService.listar.and.returnValue(of([orcamento()]));
    const atualizado = orcamento({ status: 'REPROVADO' });
    mockDialog.open.and.returnValue({ afterClosed: () => of('Motivo qualquer') } as any);
    mockService.recusar.and.returnValue(of(atualizado));
    const component = criarComponente();
    component.ngOnInit();

    component.recusar(orcamento());

    expect(mockService.recusar).toHaveBeenCalledWith(1, 'Motivo qualquer');
    expect(component.orcamentos()[0]).toEqual(atualizado);
    expect(mockSnackBar.open).toHaveBeenCalledWith('Orçamento recusado com sucesso.', 'Fechar', { duration: 3000 });
  });

  it('recusar nao deve chamar o servico quando o dialog e cancelado', () => {
    mockService.listar.and.returnValue(of([]));
    mockDialog.open.and.returnValue({ afterClosed: () => of(undefined) } as any);
    const component = criarComponente();

    component.recusar(orcamento());

    expect(mockService.recusar).not.toHaveBeenCalled();
  });

  it('statusLabel e tipoLabel devem mapear os rotulos', () => {
    mockService.listar.and.returnValue(of([]));
    const component = criarComponente();

    expect(component.statusLabel('APROVADO')).toBe('Aprovado');
    expect(component.tipoLabel('COMPLEMENTAR')).toBe('Complementar');
  });
});
