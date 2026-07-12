import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { RecusarOrcamentoDialogComponent, RecusarOrcamentoDialogData } from './recusar-orcamento-dialog.component';

describe('RecusarOrcamentoDialogComponent', () => {
  function criarComponente(data: RecusarOrcamentoDialogData): RecusarOrcamentoDialogComponent {
    TestBed.configureTestingModule({
      providers: [{ provide: MAT_DIALOG_DATA, useValue: data }],
    });

    return TestBed.runInInjectionContext(() => new RecusarOrcamentoDialogComponent());
  }

  it('deve expor os dados injetados via MAT_DIALOG_DATA', () => {
    const component = criarComponente({ numeroOs: 'OS-001' });

    expect(component.data).toEqual({ numeroOs: 'OS-001' });
  });

  it('deve iniciar o motivo vazio', () => {
    const component = criarComponente({ numeroOs: 'OS-001' });

    expect(component.motivo).toBe('');
  });
});
