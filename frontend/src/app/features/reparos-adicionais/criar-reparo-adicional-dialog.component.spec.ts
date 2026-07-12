import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { CriarReparoAdicionalDialogComponent, CriarReparoAdicionalDialogData } from './criar-reparo-adicional-dialog.component';
import { ServicoService } from '../servicos/servico.service';
import { PecaInsumoService } from '../peca-insumo/peca-insumo.service';
import { ServicoResponse } from '../servicos/servico.model';
import { PecaInsumoResponse } from '../peca-insumo/peca-insumo.model';

describe('CriarReparoAdicionalDialogComponent', () => {
  let mockServicoService: jasmine.SpyObj<ServicoService>;
  let mockPecaInsumoService: jasmine.SpyObj<PecaInsumoService>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<CriarReparoAdicionalDialogComponent>>;

  const servico = (id: number): ServicoResponse => ({ id, nome: `Servico ${id}`, descricao: '', valor: 100, ativo: true });
  const peca = (id: number): PecaInsumoResponse => ({ id, nome: `Peca ${id}`, valor: 10, quantidade: 5, tipo: 'PECA' });

  function criarComponente(data: CriarReparoAdicionalDialogData): CriarReparoAdicionalDialogComponent {
    TestBed.configureTestingModule({
      providers: [
        { provide: ServicoService, useValue: mockServicoService },
        { provide: PecaInsumoService, useValue: mockPecaInsumoService },
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data },
      ],
    });

    return TestBed.runInInjectionContext(() => new CriarReparoAdicionalDialogComponent());
  }

  beforeEach(() => {
    mockServicoService = jasmine.createSpyObj('ServicoService', ['listar']);
    mockPecaInsumoService = jasmine.createSpyObj('PecaInsumoService', ['listar']);
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
  });

  it('deve carregar servicos (excluindo os ja na OS) e pecas/insumos ao inicializar', () => {
    mockServicoService.listar.and.returnValue(of({
      content: [servico(1), servico(2)],
      page: { totalElements: 2, totalPages: 1, number: 0, size: 100 },
    }));
    mockPecaInsumoService.listar.and.returnValue(of({
      content: [peca(10)],
      page: { totalElements: 1, totalPages: 1, number: 0, size: 100 },
    }));

    const component = criarComponente({ numeroOs: 'OS-001', servicosJaNaOs: [2] });

    component.ngOnInit();

    expect(component.servicos().map(s => s.id)).toEqual([1]);
    expect(component.pecasInsumos().map(p => p.id)).toEqual([10]);
    expect(component.carregando()).toBeFalse();
  });

  it('deve parar de carregar quando algum dos forkJoin falha', () => {
    mockServicoService.listar.and.returnValue(throwError(() => ({})));
    mockPecaInsumoService.listar.and.returnValue(of({ content: [], page: { totalElements: 0, totalPages: 0, number: 0, size: 100 } }));

    const component = criarComponente({ numeroOs: 'OS-001', servicosJaNaOs: [] });

    component.ngOnInit();

    expect(component.carregando()).toBeFalse();
  });

  describe('podeSalvar', () => {
    beforeEach(() => {
      mockServicoService.listar.and.returnValue(of({ content: [], page: { totalElements: 0, totalPages: 0, number: 0, size: 100 } }));
      mockPecaInsumoService.listar.and.returnValue(of({ content: [], page: { totalElements: 0, totalPages: 0, number: 0, size: 100 } }));
    });

    it('deve ser false quando nenhum servico foi selecionado', () => {
      const component = criarComponente({ numeroOs: 'OS-001', servicosJaNaOs: [] });
      component.ngOnInit();
      component.itens = [{ pecaInsumoId: 1, quantidade: 1 }];

      expect(component.podeSalvar).toBeFalse();
    });

    it('deve ser false quando algum item nao tem peca selecionada ou quantidade invalida', () => {
      const component = criarComponente({ numeroOs: 'OS-001', servicosJaNaOs: [] });
      component.ngOnInit();
      component.servicoId = 1;
      component.itens = [{ pecaInsumoId: null, quantidade: 1 }];

      expect(component.podeSalvar).toBeFalse();
    });

    it('deve ser true quando servico e todos os itens estao validos', () => {
      const component = criarComponente({ numeroOs: 'OS-001', servicosJaNaOs: [] });
      component.ngOnInit();
      component.servicoId = 1;
      component.itens = [{ pecaInsumoId: 10, quantidade: 2 }];

      expect(component.podeSalvar).toBeTrue();
    });
  });

  describe('acoes', () => {
    beforeEach(() => {
      mockServicoService.listar.and.returnValue(of({ content: [], page: { totalElements: 0, totalPages: 0, number: 0, size: 100 } }));
      mockPecaInsumoService.listar.and.returnValue(of({ content: [], page: { totalElements: 0, totalPages: 0, number: 0, size: 100 } }));
    });

    it('adicionarItem deve incluir uma nova linha vazia', () => {
      const component = criarComponente({ numeroOs: 'OS-001', servicosJaNaOs: [] });
      component.ngOnInit();

      component.adicionarItem();

      expect(component.itens.length).toBe(2);
      expect(component.itens[1]).toEqual({ pecaInsumoId: null, quantidade: 1 });
    });

    it('removerItem deve remover o item pelo indice', () => {
      const component = criarComponente({ numeroOs: 'OS-001', servicosJaNaOs: [] });
      component.ngOnInit();
      component.adicionarItem();

      component.removerItem(0);

      expect(component.itens.length).toBe(1);
    });

    it('confirmar nao deve fechar o dialog quando podeSalvar e false', () => {
      const component = criarComponente({ numeroOs: 'OS-001', servicosJaNaOs: [] });
      component.ngOnInit();

      component.confirmar();

      expect(mockDialogRef.close).not.toHaveBeenCalled();
    });

    it('confirmar deve fechar o dialog com a requisição montada quando valido', () => {
      const component = criarComponente({ numeroOs: 'OS-001', servicosJaNaOs: [] });
      component.ngOnInit();
      component.servicoId = 1;
      component.itens = [{ pecaInsumoId: 10, quantidade: 3 }];

      component.confirmar();

      expect(mockDialogRef.close).toHaveBeenCalledWith({
        servicos: [{
          servicoId: 1,
          itensNecessarios: [{ pecaInsumoId: 10, quantidade: 3 }],
        }],
      });
    });

    it('cancelar deve fechar o dialog com null', () => {
      const component = criarComponente({ numeroOs: 'OS-001', servicosJaNaOs: [] });
      component.ngOnInit();

      component.cancelar();

      expect(mockDialogRef.close).toHaveBeenCalledWith(null);
    });
  });
});
