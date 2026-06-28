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
  templateUrl: './orcamento-detalhe.component.html',
  styleUrl: './orcamento-detalhe.component.scss',
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
      this.snackBar.open('Orçamento inválido.', 'Fechar', { duration: 4000 });
      this.voltar();
      return;
    }

    this.loading.set(true);
    this.service.buscarPorId(id)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: orcamento => this.orcamento.set(orcamento),
        error: erro => this.exibirErro(erro, 'Não foi possível carregar o orçamento.'),
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
          this.snackBar.open('Orçamento aprovado com sucesso.', 'Fechar', { duration: 3000 });
        },
        error: erro => this.exibirErro(erro, 'Não foi possível aprovar o orçamento.'),
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
            this.snackBar.open('Orçamento recusado com sucesso.', 'Fechar', { duration: 3000 });
          },
          error: erro => this.exibirErro(erro, 'Não foi possível recusar o orçamento.'),
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
    if (tipo === 'PECA') return 'Peça';
    if (tipo === 'INSUMO') return 'Insumo';
    return tipo;
  }

  baixarPdf(orcamento: OrcamentoResponse): void {
    this.acao.set(true);
    this.service.baixarPdf(orcamento.id)
      .pipe(finalize(() => this.acao.set(false)))
      .subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `orcamento-${orcamento.id}.pdf`;
          a.click();
          URL.revokeObjectURL(url);
        },
        error: (erro) => this.exibirErro(erro, 'Não foi possível baixar o PDF.'),
      });
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
