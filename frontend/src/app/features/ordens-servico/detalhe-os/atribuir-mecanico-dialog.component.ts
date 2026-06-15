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
  template: `
    <h2 mat-dialog-title>
      <mat-icon class="titulo-icon">engineering</mat-icon>
      Atribuir Mecânico
    </h2>

    <mat-dialog-content>
      <p class="sub">OS: <strong>{{ data.numeroOs }}</strong></p>

      @if (carregando()) {
        <div class="loading">
          <mat-spinner diameter="32" />
          <span>Carregando mecânicos...</span>
        </div>
      } @else if (erro()) {
        <div class="erro-inline">
          <mat-icon color="warn">error_outline</mat-icon>
          <span>{{ erro() }}</span>
        </div>
      } @else {
        <mat-form-field appearance="outline" class="campo-mecanico">
          <mat-label>Selecionar mecânico</mat-label>
          <mat-select [(ngModel)]="mecanicoSelecionadoId" name="mecanico">
            @for (m of mecanicos(); track m.id) {
              <mat-option [value]="m.id">{{ m.nome }} — {{ m.email }}</mat-option>
            }
          </mat-select>
          @if (mecanicos().length === 0) {
            <mat-hint>Nenhum mecânico cadastrado no sistema.</mat-hint>
          }
        </mat-form-field>
      }
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="null">Cancelar</button>
      <button
        mat-raised-button
        color="primary"
        [disabled]="!mecanicoSelecionadoId || carregando()"
        (click)="confirmar()"
      >
        <mat-icon>check</mat-icon>
        Confirmar
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

    .loading {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px 0;
      color: #666;
      font-size: 0.875rem;
    }

    .erro-inline {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #c62828;
      font-size: 0.875rem;
      padding: 8px 0;
    }

    .campo-mecanico {
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
