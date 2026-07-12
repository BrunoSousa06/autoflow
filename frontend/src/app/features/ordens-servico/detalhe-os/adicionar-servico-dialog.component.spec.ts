import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { AdicionarServicoDiagnosticoDialogComponent, AdicionarServicoDiagnosticoDialogData } from './adicionar-servico-dialog.component';
import { ServicoService } from '../../servicos/servico.service';
import { ServicoResponse } from '../../servicos/servico.model';

describe('AdicionarServicoDiagnosticoDialogComponent', () => {
  let mockServicoService: jasmine.SpyObj<ServicoService>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<AdicionarServicoDiagnosticoDialogComponent>>;

  const servico = (id: number): ServicoResponse => ({ id, nome: `Servico ${id}`, descricao: '', valor: 100, ativo: true });

  function criarComponente(data: AdicionarServicoDiagnosticoDialogData): AdicionarServicoDiagnosticoDialogComponent {
    TestBed.configureTestingModule({
      providers: [
        { provide: ServicoService, useValue: mockServicoService },
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data },
      ],
    });

    return TestBed.runInInjectionContext(() => new AdicionarServicoDiagnosticoDialogComponent());
  }

  beforeEach(() => {
    mockServicoService = jasmine.createSpyObj('ServicoService', ['listar']);
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
  });

  it('deve carregar servicos disponiveis excluindo os ja adicionados', () => {
    mockServicoService.listar.and.returnValue(of({
      content: [servico(1), servico(2), servico(3)],
      page: { totalElements: 3, totalPages: 1, number: 0, size: 100 },
    }));

    const component = criarComponente({ numeroOs: 'OS-001', servicosJaAdicionados: [2] });

    component.ngOnInit();

    expect(component.servicos().map(s => s.id)).toEqual([1, 3]);
    expect(component.carregando()).toBeFalse();
  });

  it('deve parar de carregar quando ocorre erro', () => {
    mockServicoService.listar.and.returnValue(throwError(() => ({})));
    const component = criarComponente({ numeroOs: 'OS-001', servicosJaAdicionados: [] });

    component.ngOnInit();

    expect(component.carregando()).toBeFalse();
  });

  it('toggle deve adicionar e remover o id do conjunto de selecionados', () => {
    mockServicoService.listar.and.returnValue(of({ content: [], page: { totalElements: 0, totalPages: 0, number: 0, size: 100 } }));
    const component = criarComponente({ numeroOs: 'OS-001', servicosJaAdicionados: [] });
    component.ngOnInit();

    component.toggle(5);
    expect(component.selecionados().has(5)).toBeTrue();

    component.toggle(5);
    expect(component.selecionados().has(5)).toBeFalse();
  });

  it('confirmar deve fechar o dialog com os servicos selecionados', () => {
    mockServicoService.listar.and.returnValue(of({ content: [], page: { totalElements: 0, totalPages: 0, number: 0, size: 100 } }));
    const component = criarComponente({ numeroOs: 'OS-001', servicosJaAdicionados: [] });
    component.ngOnInit();
    component.toggle(7);

    component.confirmar();

    expect(mockDialogRef.close).toHaveBeenCalledWith([{ servicoId: 7 }]);
  });

  it('cancelar deve fechar o dialog com null', () => {
    mockServicoService.listar.and.returnValue(of({ content: [], page: { totalElements: 0, totalPages: 0, number: 0, size: 100 } }));
    const component = criarComponente({ numeroOs: 'OS-001', servicosJaAdicionados: [] });
    component.ngOnInit();

    component.cancelar();

    expect(mockDialogRef.close).toHaveBeenCalledWith(null);
  });
});
