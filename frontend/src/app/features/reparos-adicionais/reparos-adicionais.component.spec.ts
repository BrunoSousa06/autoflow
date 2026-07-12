import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { ReparosAdicionaisComponent } from './reparos-adicionais.component';
import { OrcamentoService } from '../orcamentos/orcamento.service';
import { AuthService } from '../../core/services/auth.service';
import { OrcamentoResponse } from '../orcamentos/orcamento.model';

describe('ReparosAdicionaisComponent', () => {
  let mockService: jasmine.SpyObj<OrcamentoService>;
  let mockAuth: jasmine.SpyObj<AuthService>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
  let mockRouter: jasmine.SpyObj<Router>;

  const item = (overrides: Partial<OrcamentoResponse> = {}): OrcamentoResponse => ({
    id: 1, ordemServicoId: 1, numeroOs: 'OS-001', tipo: 'COMPLEMENTAR', versao: 1, status: 'DISPONIVEL',
    totalServicos: 100, totalItens: 0, totalGeral: 100, servicos: [], itens: [],
    criadoEm: '2026-01-01T10:00:00Z', disponibilizadoEm: '2026-01-01T10:00:00Z', ...overrides,
  });

  function criarComponente(role: string | null = 'ADMIN'): ReparosAdicionaisComponent {
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
    return TestBed.runInInjectionContext(() => new ReparosAdicionaisComponent());
  }

  beforeEach(() => {
    mockService = jasmine.createSpyObj('OrcamentoService', ['listar', 'aprovar', 'recusar']);
    mockAuth = jasmine.createSpyObj('AuthService', ['getRole']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
  });

  it('deve carregar os reparos adicionais filtrando por tipo COMPLEMENTAR', () => {
    mockService.listar.and.returnValue(of([item()]));
    const component = criarComponente();

    component.ngOnInit();

    expect(mockService.listar).toHaveBeenCalledWith({ tipo: 'COMPLEMENTAR', statusOrcamento: undefined, numeroOs: undefined });
    expect(component.itens()).toEqual([item()]);
    expect(component.loading()).toBeFalse();
  });

  it('deve incluir filtros de status e numeroOs quando informados', () => {
    mockService.listar.and.returnValue(of([]));
    const component = criarComponente();
    component.filtroStatus = 'APROVADO';
    component.filtroNumeroOs = '  OS-001  ';

    component.carregar();

    expect(mockService.listar).toHaveBeenCalledWith({ tipo: 'COMPLEMENTAR', statusOrcamento: 'APROVADO', numeroOs: 'OS-001' });
  });

  it('deve exibir snackbar quando falha ao carregar', () => {
    mockService.listar.and.returnValue(throwError(() => ({})));
    const component = criarComponente();

    component.carregar();

    expect(mockSnackBar.open).toHaveBeenCalledWith('Não foi possível carregar os reparos adicionais.', 'Fechar', { duration: 5000 });
  });

  it('limpar deve resetar filtros e recarregar', () => {
    mockService.listar.and.returnValue(of([]));
    const component = criarComponente();
    component.filtroStatus = 'APROVADO';
    component.filtroNumeroOs = 'OS-001';

    component.limpar();

    expect(component.filtroStatus).toBe('');
    expect(component.filtroNumeroOs).toBe('');
  });

  it('statusLabel deve mapear o rotulo do status', () => {
    mockService.listar.and.returnValue(of([]));
    const component = criarComponente();

    expect(component.statusLabel('REPROVADO')).toBe('Reprovado');
  });

  it('podeAprovarRecusar deve ser true apenas para ADMIN/CLIENTE com status DISPONIVEL', () => {
    mockService.listar.and.returnValue(of([]));
    const component = criarComponente('CLIENTE');

    expect(component.podeAprovarRecusar(item({ status: 'DISPONIVEL' }))).toBeTrue();
    expect(component.podeAprovarRecusar(item({ status: 'REPROVADO' }))).toBeFalse();
  });

  it('aprovar deve atualizar a lista e exibir snackbar', () => {
    mockService.listar.and.returnValue(of([item()]));
    const atualizado = item({ status: 'APROVADO' });
    mockService.aprovar.and.returnValue(of(atualizado));
    const component = criarComponente();
    component.ngOnInit();

    component.aprovar(item());

    expect(component.itens()[0]).toEqual(atualizado);
    expect(mockSnackBar.open).toHaveBeenCalledWith('Orçamento aprovado com sucesso.', 'Fechar', { duration: 3000 });
  });

  it('aprovar deve exibir erro quando falha', () => {
    mockService.listar.and.returnValue(of([]));
    mockService.aprovar.and.returnValue(throwError(() => ({})));
    const component = criarComponente();

    component.aprovar(item());

    expect(mockSnackBar.open).toHaveBeenCalledWith('Não foi possível aprovar o orçamento.', 'Fechar', { duration: 5000 });
  });

  it('recusar deve atualizar a lista quando o dialog retorna motivo', () => {
    mockService.listar.and.returnValue(of([item()]));
    const atualizado = item({ status: 'REPROVADO' });
    mockDialog.open.and.returnValue({ afterClosed: () => of('Motivo') } as any);
    mockService.recusar.and.returnValue(of(atualizado));
    const component = criarComponente();
    component.ngOnInit();

    component.recusar(item());

    expect(component.itens()[0]).toEqual(atualizado);
    expect(mockSnackBar.open).toHaveBeenCalledWith('Orçamento recusado.', 'Fechar', { duration: 3000 });
  });

  it('recusar nao deve chamar o servico quando o dialog e cancelado', () => {
    mockService.listar.and.returnValue(of([]));
    mockDialog.open.and.returnValue({ afterClosed: () => of(null) } as any);
    const component = criarComponente();

    component.recusar(item());

    expect(mockService.recusar).not.toHaveBeenCalled();
  });

  it('verOrcamento e verOs devem navegar para as rotas corretas', () => {
    mockService.listar.and.returnValue(of([]));
    const component = criarComponente();

    component.verOrcamento(5);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/orcamentos', 5]);

    component.verOs('OS-001');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/ordens-servico', 'OS-001']);
  });
});
