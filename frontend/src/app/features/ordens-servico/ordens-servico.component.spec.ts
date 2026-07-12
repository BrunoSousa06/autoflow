import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { OrdensServicoComponent } from './ordens-servico.component';
import { OrdemServicoService } from './ordem-servico.service';
import { AuthService } from '../../core/services/auth.service';
import { OrdemServicoResponse, Page } from './ordem-servico.model';

describe('OrdensServicoComponent', () => {
  let mockService: jasmine.SpyObj<OrdemServicoService>;
  let mockAuth: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const pageVazia: Page<OrdemServicoResponse> = { content: [], page: { totalElements: 0, totalPages: 0, number: 0, size: 10 } };
  const osFixture = (numeroOs: string): OrdemServicoResponse => ({
    id: 1, numeroOs, clienteNome: 'Cliente', clienteCpfCnpj: '123.456.789-00', status: 'RECEBIDA',
    dataAbertura: '2026-01-01T10:00:00Z', execucaoIniciadaEm: null, finalizadaEm: null, entregueEm: null, servicos: [],
  });

  function criarComponente(role: string | null = 'ADMIN'): OrdensServicoComponent {
    mockAuth.getRole.and.returnValue(role);
    TestBed.configureTestingModule({
      providers: [
        { provide: OrdemServicoService, useValue: mockService },
        { provide: AuthService, useValue: mockAuth },
        { provide: Router, useValue: mockRouter },
      ],
    });
    return TestBed.runInInjectionContext(() => new OrdensServicoComponent());
  }

  beforeEach(() => {
    mockService = jasmine.createSpyObj('OrdemServicoService', ['listar']);
    mockAuth = jasmine.createSpyObj('AuthService', ['getRole']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
  });

  it('podeCriar deve ser true para ADMIN e ATENDENTE', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    expect(criarComponente('ADMIN').podeCriar).toBeTrue();
  });

  it('podeCriar deve ser false para MECANICO', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    expect(criarComponente('MECANICO').podeCriar).toBeFalse();
  });

  it('deve carregar as ordens ao inicializar', fakeAsync(() => {
    mockService.listar.and.returnValue(of({ content: [osFixture('OS-001')], page: { totalElements: 1, totalPages: 1, number: 0, size: 10 } }));
    const component = criarComponente();

    component.ngOnInit();
    tick(500);

    expect(component.ordens().length).toBe(1);
    expect(component.totalElements()).toBe(1);
    expect(component.loading()).toBeFalse();

    component.ngOnDestroy();
  }));

  it('deve setar mensagem de erro quando falha ao carregar', fakeAsync(() => {
    mockService.listar.and.returnValue(throwError(() => ({ error: { erro: 'Falha ao listar' } })));
    const component = criarComponente();

    component.ngOnInit();
    tick(500);

    expect(component.erroCarregamento()).toBe('Falha ao listar');
    expect(component.loading()).toBeFalse();

    component.ngOnDestroy();
  }));

  it('onPageChange deve atualizar pageIndex/pageSize e recarregar', fakeAsync(() => {
    mockService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente();
    component.ngOnInit();
    tick(500);
    mockService.listar.calls.reset();

    component.onPageChange({ pageIndex: 2, pageSize: 25, length: 0 } as any);

    expect(component.pageSize()).toBe(25);
    expect(mockService.listar).toHaveBeenCalledWith(jasmine.objectContaining({ page: 2, size: 25 }));

    component.ngOnDestroy();
  }));

  it('limparFiltros deve resetar o formulario', fakeAsync(() => {
    mockService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente();
    component.ngOnInit();
    tick(500);
    component.filtroForm.setValue({ cliente: 'Joao', numeroOs: 'OS-001', status: 'RECEBIDA' });
    tick(500);

    component.limparFiltros();

    expect(component.filtroForm.value).toEqual({ cliente: '', numeroOs: '', status: '' });

    component.ngOnDestroy();
  }));

  it('temFiltrosAtivos deve refletir o estado do formulario', fakeAsync(() => {
    mockService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente();
    component.ngOnInit();
    tick(500);

    expect(component.temFiltrosAtivos()).toBeFalse();

    component.filtroForm.patchValue({ cliente: 'Joao' });
    tick(500);

    expect(component.temFiltrosAtivos()).toBeTrue();

    component.ngOnDestroy();
  }));

  it('novaOs deve navegar para /ordens-servico/nova', fakeAsync(() => {
    mockService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente();
    component.ngOnInit();
    tick(500);

    component.novaOs();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/ordens-servico/nova']);

    component.ngOnDestroy();
  }));

  it('verDetalhe deve navegar para a rota da OS', fakeAsync(() => {
    mockService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente();
    component.ngOnInit();
    tick(500);

    component.verDetalhe(osFixture('OS-001'));

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/ordens-servico', 'OS-001']);

    component.ngOnDestroy();
  }));

  it('labelStatus deve mapear o rotulo do status', fakeAsync(() => {
    mockService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente();
    component.ngOnInit();
    tick(500);

    expect(component.labelStatus('EM_EXECUCAO')).toBe('Em Execução');

    component.ngOnDestroy();
  }));
});
