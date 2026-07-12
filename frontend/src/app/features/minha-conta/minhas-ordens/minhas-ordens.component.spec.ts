import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { MinhasOrdensComponent } from './minhas-ordens.component';
import { MinhaContaService } from '../minha-conta.service';
import { AcompanhamentoOrdemServicoResponse } from '../minha-conta.model';

describe('MinhasOrdensComponent', () => {
  let component: MinhasOrdensComponent;
  let mockService: jasmine.SpyObj<MinhaContaService>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
  let mockRouter: jasmine.SpyObj<Router>;

  const ordem = (overrides: Partial<AcompanhamentoOrdemServicoResponse> = {}): AcompanhamentoOrdemServicoResponse => ({
    numeroOs: 'OS-001',
    placa: 'ABC1D23',
    statusAtual: 'RECEBIDA',
    dataAbertura: '2026-01-01T10:00:00Z',
    ultimaAtualizacao: '2026-01-01T10:00:00Z',
    servicosSolicitados: [],
    orcamentoAtual: null,
    situacaoAprovacao: null,
    mensagemParaCliente: '',
    historicoStatus: [],
    ...overrides,
  });

  beforeEach(() => {
    mockService = jasmine.createSpyObj('MinhaContaService', ['listarMinhasOrdens']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: MinhaContaService, useValue: mockService },
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: Router, useValue: mockRouter },
      ],
    });

    component = TestBed.runInInjectionContext(() => new MinhasOrdensComponent());
  });

  it('deve carregar as ordens do cliente ao inicializar', () => {
    const ordens = [ordem()];
    mockService.listarMinhasOrdens.and.returnValue(of(ordens));

    component.ngOnInit();

    expect(component.ordens()).toEqual(ordens);
    expect(component.loading()).toBeFalse();
  });

  it('deve exibir snackbar com mensagem do backend quando falha ao carregar', () => {
    mockService.listarMinhasOrdens.and.returnValue(
      throwError(() => ({ error: { erro: 'Falha ao buscar ordens' } }))
    );

    component.carregar();

    expect(mockSnackBar.open).toHaveBeenCalledWith('Falha ao buscar ordens', 'Fechar', { duration: 5000 });
    expect(component.loading()).toBeFalse();
  });

  it('deve exibir mensagem padrao quando erro nao possui mensagem do backend', () => {
    mockService.listarMinhasOrdens.and.returnValue(throwError(() => ({})));

    component.carregar();

    expect(mockSnackBar.open).toHaveBeenCalledWith(
      'Nao foi possivel carregar suas ordens.',
      'Fechar',
      { duration: 5000 }
    );
  });

  it('abrirDetalhe deve navegar para a rota de detalhe da OS', () => {
    component.abrirDetalhe('OS-001');

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/minha-conta/minhas-ordens', 'OS-001']);
  });

  it('abrirOrcamento deve navegar para a rota de orcamento', () => {
    component.abrirOrcamento(42);

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/orcamentos', 42]);
  });

  it('ordensFiltradas deve retornar todas as ordens quando nenhum filtro esta selecionado', () => {
    mockService.listarMinhasOrdens.and.returnValue(
      of([ordem({ numeroOs: 'OS-001', statusAtual: 'RECEBIDA' }), ordem({ numeroOs: 'OS-002', statusAtual: 'FINALIZADA' })])
    );
    component.ngOnInit();
    component.statusFiltro = '';

    expect(component.ordensFiltradas().length).toBe(2);
  });

  it('ordensFiltradas deve filtrar pelas ordens com o status selecionado', () => {
    mockService.listarMinhasOrdens.and.returnValue(
      of([ordem({ numeroOs: 'OS-001', statusAtual: 'RECEBIDA' }), ordem({ numeroOs: 'OS-002', statusAtual: 'FINALIZADA' })])
    );
    component.ngOnInit();
    component.statusFiltro = 'FINALIZADA';

    const filtradas = component.ordensFiltradas();
    expect(filtradas.length).toBe(1);
    expect(filtradas[0].numeroOs).toBe('OS-002');
  });

  it('statusLabel deve retornar o rotulo mapeado', () => {
    expect(component.statusLabel('EM_DIAGNOSTICO')).toBe('Em Diagnóstico');
  });

  it('orcamentoStatus deve retornar "-" quando nao ha orcamento atual', () => {
    expect(component.orcamentoStatus(ordem({ orcamentoAtual: null }))).toBe('-');
  });

  it('orcamentoStatus deve retornar "Aguardando aprovacao" quando orcamento esta DISPONIVEL', () => {
    const os = ordem({
      orcamentoAtual: {
        id: 1,
        tipo: 'PRINCIPAL',
        versao: 1,
        status: 'DISPONIVEL',
        totalServicos: 100,
        totalItens: 50,
        totalGeral: 150,
        criadoEm: '2026-01-01T10:00:00Z',
        disponibilizadoEm: '2026-01-01T10:00:00Z',
        aprovadoEm: null,
        reprovadoEm: null,
        mensagem: null,
      },
    });

    expect(component.orcamentoStatus(os)).toBe('Aguardando aprovacao');
  });

  it('orcamentoStatus deve retornar o rotulo mapeado para outros status', () => {
    const os = ordem({
      orcamentoAtual: {
        id: 1,
        tipo: 'PRINCIPAL',
        versao: 1,
        status: 'APROVADO',
        totalServicos: 100,
        totalItens: 50,
        totalGeral: 150,
        criadoEm: '2026-01-01T10:00:00Z',
        disponibilizadoEm: '2026-01-01T10:00:00Z',
        aprovadoEm: '2026-01-02T10:00:00Z',
        reprovadoEm: null,
        mensagem: null,
      },
    });

    expect(component.orcamentoStatus(os)).toBe('Aprovado');
  });
});