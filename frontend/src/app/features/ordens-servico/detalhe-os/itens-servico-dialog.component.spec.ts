import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { ItensServicoDialogComponent, ItensServicoDialogData } from './itens-servico-dialog.component';
import { PecaInsumoService } from '../../peca-insumo/peca-insumo.service';
import { PecaInsumoResponse } from '../../peca-insumo/peca-insumo.model';

describe('ItensServicoDialogComponent', () => {
  let mockPecaInsumoService: jasmine.SpyObj<PecaInsumoService>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<ItensServicoDialogComponent>>;

  const pageVazia = { content: [] as PecaInsumoResponse[], page: { totalElements: 0, totalPages: 0, number: 0, size: 200 } };

  function criarComponente(data: ItensServicoDialogData): ItensServicoDialogComponent {
    TestBed.configureTestingModule({
      providers: [
        { provide: PecaInsumoService, useValue: mockPecaInsumoService },
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data },
      ],
    });

    return TestBed.runInInjectionContext(() => new ItensServicoDialogComponent());
  }

  beforeEach(() => {
    mockPecaInsumoService = jasmine.createSpyObj('PecaInsumoService', ['listar']);
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
  });

  it('deve inicializar as linhas a partir dos itens atuais e carregar o catalogo', () => {
    mockPecaInsumoService.listar.and.returnValue(of({
      content: [{ id: 1, nome: 'Filtro', valor: 20, quantidade: 10, tipo: 'PECA' }],
      page: { totalElements: 1, totalPages: 1, number: 0, size: 200 },
    }));

    const component = criarComponente({
      numeroOs: 'OS-001',
      servicoId: 1,
      nomeServico: 'Troca de oleo',
      itensAtuais: [{
        pecaInsumoId: 1, nome: 'Filtro', tipo: 'PECA', valorUnitario: 20, quantidade: 2, valorTotal: 40,
        status: 'DISPONIVEL', motivoPendencia: null, quantidadeDisponivel: 10, mensagemStatus: null,
      }],
    });

    component.ngOnInit();

    expect(component.linhas.length).toBe(1);
    expect(component.linhas[0].pecaInsumoId).toBe(1);
    expect(component.linhas[0].quantidade).toBe(2);
    expect(component.pecas().length).toBe(1);
    expect(component.carregando()).toBeFalse();
  });

  it('deve setar mensagem de erro quando falha ao carregar o catalogo', () => {
    mockPecaInsumoService.listar.and.returnValue(throwError(() => ({ error: { erro: 'Falha no catalogo' } })));
    const component = criarComponente({ numeroOs: 'OS-001', servicoId: 1, nomeServico: 'Servico', itensAtuais: [] });

    component.ngOnInit();

    expect(component.erroCarregamento()).toBe('Falha no catalogo');
    expect(component.carregando()).toBeFalse();
  });

  it('adicionarLinha deve incluir uma nova linha vazia', () => {
    mockPecaInsumoService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente({ numeroOs: 'OS-001', servicoId: 1, nomeServico: 'Servico', itensAtuais: [] });
    component.ngOnInit();

    component.adicionarLinha();

    expect(component.linhas.length).toBe(1);
    expect(component.linhas[0].pecaInsumoId).toBeNull();
    expect(component.linhas[0].quantidade).toBe(1);
  });

  it('removerLinha deve remover a linha pelo uid', () => {
    mockPecaInsumoService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente({ numeroOs: 'OS-001', servicoId: 1, nomeServico: 'Servico', itensAtuais: [] });
    component.ngOnInit();
    component.adicionarLinha();
    const uid = component.linhas[0].uid;

    component.removerLinha(uid);

    expect(component.linhas.length).toBe(0);
  });

  it('linhasValidas deve filtrar linhas sem peca selecionada ou com quantidade invalida', () => {
    mockPecaInsumoService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente({ numeroOs: 'OS-001', servicoId: 1, nomeServico: 'Servico', itensAtuais: [] });
    component.ngOnInit();
    component.linhas = [
      { uid: 1, pecaInsumoId: 10, quantidade: 2 },
      { uid: 2, pecaInsumoId: null, quantidade: 1 },
      { uid: 3, pecaInsumoId: 20, quantidade: 0 },
    ];

    expect(component.linhasValidas()).toEqual([{ pecaInsumoId: 10, quantidade: 2 }]);
  });

  it('confirmar deve fechar o dialog com as linhas validas', () => {
    mockPecaInsumoService.listar.and.returnValue(of(pageVazia));
    const component = criarComponente({ numeroOs: 'OS-001', servicoId: 1, nomeServico: 'Servico', itensAtuais: [] });
    component.ngOnInit();
    component.linhas = [{ uid: 1, pecaInsumoId: 10, quantidade: 3 }];

    component.confirmar();

    expect(mockDialogRef.close).toHaveBeenCalledWith([{ pecaInsumoId: 10, quantidade: 3 }]);
  });
});
