import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { CurrencyPipe } from '@angular/common';
import { ServicoService } from '../../servicos/servico.service';
import { ServicoResponse } from '../../servicos/servico.model';
import { ServicoSolicitadoRequest } from '../ordem-servico.model';

export interface AdicionarServicoDiagnosticoDialogData {
  numeroOs: string;
  servicosJaAdicionados: number[];
}

@Component({
  selector: 'app-adicionar-servico-dialog',
  standalone: true,
  imports: [
    CommonModule,
    CurrencyPipe,
    MatDialogModule,
    MatButtonModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatDividerModule,
  ],
  template: `
    <h2 mat-dialog-title>Adicionar Serviços</h2>

    <mat-dialog-content>
      @if (carregando()) {
        <div class="loading">
          <mat-spinner diameter="36" />
          <span>Carregando serviços...</span>
        </div>
      } @else if (servicos().length === 0) {
        <p class="vazio">Nenhum serviço disponível para adicionar.</p>
      } @else {
        <p class="instrucao">Selecione os serviços a adicionar à OS durante o diagnóstico.</p>
        <div class="lista">
          @for (srv of servicos(); track srv.id) {
            <div class="servico-linha" [class.selecionado]="selecionados().has(srv.id)">
              <mat-checkbox
                [checked]="selecionados().has(srv.id)"
                (change)="toggle(srv.id)"
              >
                <div class="srv-info">
                  <span class="srv-nome">{{ srv.nome }}</span>
                  <span class="srv-valor">{{ srv.valor != null ? (srv.valor | currency:'BRL') : '—' }}</span>
                </div>
              </mat-checkbox>
            </div>
          }
        </div>
      }
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="cancelar()">Cancelar</button>
      <button
        mat-flat-button
        color="primary"
        [disabled]="selecionados().size === 0 || carregando()"
        (click)="confirmar()"
      >
        <mat-icon>add</mat-icon>
        Adicionar ({{ selecionados().size }})
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    mat-dialog-content { min-width: 360px; max-height: 420px; }

    .loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      padding: 24px 0;
      color: #666;
    }

    .vazio {
      color: #999;
      font-style: italic;
      text-align: center;
      padding: 16px 0;
    }

    .instrucao {
      font-size: 0.85rem;
      color: #666;
      margin: 0 0 12px;
    }

    .lista {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .servico-linha {
      padding: 8px 10px;
      border-radius: 6px;
      border: 1px solid #eee;
      transition: background 0.15s;

      &.selecionado {
        background: #e3f2fd;
        border-color: #90caf9;
      }
    }

    .srv-info {
      display: flex;
      align-items: center;
      gap: 12px;
      padding-left: 4px;

      .srv-nome { flex: 1; font-size: 0.9rem; color: #222; }
      .srv-valor { font-weight: 600; color: #1565c0; font-size: 0.85rem; white-space: nowrap; }
    }
  `],
})
export class AdicionarServicoDiagnosticoDialogComponent implements OnInit {
  private readonly dialogRef = inject(MatDialogRef<AdicionarServicoDiagnosticoDialogComponent>);
  private readonly data = inject<AdicionarServicoDiagnosticoDialogData>(MAT_DIALOG_DATA);
  private readonly servicoService = inject(ServicoService);

  readonly carregando = signal(true);
  readonly servicos = signal<ServicoResponse[]>([]);
  readonly selecionados = signal<Set<number>>(new Set());

  ngOnInit(): void {
    this.servicoService.listar(0, 100).subscribe({
      next: (page) => {
        const disponiveis = page.content.filter(
          (s) => !this.data.servicosJaAdicionados.includes(s.id),
        );
        this.servicos.set(disponiveis);
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }

  toggle(id: number): void {
    const set = new Set(this.selecionados());
    if (set.has(id)) {
      set.delete(id);
    } else {
      set.add(id);
    }
    this.selecionados.set(set);
  }

  confirmar(): void {
    const servicos: ServicoSolicitadoRequest[] = [...this.selecionados()].map((id) => ({ servicoId: id }));
    this.dialogRef.close(servicos);
  }

  cancelar(): void {
    this.dialogRef.close(null);
  }
}
