import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { MinhaOrdemDetalheComponent } from './minha-ordem-detalhe.component';
import { MinhaContaService } from '../minha-conta.service';
import { AcompanhamentoOrdemServicoResponse } from '../minha-conta.model';

describe('MinhaOrdemDetalheComponent', () => {
  let mockService: jasmine.SpyObj<MinhaContaService>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockRoute: { snapshot: { paramMap: { get: jasmine.Spy } } };

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
    mockService = jasmine.createSpyObj('MinhaContaService', ['buscarMinhaOrdem']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockRoute = { snapshot: { paramMap: { get: jasmine.createSpy('get') } } };

    TestBed.configureTestingModule({
      providers: [
        { provide: MinhaContaService, useValue: mockService },
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockRoute },
      ],
    });
  });

  function criarComponente(): MinhaOrdemDetalheComponent {
    return TestBed.runInInjectionContext(() => new MinhaOrdemDetalheComponent());
  }

  it('deve navegar de volta quando nao ha numeroOs na rota', () => {
    mockRoute.snapshot.paramMap.get.and.returnValue(null);
    const component = criarComponente();

    component.ngOnInit();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/minha-conta/minhas-ordens']);
    expect(mockService.buscarMinhaOrdem).not.toHaveBeenCalled();
  });

  it('deve carregar a ordem quando encontrada', () => {
    mockRoute.snapshot.paramMap.get.and.returnValue('OS-001');
    mockService.buscarMinhaOrdem.and.returnValue(of(ordem()));
    const component = criarComponente();

    component.ngOnInit();

    expect(component.ordem()).toEqual(ordem());
    expect(component.loading()).toBeFalse();
  });

  it('deve exibir snackbar e voltar quando a ordem nao e encontrada', () => {
    mockRoute.snapshot.paramMap.get.and.returnValue('OS-999');
    mockService.buscarMinhaOrdem.and.returnValue(of(null));
    const component = criarComponente();

    component.ngOnInit();

    expect(mockSnackBar.open).toHaveBeenCalledWith('Ordem de servico nao encontrada para este cliente.', 'Fechar', { duration: 5000 });
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/minha-conta/minhas-ordens']);
  });

  it('deve exibir mensagem de erro quando falha ao carregar', () => {
    mockRoute.snapshot.paramMap.get.and.returnValue('OS-001');
    mockService.buscarMinhaOrdem.and.returnValue(throwError(() => ({ error: { erro: 'Falha ao buscar' } })));
    const component = criarComponente();

    component.ngOnInit();

    expect(mockSnackBar.open).toHaveBeenCalledWith('Falha ao buscar', 'Fechar', { duration: 5000 });
  });

  it('abrirOrcamento deve navegar para a rota de orcamento', () => {
    mockRoute.snapshot.paramMap.get.and.returnValue('OS-001');
    const component = criarComponente();

    component.abrirOrcamento(5);

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/orcamentos', 5]);
  });

  it('statusLabel e servicoStatusLabel devem mapear os rotulos', () => {
    mockRoute.snapshot.paramMap.get.and.returnValue('OS-001');
    const component = criarComponente();

    expect(component.statusLabel('EM_DIAGNOSTICO')).toBe('Em Diagnóstico');
    expect(component.servicoStatusLabel('FINALIZADO')).toBe('Finalizado');
  });

  it('orcamentoStatus deve retornar "-" quando nao ha orcamento', () => {
    mockRoute.snapshot.paramMap.get.and.returnValue('OS-001');
    const component = criarComponente();

    expect(component.orcamentoStatus(ordem({ orcamentoAtual: null }))).toBe('-');
  });
});
