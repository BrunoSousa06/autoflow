import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import {
  STATUS_ORCAMENTO_LABEL,
  STATUS_OS_LABEL,
  StatusOrdemServico,
} from '../../ordens-servico/ordem-servico.model';
import { AcompanhamentoOrdemServicoResponse } from '../minha-conta.model';
import { MinhaContaService } from '../minha-conta.service';

@Component({
  selector: 'app-minhas-ordens',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  template: `
    <section class="section">
      <header class="section-header">
        <div>
          <h2>Minhas ordens</h2>
          <p>Acompanhe o andamento das suas ordens de servico.</p>
        </div>
        <button mat-stroked-button color="primary" type="button" (click)="carregar()">
          <mat-icon>refresh</mat-icon>
          Atualizar
        </button>
      </header>

      <mat-form-field appearance="outline" class="status-filter">
        <mat-label>Status</mat-label>
        <mat-select name="status" [(ngModel)]="statusFiltro">
          <mat-option value="">Todos</mat-option>
          <mat-option *ngFor="let status of statusOptions" [value]="status">
            {{ statusLabel(status) }}
          </mat-option>
        </mat-select>
      </mat-form-field>

      <div *ngIf="loading()" class="loading">
        <mat-spinner diameter="32"></mat-spinner>
      </div>

      <div *ngIf="!loading() && !ordensFiltradas().length" class="empty">
        Nenhuma ordem encontrada.
      </div>

      <div *ngIf="!loading() && ordensFiltradas().length" class="grid">
        <mat-card *ngFor="let ordem of ordensFiltradas()" class="card">
          <mat-card-header>
            <mat-card-title>OS {{ ordem.numeroOs }}</mat-card-title>
            <mat-card-subtitle>Veiculo {{ ordem.placa }}</mat-card-subtitle>
          </mat-card-header>

          <mat-card-content>
            <span class="status" [ngClass]="ordem.statusAtual.toLowerCase()">{{ statusLabel(ordem.statusAtual) }}</span>
            <p>{{ ordem.mensagemParaCliente }}</p>

            <dl>
              <div><dt>Abertura</dt><dd>{{ ordem.dataAbertura | date:'short' }}</dd></div>
              <div><dt>Atualizacao</dt><dd>{{ ordem.ultimaAtualizacao | date:'short' }}</dd></div>
              <div><dt>Servicos</dt><dd>{{ ordem.servicosSolicitados.length }}</dd></div>
              <div *ngIf="ordem.orcamentoAtual">
                <dt>Orcamento</dt>
                <dd>{{ orcamentoStatus(ordem) }} - {{ ordem.orcamentoAtual.totalGeral | currency:'BRL' }}</dd>
              </div>
            </dl>
          </mat-card-content>

          <mat-card-actions align="end">
            <button mat-button color="primary" type="button" (click)="abrirDetalhe(ordem.numeroOs)">Detalhes</button>
            <button
              *ngIf="ordem.orcamentoAtual"
              mat-button
              color="primary"
              type="button"
              (click)="abrirOrcamento(ordem.orcamentoAtual.id)">
              Ver orcamento
            </button>
          </mat-card-actions>
        </mat-card>
      </div>
    </section>
  `,
  styles: [`
    .section { display: grid; gap: 14px; }
    .section-header { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; }
    h2 { margin: 0; font-size: 1.35rem; }
    p { margin: 4px 0 0; color: #666; }
    .status-filter { max-width: 260px; }
    .loading, .empty { display: flex; justify-content: center; padding: 28px; color: #666; }
    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(290px, 1fr)); gap: 14px; }
    .card { border-radius: 8px; }
    .status { display: inline-flex; margin: 8px 0 10px; padding: 4px 10px; border-radius: 999px; font-size: .78rem; font-weight: 700; background: #eef2f7; }
    .recebida { background: #e8f0fe; color: #174ea6; }
    .em_diagnostico, .aguardando_aprovacao { background: #fff8e1; color: #8a5a00; }
    .em_execucao { background: #f3e5f5; color: #6a1b9a; }
    .finalizada, .entregue { background: #e6f4ea; color: #137333; }
    dl { margin: 12px 0 0; display: grid; gap: 8px; }
    dl div { display: flex; justify-content: space-between; gap: 12px; }
    dt { color: #666; }
    dd { margin: 0; font-weight: 600; text-align: right; }
    @media (max-width: 700px) { .section-header { flex-direction: column; } }
  `],
})
export class MinhasOrdensComponent implements OnInit {
  private readonly service = inject(MinhaContaService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  readonly statusOptions: StatusOrdemServico[] = [
    'RECEBIDA',
    'EM_DIAGNOSTICO',
    'AGUARDANDO_APROVACAO',
    'EM_EXECUCAO',
    'FINALIZADA',
    'ENTREGUE',
  ];
  readonly ordens = signal<AcompanhamentoOrdemServicoResponse[]>([]);
  readonly loading = signal(false);

  statusFiltro: StatusOrdemServico | '' = '';

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.service.listarMinhasOrdens()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ordens => this.ordens.set(ordens),
        error: erro => this.exibirErro(erro, 'Nao foi possivel carregar suas ordens.'),
      });
  }

  abrirDetalhe(numeroOs: string): void {
    this.router.navigate(['/minha-conta/minhas-ordens', numeroOs]);
  }

  abrirOrcamento(orcamentoId: number): void {
    this.router.navigate(['/orcamentos', orcamentoId]);
  }

  ordensFiltradas(): AcompanhamentoOrdemServicoResponse[] {
    return this.statusFiltro
      ? this.ordens().filter(ordem => ordem.statusAtual === this.statusFiltro)
      : this.ordens();
  }

  statusLabel(status: StatusOrdemServico): string {
    return STATUS_OS_LABEL[status] ?? status;
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
