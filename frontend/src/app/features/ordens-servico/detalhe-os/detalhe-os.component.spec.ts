import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { DetalheOsComponent } from './detalhe-os.component';
import { OrdemServicoService } from '../ordem-servico.service';
import { OrcamentoService } from '../../orcamentos/orcamento.service';
import { ReparoAdicionalService } from '../../reparos-adicionais/reparo-adicional.service';
import { AuthService } from '../../../core/services/auth.service';
import { OrdemServicoDetalheResponse } from '../ordem-servico.model';

describe('DetalheOsComponent', () => {
  let mockOsService: jasmine.SpyObj<OrdemServicoService>;
  let mockOrcamentoService: jasmine.SpyObj<OrcamentoService>;
  let mockReparoService: jasmine.SpyObj<ReparoAdicionalService>;
  let mockAuth: jasmine.SpyObj<AuthService>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockRoute: { snapshot: { paramMap: { get: jasmine.Spy } } };

  const osFixture = (overrides: Partial<OrdemServicoDetalheResponse> = {}): OrdemServicoDetalheResponse => ({
    id: 1,
    numeroOs: 'OS-001',
    status: 'EM_DIAGNOSTICO',
    dataAbertura: '2026-01-01T10:00:00Z',
    ultimaAtualizacao: '2026-01-01T10:00:00Z',
    execucaoIniciadaEm: null,
    finalizadaEm: null,
    entregueEm: null,
    cliente: { id: 1, nome: 'Cliente Teste', cpfCnpj: '123.456.789-00', email: 'cliente@teste.com', telefone: '11999999999' },
    veiculo: { id: 1, placa: 'ABC1D23', marca: 'Fiat', modelo: 'Uno', ano: 2020 },
    servicos: [],
    orcamentoAtual: null,
    diagnostico: null,
    ...overrides,
  });

  function criarComponente(role: string | null, email: string | null = null, numeroOs: string | null = 'OS-001'): DetalheOsComponent {
    mockAuth.getRole.and.returnValue(role);
    mockAuth.getUsuarioLogado.and.returnValue(email ? { email, role: role ?? '' } : null);
    mockRoute.snapshot.paramMap.get.and.returnValue(numeroOs);
    return TestBed.runInInjectionContext(() => new DetalheOsComponent());
  }

  beforeEach(() => {
    mockOsService = jasmine.createSpyObj('OrdemServicoService', [
      'buscarPorNumeroOs', 'iniciarDiagnostico', 'registrarLaudo', 'incluirServicos',
      'registrarItensServico', 'finalizarDiagnostico', 'atribuirMecanico', 'entregar',
      'iniciarServico', 'finalizarServico',
    ]);
    mockOrcamentoService = jasmine.createSpyObj('OrcamentoService', ['aprovar', 'recusar', 'baixarPdf']);
    mockReparoService = jasmine.createSpyObj('ReparoAdicionalService', ['criar']);
    mockAuth = jasmine.createSpyObj('AuthService', ['getRole', 'getUsuarioLogado']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockRoute = { snapshot: { paramMap: { get: jasmine.createSpy('get') } } };

    TestBed.configureTestingModule({
      providers: [
        { provide: OrdemServicoService, useValue: mockOsService },
        { provide: OrcamentoService, useValue: mockOrcamentoService },
        { provide: ReparoAdicionalService, useValue: mockReparoService },
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: Router, useValue: mockRouter },
        { provide: AuthService, useValue: mockAuth },
        { provide: MatDialog, useValue: mockDialog },
        { provide: MatSnackBar, useValue: mockSnackBar },
      ],
    });
  });

  describe('ngOnInit', () => {
    it('deve carregar a OS quando numeroOs esta presente na rota', () => {
      const os = osFixture();
      mockOsService.buscarPorNumeroOs.and.returnValue(of(os));
      const component = criarComponente('ADMIN');

      component.ngOnInit();

      expect(mockOsService.buscarPorNumeroOs).toHaveBeenCalledWith('OS-001');
      expect(component.os()).toEqual(os);
      expect(component.loading()).toBeFalse();
    });

    it('deve navegar para /ordens-servico quando numeroOs nao esta presente', () => {
      const component = criarComponente('ADMIN', null, null);

      component.ngOnInit();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/ordens-servico']);
      expect(mockOsService.buscarPorNumeroOs).not.toHaveBeenCalled();
    });

    it('deve setar mensagem de erro quando falha ao buscar a OS', () => {
      mockOsService.buscarPorNumeroOs.and.returnValue(throwError(() => ({ error: { erro: 'OS nao encontrada' } })));
      const component = criarComponente('ADMIN');

      component.ngOnInit();

      expect(component.erro()).toBe('OS nao encontrada');
      expect(component.loading()).toBeFalse();
    });
  });

  describe('permissoes por role', () => {
    it('ADMIN deve ver todos os paineis', () => {
      const component = criarComponente('ADMIN');
      expect(component.podeVerPainelAtendente).toBeTrue();
      expect(component.podeVerPainelMecanico).toBeTrue();
      expect(component.podeVerPainelCliente).toBeTrue();
    });

    it('ATENDENTE deve ver apenas painel de atendente', () => {
      const component = criarComponente('ATENDENTE');
      expect(component.podeVerPainelAtendente).toBeTrue();
      expect(component.podeVerPainelMecanico).toBeFalse();
      expect(component.podeVerPainelCliente).toBeFalse();
    });

    it('CLIENTE deve ver apenas painel de cliente', () => {
      const component = criarComponente('CLIENTE');
      expect(component.podeVerPainelAtendente).toBeFalse();
      expect(component.podeVerPainelMecanico).toBeFalse();
      expect(component.podeVerPainelCliente).toBeTrue();
    });
  });

  describe('podeAlterarDiagnostico', () => {
    it('ADMIN sempre pode alterar diagnostico', () => {
      mockOsService.buscarPorNumeroOs.and.returnValue(of(osFixture()));
      const component = criarComponente('ADMIN');
      component.ngOnInit();

      expect(component.podeAlterarDiagnostico()).toBeTrue();
    });

    it('MECANICO atribuido pode alterar diagnostico', () => {
      const os = osFixture({ diagnostico: { mecanicoId: 1, mecanicoNome: 'Joao', mecanicoEmail: 'mecanico@teste.com', laudo: null, iniciadoEm: null, concluidoEm: null } });
      mockOsService.buscarPorNumeroOs.and.returnValue(of(os));
      const component = criarComponente('MECANICO', 'mecanico@teste.com');
      component.ngOnInit();

      expect(component.podeAlterarDiagnostico()).toBeTrue();
    });

    it('MECANICO nao atribuido nao pode alterar diagnostico', () => {
      const os = osFixture({ diagnostico: { mecanicoId: 1, mecanicoNome: 'Joao', mecanicoEmail: 'outro@teste.com', laudo: null, iniciadoEm: null, concluidoEm: null } });
      mockOsService.buscarPorNumeroOs.and.returnValue(of(os));
      const component = criarComponente('MECANICO', 'mecanico@teste.com');
      component.ngOnInit();

      expect(component.podeAlterarDiagnostico()).toBeFalse();
    });

    it('CLIENTE nunca pode alterar diagnostico', () => {
      mockOsService.buscarPorNumeroOs.and.returnValue(of(osFixture()));
      const component = criarComponente('CLIENTE');
      component.ngOnInit();

      expect(component.podeAlterarDiagnostico()).toBeFalse();
    });
  });

  describe('passoStatus', () => {
    it('deve retornar "pendente" quando nao ha OS carregada', () => {
      const component = criarComponente('ADMIN');
      expect(component.passoStatus('RECEBIDA')).toBe('pendente');
    });

    it('deve classificar passos como concluido, atual e pendente', () => {
      mockOsService.buscarPorNumeroOs.and.returnValue(of(osFixture({ status: 'EM_EXECUCAO' })));
      const component = criarComponente('ADMIN');
      component.ngOnInit();

      expect(component.passoStatus('RECEBIDA')).toBe('concluido');
      expect(component.passoStatus('EM_EXECUCAO')).toBe('atual');
      expect(component.passoStatus('ENTREGUE')).toBe('pendente');
    });
  });

  describe('labels', () => {
    it('deve mapear os rotulos de status corretamente', () => {
      const component = criarComponente('ADMIN');
      expect(component.labelStatusOs('EM_DIAGNOSTICO')).toBe('Em Diagnóstico');
      expect(component.labelStatusServico('FINALIZADO')).toBe('Finalizado');
      expect(component.labelStatusOrcamento('APROVADO')).toBe('Aprovado');
      expect(component.labelStatusItem('PENDENTE')).toBe('Pendência de peça');
    });
  });

  describe('acoes de diagnostico e orcamento', () => {
    it('iniciarDiagnostico deve chamar servico quando confirmado no dialog', () => {
      const os = osFixture();
      mockOsService.buscarPorNumeroOs.and.returnValue(of(os));
      const component = criarComponente('ADMIN');
      component.ngOnInit();

      mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
      mockOsService.iniciarDiagnostico.and.returnValue(of({} as any));

      component.iniciarDiagnostico();

      expect(mockOsService.iniciarDiagnostico).toHaveBeenCalledWith('OS-001');
      expect(mockSnackBar.open).toHaveBeenCalledWith('Diagnóstico iniciado.', 'Fechar', { duration: 3000 });
    });

    it('iniciarDiagnostico nao deve chamar servico quando dialog e cancelado', () => {
      mockOsService.buscarPorNumeroOs.and.returnValue(of(osFixture()));
      const component = criarComponente('ADMIN');
      component.ngOnInit();

      mockDialog.open.and.returnValue({ afterClosed: () => of(false) } as any);

      component.iniciarDiagnostico();

      expect(mockOsService.iniciarDiagnostico).not.toHaveBeenCalled();
    });

    it('confirmarFinalizarDiagnostico deve informar que a notificacao foi solicitada', () => {
      mockOsService.buscarPorNumeroOs.and.returnValue(of(osFixture()));
      const component = criarComponente('ADMIN');
      component.ngOnInit();
      mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
      mockOsService.finalizarDiagnostico.and.returnValue(of({} as any));

      component.confirmarFinalizarDiagnostico();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'Diagnóstico finalizado. Orçamento gerado; a notificação ao cliente foi solicitada.',
        'Fechar',
        { duration: 5000 },
      );
    });

    it('criarReparoAdicional deve informar que a notificacao foi solicitada', () => {
      mockOsService.buscarPorNumeroOs.and.returnValue(of(osFixture({ status: 'EM_EXECUCAO' })));
      const component = criarComponente('ADMIN');
      component.ngOnInit();
      mockDialog.open.and.returnValue({ afterClosed: () => of({ servicos: [] }) } as any);
      mockReparoService.criar.and.returnValue(of({ reparoAdicionalId: 1, orcamentoId: 10, publicUrl: 'url' }));

      component.criarReparoAdicional();

      expect(mockSnackBar.open).toHaveBeenCalledWith(
        'Reparo adicional criado. Orçamento #10 disponibilizado para aprovação; a notificação ao cliente foi solicitada.',
        'Fechar',
        { duration: 6000 },
      );
    });

    it('aprovarOrcamento deve chamar orcamentoService.aprovar quando confirmado', () => {
      const os = osFixture({ orcamentoAtual: { id: 10, tipo: 'PRINCIPAL', versao: 1, status: 'DISPONIVEL', totalServicos: 100, totalItens: 0, totalGeral: 100, criadoEm: '2026-01-01T10:00:00Z', disponibilizadoEm: null, aprovadoEm: null, reprovadoEm: null, mensagem: null } });
      mockOsService.buscarPorNumeroOs.and.returnValue(of(os));
      const component = criarComponente('CLIENTE');
      component.ngOnInit();

      mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
      mockOrcamentoService.aprovar.and.returnValue(of({} as any));

      component.aprovarOrcamento(10);

      expect(mockOrcamentoService.aprovar).toHaveBeenCalledWith(10);
      expect(mockSnackBar.open).toHaveBeenCalledWith('Orçamento aprovado. OS em execução.', 'Fechar', { duration: 3000 });
    });

    it('recusarOrcamento deve chamar orcamentoService.recusar com o motivo informado', () => {
      mockOsService.buscarPorNumeroOs.and.returnValue(of(osFixture()));
      const component = criarComponente('CLIENTE');
      component.ngOnInit();

      mockDialog.open.and.returnValue({ afterClosed: () => of('Preço muito alto') } as any);
      mockOrcamentoService.recusar.and.returnValue(of({} as any));

      component.recusarOrcamento(10);

      expect(mockOrcamentoService.recusar).toHaveBeenCalledWith(10, 'Preço muito alto');
      expect(mockSnackBar.open).toHaveBeenCalledWith('Orçamento recusado.', 'Fechar', { duration: 3000 });
    });

    it('recusarOrcamento nao deve chamar servico quando dialog e fechado sem motivo', () => {
      mockOsService.buscarPorNumeroOs.and.returnValue(of(osFixture()));
      const component = criarComponente('CLIENTE');
      component.ngOnInit();

      mockDialog.open.and.returnValue({ afterClosed: () => of(undefined) } as any);

      component.recusarOrcamento(10);

      expect(mockOrcamentoService.recusar).not.toHaveBeenCalled();
    });
  });

  describe('voltar', () => {
    it('deve navegar para /ordens-servico', () => {
      const component = criarComponente('ADMIN');
      component.voltar();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/ordens-servico']);
    });
  });
});
