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
  templateUrl: './minhas-ordens.component.html',
  styleUrl: './minhas-ordens.component.scss',
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
