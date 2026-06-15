import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import {
  STATUS_ORCAMENTO_LABEL,
  STATUS_OS_LABEL,
  STATUS_SERVICO_OS_LABEL,
  StatusOrdemServico,
  StatusServicoOs,
} from '../../ordens-servico/ordem-servico.model';
import { AcompanhamentoOrdemServicoResponse } from '../minha-conta.model';
import { MinhaContaService } from '../minha-conta.service';

@Component({
  selector: 'app-minha-ordem-detalhe',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <section class="detail">
      <button mat-button type="button" class="back" (click)="voltar()">
        <mat-icon>arrow_back</mat-icon>
        Voltar
      </button>

      <div *ngIf="loading()" class="loading">
        <mat-spinner diameter="32"></mat-spinner>
      </div>

      <ng-container *ngIf="!loading() && ordem() as item">
        <header class="header">
          <div>
            <h2>OS {{ item.numeroOs }}</h2>
            <p>Veiculo {{ item.placa }}</p>
          </div>
          <span class="status" [ngClass]="item.statusAtual.toLowerCase()">{{ statusLabel(item.statusAtual) }}</span>
        </header>

        <mat-card>
          <mat-card-content>
            <p class="message">{{ item.mensagemParaCliente }}</p>
            <dl>
              <div><dt>Abertura</dt><dd>{{ item.dataAbertura | date:'short' }}</dd></div>
              <div><dt>Ultima atualizacao</dt><dd>{{ item.ultimaAtualizacao | date:'short' }}</dd></div>
            </dl>
          </mat-card-content>
        </mat-card>

        <mat-card *ngIf="item.orcamentoAtual as orcamento">
          <mat-card-header>
            <mat-card-title>Orcamento</mat-card-title>
            <mat-card-subtitle>{{ orcamento.mensagem }}</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <dl>
              <div><dt>Status</dt><dd>{{ orcamentoStatus(item) }}</dd></div>
              <div><dt>Servicos</dt><dd>{{ orcamento.totalServicos | currency:'BRL' }}</dd></div>
              <div><dt>Pecas/Insumos</dt><dd>{{ orcamento.totalItens | currency:'BRL' }}</dd></div>
              <div><dt>Total</dt><dd>{{ orcamento.totalGeral | currency:'BRL' }}</dd></div>
            </dl>
          </mat-card-content>
          <mat-card-actions align="end">
            <button mat-button color="primary" type="button" (click)="abrirOrcamento(orcamento.id)">
              Ver orcamento
            </button>
          </mat-card-actions>
        </mat-card>

        <mat-card>
          <mat-card-header>
            <mat-card-title>Servicos solicitados</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div *ngIf="item.servicosSolicitados.length; else semServicos" class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Servico</th>
                    <th>Status</th>
                    <th class="money">Valor</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let servico of item.servicosSolicitados">
                    <td>{{ servico.nome }}</td>
                    <td>{{ servicoStatusLabel(servico.status) }}</td>
                    <td class="money">{{ servico.valor | currency:'BRL' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <ng-template #semServicos>
              <p class="muted">Nenhum servico vinculado.</p>
            </ng-template>
          </mat-card-content>
        </mat-card>

        <mat-card>
          <mat-card-header>
            <mat-card-title>Historico</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <ol *ngIf="item.historicoStatus.length; else semHistorico" class="timeline">
              <li *ngFor="let historico of item.historicoStatus">
                <strong>{{ statusLabel(historico.status) }}</strong>
                <span>{{ historico.registradoEm | date:'short' }}</span>
                <p>{{ historico.mensagemCliente }}</p>
              </li>
            </ol>
            <ng-template #semHistorico>
              <p class="muted">Nenhum historico disponivel.</p>
            </ng-template>
          </mat-card-content>
        </mat-card>
      </ng-container>
    </section>
  `,
  styles: [`
    .detail { display: grid; gap: 14px; }
    .back { width: fit-content; }
    .header { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; }
    h2 { margin: 0; font-size: 1.35rem; }
    p { margin: 4px 0 0; color: #666; }
    mat-card { border-radius: 8px; }
    .loading { display: flex; justify-content: center; padding: 28px; }
    .message { margin: 0 0 14px; color: #333; }
    .status { padding: 6px 12px; border-radius: 999px; font-weight: 700; background: #eef2f7; }
    .recebida { background: #e8f0fe; color: #174ea6; }
    .em_diagnostico, .aguardando_aprovacao { background: #fff8e1; color: #8a5a00; }
    .em_execucao { background: #f3e5f5; color: #6a1b9a; }
    .finalizada, .entregue { background: #e6f4ea; color: #137333; }
    dl { margin: 0; display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; }
    dt { color: #666; }
    dd { margin: 3px 0 0; font-weight: 600; }
    .table-wrap { overflow-x: auto; }
    table { width: 100%; min-width: 520px; border-collapse: collapse; }
    th, td { padding: 10px 8px; border-bottom: 1px solid #e0e0e0; text-align: left; }
    th { color: #666; font-weight: 600; }
    .money { text-align: right; white-space: nowrap; }
    .timeline { margin: 0; padding-left: 20px; display: grid; gap: 12px; }
    .timeline span { display: block; color: #666; font-size: .85rem; margin-top: 2px; }
    .timeline p, .muted { margin: 4px 0 0; color: #666; }
    @media (max-width: 700px) { .header { flex-direction: column; } }
  `],
})
export class MinhaOrdemDetalheComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(MinhaContaService);
  private readonly snackBar = inject(MatSnackBar);

  readonly ordem = signal<AcompanhamentoOrdemServicoResponse | null>(null);
  readonly loading = signal(false);

  ngOnInit(): void {
    const numeroOs = this.route.snapshot.paramMap.get('numeroOs');
    if (!numeroOs) {
      this.voltar();
      return;
    }

    this.loading.set(true);
    this.service.buscarMinhaOrdem(numeroOs)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ordem => {
          if (!ordem) {
            this.snackBar.open('Ordem de servico nao encontrada para este cliente.', 'Fechar', { duration: 5000 });
            this.voltar();
            return;
          }
          this.ordem.set(ordem);
        },
        error: erro => this.exibirErro(erro, 'Nao foi possivel carregar a ordem de servico.'),
      });
  }

  voltar(): void {
    this.router.navigate(['/minha-conta/minhas-ordens']);
  }

  abrirOrcamento(orcamentoId: number): void {
    this.router.navigate(['/orcamentos', orcamentoId]);
  }

  statusLabel(status: StatusOrdemServico): string {
    return STATUS_OS_LABEL[status] ?? status;
  }

  servicoStatusLabel(status: StatusServicoOs): string {
    return STATUS_SERVICO_OS_LABEL[status] ?? status;
  }

  orcamentoStatus(ordem: AcompanhamentoOrdemServicoResponse): string {
    const status = ordem.orcamentoAtual?.status;
    if (!status) return '-';
    if (status === 'DISPONIVEL') return 'Aguardando aprovacao';
    return STATUS_ORCAMENTO_LABEL[status] ?? status;
  }

  private exibirErro(erro: any, fallback: string): void {
    const mensagem = erro?.error?.erro ?? erro?.error?.message ?? erro?.error;
    this.snackBar.open(typeof mensagem === 'string' ? mensagem : fallback, 'Fechar', { duration: 5000 });
  }
}
