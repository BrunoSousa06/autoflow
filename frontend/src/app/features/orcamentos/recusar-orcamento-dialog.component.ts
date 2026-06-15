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
  template: `
    <h2 mat-dialog-title>Recusar orcamento</h2>

    <mat-dialog-content>
      <p>Informe o motivo da recusa para a OS {{ data.numeroOs }}.</p>
      <mat-form-field appearance="outline">
        <mat-label>Motivo</mat-label>
        <textarea matInput rows="4" [(ngModel)]="motivo"></textarea>
      </mat-form-field>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="null">Cancelar</button>
      <button mat-raised-button color="warn" [mat-dialog-close]="motivo">Recusar</button>
    </mat-dialog-actions>
  `,
  styles: [`
    mat-dialog-content {
      display: grid;
      gap: 12px;
      min-width: min(420px, 78vw);
    }

    p {
      margin: 0;
      color: #555;
    }
  `],
})
export class RecusarOrcamentoDialogComponent {
  readonly data = inject<RecusarOrcamentoDialogData>(MAT_DIALOG_DATA);
  motivo = '';
}
