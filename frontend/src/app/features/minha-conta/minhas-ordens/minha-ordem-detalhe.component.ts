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
  templateUrl: './minha-ordem-detalhe.component.html',
  styleUrl: './minha-ordem-detalhe.component.scss',
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
