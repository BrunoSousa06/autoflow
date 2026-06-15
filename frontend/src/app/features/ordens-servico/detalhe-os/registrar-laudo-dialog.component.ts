import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';

export interface RegistrarLaudoDialogData {
  numeroOs: string;
  laudoAtual: string | null;
}

export interface RegistrarLaudoDialogResult {
  laudo: string;
}

@Component({
  selector: 'app-registrar-laudo-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon class="titulo-icon">description</mat-icon>
      Registrar Laudo de Diagnóstico
    </h2>

    <mat-dialog-content>
      <p class="sub">OS: <strong>{{ data.numeroOs }}</strong></p>

      <mat-form-field appearance="outline" class="campo-laudo">
        <mat-label>Laudo técnico</mat-label>
        <textarea
          matInput
          [(ngModel)]="laudo"
          name="laudo"
          rows="6"
          placeholder="Descreva o diagnóstico técnico do veículo..."
        ></textarea>
        <mat-hint>Descreva os problemas identificados e os serviços necessários.</mat-hint>
      </mat-form-field>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="null">Cancelar</button>
      <button
        mat-raised-button
        color="primary"
        [disabled]="!laudo.trim()"
        (click)="confirmar()"
      >
        <mat-icon>save</mat-icon>
        Salvar Laudo
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
        color: #1976d2;
      }
    }

    .sub {
      margin: 0 0 16px;
      font-size: 0.875rem;
      color: #666;
    }

    .campo-laudo {
      width: 100%;
    }

    mat-dialog-actions {
      gap: 8px;
      padding-top: 8px;

      button {
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }
  `],
})
export class RegistrarLaudoDialogComponent implements OnInit {
  private readonly dialogRef = inject(MatDialogRef<RegistrarLaudoDialogComponent>);
  readonly data = inject<RegistrarLaudoDialogData>(MAT_DIALOG_DATA);

  laudo = '';

  ngOnInit(): void {
    this.laudo = this.data.laudoAtual ?? '';
  }

  confirmar(): void {
    const texto = this.laudo.trim();
    if (!texto) return;
    this.dialogRef.close({ laudo: texto });
  }
}
