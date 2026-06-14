import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmacaoDialogData {
  titulo: string;
  mensagem: string;
  labelConfirmar?: string;
}

@Component({
  selector: 'app-confirmacao-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>
      <mat-icon color="warn" class="titulo-icon">warning_amber</mat-icon>
      {{ data.titulo }}
    </h2>

    <mat-dialog-content>
      <p>{{ data.mensagem }}</p>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false">Cancelar</button>
      <button mat-raised-button color="warn" [mat-dialog-close]="true">
        {{ data.labelConfirmar ?? 'Confirmar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    h2[mat-dialog-title] {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 1.1rem;

      .titulo-icon {
        font-size: 22px;
        width: 22px;
        height: 22px;
      }
    }

    mat-dialog-content p {
      color: #555;
      margin: 0;
      line-height: 1.5;
    }

    mat-dialog-actions {
      gap: 8px;
      padding-top: 8px;
    }
  `],
})
export class ConfirmacaoDialogComponent {
  readonly data = inject<ConfirmacaoDialogData>(MAT_DIALOG_DATA);
}
