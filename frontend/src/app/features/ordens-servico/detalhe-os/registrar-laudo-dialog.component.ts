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
  templateUrl: './registrar-laudo-dialog.component.html',
  styleUrl: './registrar-laudo-dialog.component.scss',
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
