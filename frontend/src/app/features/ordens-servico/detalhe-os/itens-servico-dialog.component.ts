import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PecaInsumoResponse } from '../../../features/peca-insumo/peca-insumo.model';
import { PecaInsumoService } from '../../../features/peca-insumo/peca-insumo.service';
import { ItemNecessarioOs, ItensNecessariosRequest } from '../ordem-servico.model';

export interface ItensServicoDialogData {
  numeroOs: string;
  servicoId: number;
  nomeServico: string;
  itensAtuais: ItemNecessarioOs[];
}

interface LinhaItem {
  uid: number;
  pecaInsumoId: number | null;
  quantidade: number;
}

@Component({
  selector: 'app-itens-servico-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon class="titulo-icon">inventory_2</mat-icon>
      Itens Necessários
    </h2>

    <mat-dialog-content>
      <p class="sub">
        Serviço: <strong>{{ data.nomeServico }}</strong>
        &nbsp;·&nbsp; OS: <strong>{{ data.numeroOs }}</strong>
      </p>

      @if (carregando()) {
        <div class="loading">
          <mat-spinner diameter="32" />
          <span>Carregando catálogo de peças e insumos...</span>
        </div>
      } @else if (erroCarregamento()) {
        <div class="erro-inline">
          <mat-icon color="warn">error_outline</mat-icon>
          <span>{{ erroCarregamento() }}</span>
        </div>
      } @else {
        <div class="linhas-container">
          @for (linha of linhas; track linha.uid) {
            <div class="linha-item">
              <mat-form-field appearance="outline" class="campo-peca">
                <mat-label>Peça / Insumo</mat-label>
                <mat-select [(ngModel)]="linha.pecaInsumoId" [name]="'peca-' + linha.uid">
                  @for (p of pecas(); track p.id) {
                    <mat-option [value]="p.id">
                      {{ p.nome }}
                      <span class="peca-info">· {{ p.tipo }} · Estoque: {{ p.quantidade }}</span>
                    </mat-option>
                  }
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline" class="campo-qtd">
                <mat-label>Qtd.</mat-label>
                <input
                  matInput
                  type="number"
                  [(ngModel)]="linha.quantidade"
                  [name]="'qtd-' + linha.uid"
                  min="1"
                />
              </mat-form-field>

              <button mat-icon-button color="warn" (click)="removerLinha(linha.uid)" matTooltip="Remover">
                <mat-icon>delete</mat-icon>
              </button>
            </div>
          }

          @if (linhas.length === 0) {
            <p class="sem-itens">Nenhum item adicionado. Clique em "Adicionar Item" para começar.</p>
          }
        </div>

        <button mat-stroked-button (click)="adicionarLinha()" class="btn-adicionar">
          <mat-icon>add</mat-icon>
          Adicionar Item
        </button>
      }
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="null">Cancelar</button>
      <button
        mat-raised-button
        color="primary"
        [disabled]="carregando() || !linhasValidas().length"
        (click)="confirmar()"
      >
        <mat-icon>save</mat-icon>
        Salvar Itens
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

    .linhas-container {
      display: flex;
      flex-direction: column;
      gap: 8px;
      min-height: 40px;
    }

    .linha-item {
      display: flex;
      align-items: flex-start;
      gap: 8px;
    }

    .campo-peca {
      flex: 1;
    }

    .campo-qtd {
      width: 80px;
      flex-shrink: 0;
    }

    .peca-info {
      font-size: 0.75rem;
      color: #888;
      margin-left: 4px;
    }

    .sem-itens {
      color: #999;
      font-size: 0.875rem;
      font-style: italic;
      margin: 8px 0;
      text-align: center;
    }

    .btn-adicionar {
      margin-top: 8px;
      display: flex;
      align-items: center;
      gap: 6px;
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
export class ItensServicoDialogComponent implements OnInit {
  private readonly pecaInsumoService = inject(PecaInsumoService);
  private readonly dialogRef = inject(MatDialogRef<ItensServicoDialogComponent>);
  readonly data = inject<ItensServicoDialogData>(MAT_DIALOG_DATA);

  readonly pecas = signal<PecaInsumoResponse[]>([]);
  readonly carregando = signal(true);
  readonly erroCarregamento = signal<string | null>(null);

  linhas: LinhaItem[] = [];
  private nextUid = 0;

  ngOnInit(): void {
    this.linhas = this.data.itensAtuais.map(item => ({
      uid: this.nextUid++,
      pecaInsumoId: item.pecaInsumoId,
      quantidade: item.quantidade,
    }));

    this.pecaInsumoService.listar(0, 200).subscribe({
      next: (page) => {
        this.pecas.set(page.content);
        this.carregando.set(false);
      },
      error: (err) => {
        const raw = err?.error?.erro ?? 'Erro ao carregar catálogo de peças e insumos.';
        this.erroCarregamento.set(typeof raw === 'string' ? raw : 'Erro ao carregar catálogo.');
        this.carregando.set(false);
      },
    });
  }

  adicionarLinha(): void {
    this.linhas = [...this.linhas, { uid: this.nextUid++, pecaInsumoId: null, quantidade: 1 }];
  }

  removerLinha(uid: number): void {
    this.linhas = this.linhas.filter(l => l.uid !== uid);
  }

  linhasValidas(): ItensNecessariosRequest[] {
    return this.linhas
      .filter(l => l.pecaInsumoId !== null && l.quantidade >= 1)
      .map(l => ({ pecaInsumoId: l.pecaInsumoId!, quantidade: l.quantidade }));
  }

  confirmar(): void {
    const validas = this.linhasValidas();
    this.dialogRef.close(validas);
  }
}
