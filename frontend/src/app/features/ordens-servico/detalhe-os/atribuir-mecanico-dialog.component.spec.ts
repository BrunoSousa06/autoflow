import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { AtribuirMecanicoDialogComponent, AtribuirMecanicoDialogData } from './atribuir-mecanico-dialog.component';
import { UsuarioResponse, UsuarioService } from '../../../core/services/usuario.service';

describe('AtribuirMecanicoDialogComponent', () => {
  let mockUsuarioService: jasmine.SpyObj<UsuarioService>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<AtribuirMecanicoDialogComponent>>;

  function criarComponente(data: AtribuirMecanicoDialogData = { numeroOs: 'OS-001' }): AtribuirMecanicoDialogComponent {
    TestBed.configureTestingModule({
      providers: [
        { provide: UsuarioService, useValue: mockUsuarioService },
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data },
      ],
    });

    return TestBed.runInInjectionContext(() => new AtribuirMecanicoDialogComponent());
  }

  beforeEach(() => {
    mockUsuarioService = jasmine.createSpyObj('UsuarioService', ['listarMecanicos']);
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
  });

  it('deve carregar a lista de mecanicos ao inicializar', () => {
    const mecanicos: UsuarioResponse[] = [{ id: 1, nome: 'Joao', email: 'joao@teste.com', role: 'MECANICO' }];
    mockUsuarioService.listarMecanicos.and.returnValue(of(mecanicos));
    const component = criarComponente();

    component.ngOnInit();

    expect(component.mecanicos()).toEqual(mecanicos);
    expect(component.carregando()).toBeFalse();
  });

  it('deve setar mensagem de erro quando falha ao carregar mecanicos', () => {
    mockUsuarioService.listarMecanicos.and.returnValue(throwError(() => ({ error: { erro: 'Falha ao listar' } })));
    const component = criarComponente();

    component.ngOnInit();

    expect(component.erro()).toBe('Falha ao listar');
    expect(component.carregando()).toBeFalse();
  });

  it('deve usar mensagem padrao quando erro nao possui mensagem do backend', () => {
    mockUsuarioService.listarMecanicos.and.returnValue(throwError(() => ({})));
    const component = criarComponente();

    component.ngOnInit();

    expect(component.erro()).toBe('Erro ao carregar mecânicos.');
  });

  it('confirmar nao deve fechar o dialog quando nenhum mecanico foi selecionado', () => {
    mockUsuarioService.listarMecanicos.and.returnValue(of([]));
    const component = criarComponente();
    component.ngOnInit();

    component.confirmar();

    expect(mockDialogRef.close).not.toHaveBeenCalled();
  });

  it('confirmar deve fechar o dialog com o mecanicoId selecionado', () => {
    mockUsuarioService.listarMecanicos.and.returnValue(of([]));
    const component = criarComponente();
    component.ngOnInit();
    component.mecanicoSelecionadoId = 5;

    component.confirmar();

    expect(mockDialogRef.close).toHaveBeenCalledWith({ mecanicoId: 5 });
  });
});
