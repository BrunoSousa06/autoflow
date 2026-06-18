import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { CurrencyPipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { ServicoService } from '../servicos/servico.service';
import { ServicoResponse } from '../servicos/servico.model';
import { PecaInsumoService } from '../peca-insumo/peca-insumo.service';
import { PecaInsumoResponse } from '../peca-insumo/peca-insumo.model';
import { CriarReparoAdicionalRequest } from './reparo-adicional.model';

export interface CriarReparoAdicionalDialogData {
  numeroOs: string;
}

interface ItemRow {
  pecaInsumoId: number | null;
  quantidade: number;
}

@Component({
  selector: 'app-criar-reparo-adicional-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CurrencyPipe,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDividerModule,
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>construction</mat-icon>
      Criar Reparo Adicional
    </h2>
    <p class="subtitulo">OS {{ data.numeroOs }}</p>

    <mat-dialog-content>
      @if (carregando()) {
        <div class="loading">
          <mat-spinner diameter="36" />
          <span>Carregando catálogo...</span>
        </div>
      } @else {

        <section class="secao">
          <h3>Serviço</h3>
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Selecione o serviço</mat-label>
            <mat-select [(ngModel)]="servicoId" name="servico">
              <mat-option *ngFor="let s of servicos()" [value]="s.id">
                {{ s.nome }} — {{ s.valor != null ? (s.valor | currency:'BRL') : 'Sob consulta' }}
              </mat-option>
            </mat-select>
          </mat-form-field>
        </section>

        <mat-divider />

        <section class="secao">
          <h3>Peças e Insumos <small>(ao menos 1 obrigatório)</small></h3>

          <div class="itens-lista">
            @for (item of itens; track $index) {
              <div class="item-linha">
                <mat-form-field appearance="outline" class="item-peca">
                  <mat-label>Peça / Insumo</mat-label>
                  <mat-select [(ngModel)]="item.pecaInsumoId" [name]="'peca-' + $index">
                    <mat-option *ngFor="let p of pecasInsumos()" [value]="p.id">
                      {{ p.nome }} ({{ p.tipo }}) — Estoque: {{ p.quantidade }}
                    </mat-option>
                  </mat-select>
                </mat-form-field>

                <mat-form-field appearance="outline" class="item-qtd">
                  <mat-label>Qtd.</mat-label>
                  <input
                    matInput
                    type="number"
                    min="1"
                    [(ngModel)]="item.quantidade"
                    [name]="'qtd-' + $index"
                  >
                </mat-form-field>

                <button
                  mat-icon-button
                  color="warn"
                  type="button"
                  (click)="removerItem($index)"
                  [disabled]="itens.length === 1"
                  matTooltip="Remover"
                >
                  <mat-icon>delete</mat-icon>
                </button>
              </div>
            }
          </div>

          <button mat-stroked-button type="button" (click)="adicionarItem()" class="btn-add-item">
            <mat-icon>add</mat-icon>
            Adicionar peça/insumo
          </button>
        </section>

      }
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="cancelar()">Cancelar</button>
      <button
        mat-flat-button
        color="primary"
        type="button"
        [disabled]="!podeSalvar || carregando()"
        (click)="confirmar()"
      >
        <mat-icon>send</mat-icon>
        Criar e enviar para aprovação
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    h2[mat-dialog-title] { display: flex; align-items: center; gap: 8px; }
    .subtitulo { margin: -8px 24px 8px; color: #666; font-size: 0.85rem; }

    mat-dialog-content { min-width: 480px; max-height: 520px; padding-top: 8px; }

    .loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      padding: 32px 0;
      color: #666;
    }

    .secao { padding: 12px 0; }
    .secao h3 { margin: 0 0 12px; font-size: 0.95rem; color: #333; }
    .secao h3 small { font-weight: 400; color: #999; font-size: 0.8rem; }

    .full-width { width: 100%; }

    .itens-lista { display: flex; flex-direction: column; gap: 8px; }

    .item-linha {
      display: flex;
      align-items: flex-start;
      gap: 8px;
    }
    .item-peca { flex: 1; }
    .item-qtd { width: 90px; }

    .btn-add-item { margin-top: 4px; }

    @media (max-width: 540px) {
      mat-dialog-content { min-width: unset; }
      .item-linha { flex-wrap: wrap; }
    }
  `],
})
export class CriarReparoAdicionalDialogComponent implements OnInit {
  readonly dialogRef = inject(MatDialogRef<CriarReparoAdicionalDialogComponent>);
  readonly data = inject<CriarReparoAdicionalDialogData>(MAT_DIALOG_DATA);

  private readonly servicoService = inject(ServicoService);
  private readonly pecaInsumoService = inject(PecaInsumoService);

  readonly carregando = signal(true);
  readonly servicos = signal<ServicoResponse[]>([]);
  readonly pecasInsumos = signal<PecaInsumoResponse[]>([]);

  servicoId: number | null = null;
  itens: ItemRow[] = [{ pecaInsumoId: null, quantidade: 1 }];

  get podeSalvar(): boolean {
    return (
      this.servicoId != null &&
      this.itens.length > 0 &&
      this.itens.every(i => i.pecaInsumoId != null && i.quantidade >= 1)
    );
  }

  ngOnInit(): void {
    forkJoin({
      servicos: this.servicoService.listar(0, 100),
      pecas: this.pecaInsumoService.listar(0, 100),
    }).subscribe({
      next: ({ servicos, pecas }) => {
        this.servicos.set(servicos.content);
        this.pecasInsumos.set(pecas.content);
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }

  adicionarItem(): void {
    this.itens = [...this.itens, { pecaInsumoId: null, quantidade: 1 }];
  }

  removerItem(index: number): void {
    this.itens = this.itens.filter((_, i) => i !== index);
  }

  confirmar(): void {
    if (!this.servicoId || !this.podeSalvar) return;

    const req: CriarReparoAdicionalRequest = {
      servicos: [{
        servicoId: this.servicoId,
        itensNecessarios: this.itens
          .filter(i => i.pecaInsumoId != null)
          .map(i => ({ pecaInsumoId: i.pecaInsumoId!, quantidade: i.quantidade })),
      }],
    };

    this.dialogRef.close(req);
  }

  cancelar(): void {
    this.dialogRef.close(null);
  }
}
