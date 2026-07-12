import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { RegistrarLaudoDialogComponent, RegistrarLaudoDialogData } from './registrar-laudo-dialog.component';

describe('RegistrarLaudoDialogComponent', () => {
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<RegistrarLaudoDialogComponent>>;

  function criarComponente(data: RegistrarLaudoDialogData): RegistrarLaudoDialogComponent {
    TestBed.configureTestingModule({
      providers: [
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data },
      ],
    });

    return TestBed.runInInjectionContext(() => new RegistrarLaudoDialogComponent());
  }

  beforeEach(() => {
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
  });

  it('deve inicializar o campo laudo com o laudoAtual informado', () => {
    const component = criarComponente({ numeroOs: 'OS-001', laudoAtual: 'Laudo existente' });

    component.ngOnInit();

    expect(component.laudo).toBe('Laudo existente');
  });

  it('deve inicializar o campo laudo vazio quando laudoAtual e null', () => {
    const component = criarComponente({ numeroOs: 'OS-001', laudoAtual: null });

    component.ngOnInit();

    expect(component.laudo).toBe('');
  });

  it('confirmar nao deve fechar o dialog quando o laudo esta vazio', () => {
    const component = criarComponente({ numeroOs: 'OS-001', laudoAtual: null });
    component.laudo = '   ';

    component.confirmar();

    expect(mockDialogRef.close).not.toHaveBeenCalled();
  });

  it('confirmar deve fechar o dialog com o laudo (trim aplicado)', () => {
    const component = criarComponente({ numeroOs: 'OS-001', laudoAtual: null });
    component.laudo = '  Laudo com espacos  ';

    component.confirmar();

    expect(mockDialogRef.close).toHaveBeenCalledWith({ laudo: 'Laudo com espacos' });
  });
});
