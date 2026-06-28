import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UsuarioResponse, UsuarioService } from '../../../core/services/usuario.service';

export interface AtribuirMecanicoDialogData {
  numeroOs: string;
}

export interface AtribuirMecanicoDialogResult {
  mecanicoId: number;
}

@Component({
  selector: 'app-atribuir-mecanico-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './atribuir-mecanico-dialog.component.html',
  styleUrl: './atribuir-mecanico-dialog.component.scss',
})
export class AtribuirMecanicoDialogComponent implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly dialogRef = inject(MatDialogRef<AtribuirMecanicoDialogComponent>);
  readonly data = inject<AtribuirMecanicoDialogData>(MAT_DIALOG_DATA);

  readonly mecanicos = signal<UsuarioResponse[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);

  mecanicoSelecionadoId: number | null = null;

  ngOnInit(): void {
    this.usuarioService.listarMecanicos().subscribe({
      next: (lista) => {
        this.mecanicos.set(lista);
        this.carregando.set(false);
      },
      error: (err) => {
        const raw = err?.error?.erro ?? 'Erro ao carregar mecânicos.';
        this.erro.set(typeof raw === 'string' ? raw : 'Erro ao carregar mecânicos.');
        this.carregando.set(false);
      },
    });
  }

  confirmar(): void {
    if (!this.mecanicoSelecionadoId) return;
    this.dialogRef.close({ mecanicoId: this.mecanicoSelecionadoId });
  }
}
