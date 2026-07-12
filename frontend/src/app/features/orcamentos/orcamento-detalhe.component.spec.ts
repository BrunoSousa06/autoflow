import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { OrcamentoDetalheComponent } from './orcamento-detalhe.component';
import { OrcamentoService } from './orcamento.service';
import { AuthService } from '../../core/services/auth.service';
import { OrcamentoResponse } from './orcamento.model';

describe('OrcamentoDetalheComponent', () => {
  let mockService: jasmine.SpyObj<OrcamentoService>;
  let mockAuth: jasmine.SpyObj<AuthService>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockRoute: { snapshot: { paramMap: { get: jasmine.Spy } } };

  const orcamentoFixture = (overrides: Partial<OrcamentoResponse> = {}): OrcamentoResponse => ({
    id: 10,
    ordemServicoId: 1,
    numeroOs: 'OS-001',
    tipo: 'PRINCIPAL',
    versao: 1,
    status: 'DISPONIVEL',
    totalServicos: 100,
    totalItens: 50,
    totalGeral: 150,
    servicos: [],
    itens: [],
    criadoEm: '2026-01-01T10:00:00Z',
    disponibilizadoEm: '2026-01-01T10:00:00Z',
    ...overrides,
  });

  function criarComponente(role: string | null, id: string | null = '10'): OrcamentoDetalheComponent {
    mockAuth.getRole.and.returnValue(role);
    mockRoute.snapshot.paramMap.get.and.returnValue(id);
    return TestBed.runInInjectionContext(() => new OrcamentoDetalheComponent());
  }

  beforeEach(() => {
    mockService = jasmine.createSpyObj('OrcamentoService', ['buscarPorId', 'aprovar', 'recusar', 'baixarPdf']);
    mockAuth = jasmine.createSpyObj('AuthService', ['getRole']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockRoute = { snapshot: { paramMap: { get: jasmine.createSpy('get') } } };

    TestBed.configureTestingModule({
      providers: [
        { provide: OrcamentoService, useValue: mockService },
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: Router, useValue: mockRouter },
        { provide: AuthService, useValue: mockAuth },
        { provide: MatDialog, useValue: mockDialog },
        { provide: MatSnackBar, useValue: mockSnackBar },
      ],
    });
  });

  describe('carregar / ngOnInit', () => {
    it('deve carregar o orcamento pelo id da rota', () => {
      const orcamento = orcamentoFixture();
      mockService.buscarPorId.and.returnValue(of(orcamento));
      const component = criarComponente('CLIENTE');

      component.ngOnInit();

      expect(mockService.buscarPorId).toHaveBeenCalledWith(10);
      expect(component.orcamento()).toEqual(orcamento);
      expect(component.loading()).toBeFalse();
    });

    it('deve exibir snackbar e voltar quando id da rota e invalido', () => {
      const component = criarComponente('CLIENTE', null);

      component.carregar();

      expect(mockSnackBar.open).toHaveBeenCalledWith('Orçamento inválido.', 'Fechar', { duration: 4000 });
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/orcamentos']);
      expect(mockService.buscarPorId).not.toHaveBeenCalled();
    });

    it('deve exibir mensagem de erro quando falha ao carregar', () => {
      mockService.buscarPorId.and.returnValue(throwError(() => ({ error: { erro: 'Orcamento nao encontrado' } })));
      const component = criarComponente('CLIENTE');

      component.carregar();

      expect(mockSnackBar.open).toHaveBeenCalledWith('Orcamento nao encontrado', 'Fechar', { duration: 5000 });
    });
  });

  describe('podeAprovarRecusar', () => {
    it('deve ser true para ADMIN quando orcamento esta DISPONIVEL', () => {
      mockService.buscarPorId.and.returnValue(of(orcamentoFixture({ status: 'DISPONIVEL' })));
      const component = criarComponente('ADMIN');
      component.ngOnInit();

      expect(component.podeAprovarRecusar()).toBeTrue();
    });

    it('deve ser true para CLIENTE quando orcamento esta DISPONIVEL', () => {
      mockService.buscarPorId.and.returnValue(of(orcamentoFixture({ status: 'DISPONIVEL' })));
      const component = criarComponente('CLIENTE');
      component.ngOnInit();

      expect(component.podeAprovarRecusar()).toBeTrue();
    });

    it('deve ser false para outras roles', () => {
      mockService.buscarPorId.and.returnValue(of(orcamentoFixture({ status: 'DISPONIVEL' })));
      const component = criarComponente('MECANICO');
      component.ngOnInit();

      expect(component.podeAprovarRecusar()).toBeFalse();
    });

    it('deve ser false quando orcamento nao esta DISPONIVEL', () => {
      mockService.buscarPorId.and.returnValue(of(orcamentoFixture({ status: 'APROVADO' })));
      const component = criarComponente('CLIENTE');
      component.ngOnInit();

      expect(component.podeAprovarRecusar()).toBeFalse();
    });
  });

  describe('aprovar', () => {
    it('deve aprovar o orcamento e atualizar o signal', () => {
      const component = criarComponente('CLIENTE');
      const atualizado = orcamentoFixture({ status: 'APROVADO' });
      mockService.aprovar.and.returnValue(of(atualizado));

      component.aprovar(orcamentoFixture());

      expect(mockService.aprovar).toHaveBeenCalledWith(10);
      expect(component.orcamento()).toEqual(atualizado);
      expect(mockSnackBar.open).toHaveBeenCalledWith('Orçamento aprovado com sucesso.', 'Fechar', { duration: 3000 });
      expect(component.acao()).toBeFalse();
    });

    it('deve exibir erro quando falha ao aprovar', () => {
      const component = criarComponente('CLIENTE');
      mockService.aprovar.and.returnValue(throwError(() => ({ error: { message: 'Erro ao aprovar' } })));

      component.aprovar(orcamentoFixture());

      expect(mockSnackBar.open).toHaveBeenCalledWith('Erro ao aprovar', 'Fechar', { duration: 5000 });
      expect(component.acao()).toBeFalse();
    });
  });

  describe('recusar', () => {
    it('deve recusar o orcamento com o motivo informado pelo dialog', () => {
      const component = criarComponente('CLIENTE');
      const atualizado = orcamentoFixture({ status: 'REPROVADO' });
      mockDialog.open.and.returnValue({ afterClosed: () => of('Fora do orcamento') } as any);
      mockService.recusar.and.returnValue(of(atualizado));

      component.recusar(orcamentoFixture());

      expect(mockService.recusar).toHaveBeenCalledWith(10, 'Fora do orcamento');
      expect(component.orcamento()).toEqual(atualizado);
      expect(mockSnackBar.open).toHaveBeenCalledWith('Orçamento recusado com sucesso.', 'Fechar', { duration: 3000 });
    });

    it('nao deve chamar servico quando dialog e fechado sem motivo', () => {
      const component = criarComponente('CLIENTE');
      mockDialog.open.and.returnValue({ afterClosed: () => of(undefined) } as any);

      component.recusar(orcamentoFixture());

      expect(mockService.recusar).not.toHaveBeenCalled();
    });
  });

  describe('labels', () => {
    it('statusLabel e tipoLabel devem retornar os rotulos mapeados', () => {
      const component = criarComponente('CLIENTE');
      expect(component.statusLabel('APROVADO')).toBe('Aprovado');
      expect(component.tipoLabel('COMPLEMENTAR')).toBe('Complementar');
    });

    it('tipoItemLabel deve mapear PECA e INSUMO e retornar o valor original para outros', () => {
      const component = criarComponente('CLIENTE');
      expect(component.tipoItemLabel('PECA')).toBe('Peça');
      expect(component.tipoItemLabel('INSUMO')).toBe('Insumo');
      expect(component.tipoItemLabel('OUTRO')).toBe('OUTRO');
    });
  });

  describe('voltar', () => {
    it('deve navegar para /orcamentos', () => {
      const component = criarComponente('CLIENTE');
      component.voltar();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/orcamentos']);
    });
  });
});