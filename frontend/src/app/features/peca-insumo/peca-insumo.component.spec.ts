import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { PecaInsumoComponent } from './peca-insumo.component';
import { PecaInsumoService } from './peca-insumo.service';
import { AuthService } from '../../core/services/auth.service';
import { PecaInsumoResponse } from './peca-insumo.model';

describe('PecaInsumoComponent', () => {
  let mockService: jasmine.SpyObj<PecaInsumoService>;
  let mockAuth: jasmine.SpyObj<AuthService>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;

  const item: PecaInsumoResponse = { id: 1, nome: 'Filtro', valor: 20, quantidade: 10, tipo: 'PECA' };
  const pageVazia = { content: [] as PecaInsumoResponse[], page: { totalElements: 0, totalPages: 0, number: 0, size: 10 } };

  function criarComponente(role: string | null = 'ADMIN'): PecaInsumoComponent {
    mockAuth.getRole.and.returnValue(role);
    TestBed.configureTestingModule({
      providers: [
        { provide: PecaInsumoService, useValue: mockService },
        { provide: AuthService, useValue: mockAuth },
        { provide: MatDialog, useValue: mockDialog },
        { provide: MatSnackBar, useValue: mockSnackBar },
      ],
    });
    return TestBed.runInInjectionContext(() => new PecaInsumoComponent());
  }

  beforeEach(() => {
    mockService = jasmine.createSpyObj('PecaInsumoService', ['listar', 'buscarPorId']);
    mockAuth = jasmine.createSpyObj('AuthService', ['getRole']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
  });

  it('podeGerenciar deve ser true para ADMIN', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    expect(criarComponente('ADMIN').podeGerenciar).toBeTrue();
  });

  it('podeGerenciar deve ser true para ATENDENTE', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    expect(criarComponente('ATENDENTE').podeGerenciar).toBeTrue();
  });

  it('podeGerenciar deve ser false para MECANICO', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    expect(criarComponente('MECANICO').podeGerenciar).toBeFalse();
  });

  it('deve carregar os itens ao inicializar', () => {
    mockService.listar.and.returnValue(of({ content: [item], page: { totalElements: 1, totalPages: 1, number: 0, size: 10 } }));
    const component = criarComponente();

    component.ngOnInit();

    expect(mockService.listar).toHaveBeenCalledWith(0, 10, '', '');
    expect(component.itens()).toEqual([item]);
    expect(component.loading()).toBeFalse();
  });

  it('deve setar mensagem de erro do backend quando falha ao carregar', () => {
    mockService.listar.and.returnValue(throwError(() => ({ error: { erro: 'Falha ao listar' } })));
    const component = criarComponente();

    component.carregar();

    expect(component.erroCarregamento()).toBe('Falha ao listar');
    expect(component.loading()).toBeFalse();
  });

  it('deve usar mensagem padrao quando o erro nao possui detalhe', () => {
    mockService.listar.and.returnValue(throwError(() => ({})));
    const component = criarComponente();

    component.carregar();

    expect(component.erroCarregamento()).toBe('Nao foi possivel carregar pecas e insumos.');
  });

  it('temFiltrosAtivos deve refletir os filtros de nome e tipo', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente();

    expect(component.temFiltrosAtivos).toBeFalse();

    component.filtroNome = 'Filtro';
    expect(component.temFiltrosAtivos).toBeTrue();

    component.filtroNome = '';
    component.filtroTipo = 'PECA';
    expect(component.temFiltrosAtivos).toBeTrue();
  });

  it('buscar deve recarregar a partir da primeira pagina', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente();
    component.filtroNome = 'Filtro';

    component.buscar();

    expect(mockService.listar).toHaveBeenCalledWith(0, 10, 'Filtro', '');
  });

  it('limparFiltros deve resetar nome/tipo e recarregar', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente();
    component.filtroNome = 'Filtro';
    component.filtroTipo = 'PECA';

    component.limparFiltros();

    expect(component.filtroNome).toBe('');
    expect(component.filtroTipo).toBe('');
  });

  it('onPage deve recarregar com pageIndex e pageSize do evento', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente();

    component.onPage({ pageIndex: 1, pageSize: 25, length: 0 } as any);

    expect(mockService.listar).toHaveBeenCalledWith(1, 25, '', '');
  });

  it('abrirFormulario deve recarregar quando o dialog fecha com sucesso', () => {
    mockService.listar.and.returnValue(of(pageVazia));
    mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);
    const component = criarComponente();

    component.abrirFormulario();

    expect(mockService.listar).toHaveBeenCalled();
  });

  describe('toggleDetalhe', () => {
    it('deve buscar e exibir o detalhe do item selecionado', () => {
      mockService.listar.and.returnValue(of(pageVazia));
      mockService.buscarPorId.and.returnValue(of(item));
      const component = criarComponente();
      const event = jasmine.createSpyObj('Event', ['stopPropagation']);

      component.toggleDetalhe(item, event);

      expect(event.stopPropagation).toHaveBeenCalled();
      expect(mockService.buscarPorId).toHaveBeenCalledWith(1);
      expect(component.detalhe()).toEqual(item);
      expect(component.loadingDetalhe()).toBeFalse();
    });

    it('deve fechar o detalhe quando o mesmo item e clicado novamente', () => {
      mockService.listar.and.returnValue(of(pageVazia));
      mockService.buscarPorId.and.returnValue(of(item));
      const component = criarComponente();
      const event = jasmine.createSpyObj('Event', ['stopPropagation']);
      component.toggleDetalhe(item, event);

      component.toggleDetalhe(item, event);

      expect(component.detalhe()).toBeNull();
    });

    it('deve exibir snackbar e limpar o detalhe quando falha ao buscar', () => {
      mockService.listar.and.returnValue(of(pageVazia));
      mockService.buscarPorId.and.returnValue(throwError(() => ({ error: { erro: 'Item nao encontrado' } })));
      const component = criarComponente();
      const event = jasmine.createSpyObj('Event', ['stopPropagation']);

      component.toggleDetalhe(item, event);

      expect(component.detalhe()).toBeNull();
      expect(component.loadingDetalhe()).toBeFalse();
      expect(mockSnackBar.open).toHaveBeenCalledWith('Item nao encontrado', 'Fechar', { duration: 4000 });
    });
  });
});
