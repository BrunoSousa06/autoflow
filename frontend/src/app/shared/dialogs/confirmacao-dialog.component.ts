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
  templateUrl: './confirmacao-dialog.component.html',
  styleUrl: './confirmacao-dialog.component.scss',
})
export class ConfirmacaoDialogComponent {
  readonly data = inject<ConfirmacaoDialogData>(MAT_DIALOG_DATA);
}
