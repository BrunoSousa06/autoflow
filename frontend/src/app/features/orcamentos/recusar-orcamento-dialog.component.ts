import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

export interface RecusarOrcamentoDialogData {
  numeroOs: string;
}

@Component({
  selector: 'app-recusar-orcamento-dialog',
  standalone: true,
  imports: [FormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './recusar-orcamento-dialog.component.html',
  styleUrl: './recusar-orcamento-dialog.component.scss',
})
export class RecusarOrcamentoDialogComponent {
  readonly data = inject<RecusarOrcamentoDialogData>(MAT_DIALOG_DATA);
  motivo = '';
}
