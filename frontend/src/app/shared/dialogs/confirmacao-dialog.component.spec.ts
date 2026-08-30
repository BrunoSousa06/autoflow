import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ConfirmacaoDialogComponent, ConfirmacaoDialogData } from './confirmacao-dialog.component';

describe('ConfirmacaoDialogComponent', () => {
  function criarComponente(data: ConfirmacaoDialogData): ConfirmacaoDialogComponent {
    TestBed.configureTestingModule({
      providers: [{ provide: MAT_DIALOG_DATA, useValue: data }],
    });

    return TestBed.runInInjectionContext(() => new ConfirmacaoDialogComponent());
  }

  it('deve expor os dados injetados via MAT_DIALOG_DATA', () => {
    const data: ConfirmacaoDialogData = {
      titulo: 'Confirmar acao',
      mensagem: 'Deseja confirmar?',
      labelConfirmar: 'Confirmar',
    };

    const component = criarComponente(data);

    expect(component.data).toEqual(data);
  });

  it('deve funcionar sem labelConfirmar informado', () => {
    const data: ConfirmacaoDialogData = {
      titulo: 'Titulo',
      mensagem: 'Mensagem',
    };

    const component = criarComponente(data);

    expect(component.data.labelConfirmar).toBeUndefined();
  });
});
