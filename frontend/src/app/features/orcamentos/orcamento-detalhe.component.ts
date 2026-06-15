import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import {
  OrcamentoResponse,
  STATUS_ORCAMENTO_LABEL,
  StatusOrcamento,
  TIPO_ORCAMENTO_LABEL,
  TipoOrcamento,
} from './orcamento.model';
import { OrcamentoService } from './orcamento.service';
import { RecusarOrcamentoDialogComponent } from './recusar-orcamento-dialog.component';

@Component({
  selector: 'app-orcamento-detalhe',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <section class="page">
      <button mat-button type="button" class="back" (click)="voltar()">
        <mat-icon>arrow_back</mat-icon>
        Voltar
      </button>

      <div *ngIf="loading()" class="loading">
        <mat-spinner diameter="36"></mat-spinner>
      </div>

      <ng-container *ngIf="!loading() && orcamento() as item">
        <header class="header">
          <div>
            <h1>Orcamento OS {{ item.numeroOs }}</h1>
            <p>{{ tipoLabel(item.tipo) }} v{{ item.versao }}</p>
          </div>
          <span class="status" [ngClass]="item.status.toLowerCase()">{{ statusLabel(item.status) }}</span>
        </header>

        <mat-card class="summary">
          <mat-card-content>
            <dl>
              <div><dt>ID do orcamento</dt><dd>{{ item.id }}</dd></div>
              <div><dt>ID da ordem de servico</dt><dd>{{ item.ordemServicoId }}</dd></div>
              <div><dt>Criado em</dt><dd>{{ item.criadoEm | date:'short' }}</dd></div>
              <div><dt>Disponibilizado em</dt><dd>{{ item.disponibilizadoEm ? (item.disponibilizadoEm | date:'short') : '-' }}</dd></div>
            </dl>
          </mat-card-content>
        </mat-card>

        <div class="totals">
          <mat-card><mat-card-content><span>Servicos</span><strong>{{ item.totalServicos | currency:'BRL' }}</strong></mat-card-content></mat-card>
          <mat-card><mat-card-content><span>Pecas/Insumos</span><strong>{{ item.totalItens | currency:'BRL' }}</strong></mat-card-content></mat-card>
          <mat-card><mat-card-content><span>Total</span><strong>{{ item.totalGeral | currency:'BRL' }}</strong></mat-card-content></mat-card>
        </div>

        <mat-card>
          <mat-card-header>
            <mat-card-title>Servicos</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div *ngIf="item.servicos.length; else semServicos" class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Servico</th>
                    <th>ID catalogo</th>
                    <th class="money">Valor</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let servico of item.servicos">
                    <td>{{ servico.nome }}</td>
                    <td>{{ servico.servicoId }}</td>
                    <td class="money">{{ servico.valor | currency:'BRL' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <ng-template #semServicos>
              <p class="notice">Nenhum servico vinculado ao orcamento.</p>
            </ng-template>
          </mat-card-content>
        </mat-card>

        <mat-card>
          <mat-card-header>
            <mat-card-title>Pecas e insumos</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div *ngIf="item.itens.length; else semItens" class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Item</th>
                    <th>Tipo</th>
                    <th>Qtd.</th>
                    <th class="money">Valor unitario</th>
                    <th class="money">Total</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let peca of item.itens">
                    <td>{{ peca.nome }}</td>
                    <td>{{ tipoItemLabel(peca.tipo) }}</td>
                    <td>{{ peca.quantidade }}</td>
                    <td class="money">{{ peca.valorUnitario | currency:'BRL' }}</td>
                    <td class="money">{{ peca.valorTotal | currency:'BRL' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <ng-template #semItens>
              <p class="notice">Nenhuma peca ou insumo vinculado ao orcamento.</p>
            </ng-template>
          </mat-card-content>
        </mat-card>

        <div class="actions" *ngIf="podeAprovarRecusar()">
          <button mat-raised-button color="primary" type="button" [disabled]="acao()" (click)="aprovar(item)">
            Aprovar orcamento
          </button>
          <button mat-stroked-button color="warn" type="button" [disabled]="acao()" (click)="recusar(item)">
            Recusar orcamento
          </button>
        </div>
      </ng-container>
    </section>
  `,
  styles: [`
    .page { padding: 24px; display: grid; gap: 16px; }
    .back { width: fit-content; }
    .header { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; }
    h1 { margin: 0; font-size: 1.6rem; }
    p { margin: 4px 0 0; color: #666; }
    .loading { display: flex; justify-content: center; padding: 32px; }
    .summary, .totals mat-card, section mat-card { border-radius: 8px; }
    dl { margin: 0; display: grid; grid-template-columns: repeat(auto-fit, minmax(210px, 1fr)); gap: 12px; }
    dt, .totals span { color: #666; }
    dd { margin: 2px 0 0; font-weight: 600; }
    .totals { display: grid; grid-template-columns: repeat(auto-fit, minmax(210px, 1fr)); gap: 12px; }
    .totals mat-card-content { display: grid; gap: 4px; }
    .totals strong { font-size: 1.25rem; }
    .status { padding: 6px 12px; border-radius: 999px; font-weight: 700; background: #eef2f7; }
    .aprovado { background: #e6f4ea; color: #137333; }
    .reprovado { background: #fce8e6; color: #a50e0e; }
    .disponivel { background: #e8f0fe; color: #174ea6; }
    .substituido { background: #f1f3f4; color: #5f6368; }
    .notice { margin: 0; }
    .table-wrap { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; min-width: 560px; }
    th, td { padding: 10px 8px; border-bottom: 1px solid #e0e0e0; text-align: left; }
    th { color: #666; font-weight: 600; }
    .money { text-align: right; white-space: nowrap; }
    .actions { display: flex; gap: 10px; justify-content: flex-end; }
    @media (max-width: 700px) { .header, .actions { flex-direction: column; align-items: stretch; } .page { padding: 16px; } }
  `],
})
export class OrcamentoDetalheComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(OrcamentoService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly auth = inject(AuthService);
  private readonly dialog = inject(MatDialog);

  readonly orcamento = signal<OrcamentoResponse | null>(null);
  readonly loading = signal(false);
  readonly acao = signal(false);
  readonly podeAprovarRecusar = computed(() => {
    const role = this.auth.getRole();
    return this.orcamento()?.status === 'DISPONIVEL' && (role === 'ADMIN' || role === 'CLIENTE');
  });

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.snackBar.open('Orcamento invalido.', 'Fechar', { duration: 4000 });
      this.voltar();
      return;
    }

    this.loading.set(true);
    this.service.buscarPorId(id)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: orcamento => this.orcamento.set(orcamento),
        error: erro => this.exibirErro(erro, 'Nao foi possivel carregar o orcamento.'),
      });
  }

  voltar(): void {
    this.router.navigate(['/orcamentos']);
  }

  aprovar(orcamento: OrcamentoResponse): void {
    this.acao.set(true);
    this.service.aprovar(orcamento.id)
      .pipe(finalize(() => this.acao.set(false)))
      .subscribe({
        next: atualizado => {
          this.orcamento.set(atualizado);
          this.snackBar.open('Orcamento aprovado com sucesso.', 'Fechar', { duration: 3000 });
        },
        error: erro => this.exibirErro(erro, 'Nao foi possivel aprovar o orcamento.'),
      });
  }

  recusar(orcamento: OrcamentoResponse): void {
    const ref = this.dialog.open(RecusarOrcamentoDialogComponent, {
      data: { numeroOs: orcamento.numeroOs },
      width: '520px',
    });

    ref.afterClosed().subscribe((motivo: string | null) => {
      if (motivo === null || motivo === undefined) return;

      this.acao.set(true);
      this.service.recusar(orcamento.id, motivo)
        .pipe(finalize(() => this.acao.set(false)))
        .subscribe({
          next: atualizado => {
            this.orcamento.set(atualizado);
            this.snackBar.open('Orcamento recusado com sucesso.', 'Fechar', { duration: 3000 });
          },
          error: erro => this.exibirErro(erro, 'Nao foi possivel recusar o orcamento.'),
        });
    });
  }

  statusLabel(status: StatusOrcamento): string {
    return STATUS_ORCAMENTO_LABEL[status];
  }

  tipoLabel(tipo: TipoOrcamento): string {
    return TIPO_ORCAMENTO_LABEL[tipo];
  }

  tipoItemLabel(tipo: string): string {
    if (tipo === 'PECA') return 'Peca';
    if (tipo === 'INSUMO') return 'Insumo';
    return tipo;
  }

  private exibirErro(erro: unknown, fallback: string): void {
    const mensagem = this.extrairMensagemErro(erro) ?? fallback;
    this.snackBar.open(mensagem, 'Fechar', { duration: 5000 });
  }

  private extrairMensagemErro(erro: any): string | null {
    const payload = erro?.error?.erro ?? erro?.error?.message ?? erro?.error;
    return typeof payload === 'string' ? payload : null;
  }
}
