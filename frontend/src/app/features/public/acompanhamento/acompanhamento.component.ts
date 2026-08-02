import { DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { STATUS_OS_LABEL, StatusOrdemServico } from '../../ordens-servico/ordem-servico.model';
import { AcompanhamentoPublico, AcompanhamentoService } from './acompanhamento.service';
import { ConfirmacaoDialogComponent, ConfirmacaoDialogData } from '../../../shared/dialogs/confirmacao-dialog.component';

@Component({
  selector: 'app-acompanhamento',
  standalone: true,
  imports: [DatePipe, MatButtonModule, MatCardModule, MatIconModule, MatProgressSpinnerModule, MatSnackBarModule],
  templateUrl: './acompanhamento.component.html',
  styleUrl: './acompanhamento.component.scss',
})
export class AcompanhamentoComponent implements OnInit {
  acompanhamento: AcompanhamentoPublico | null = null;
  carregando = false;
  erro = '';
  atualizadoEm: Date | null = null;
  processandoOrcamento = false;
  podeTentarNovamente = false;
  private token = '';
  readonly etapas: StatusOrdemServico[] = ['RECEBIDA', 'EM_DIAGNOSTICO', 'AGUARDANDO_APROVACAO', 'EM_EXECUCAO', 'FINALIZADA', 'ENTREGUE'];

  constructor(
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly service: AcompanhamentoService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.erro = 'O link de acompanhamento está incompleto.';
      return;
    }
    this.podeTentarNovamente = true;
    this.consultar();
  }

  atualizar(): void {
    this.consultar();
  }

  private consultar(): void {
    this.carregando = true;
    this.erro = '';
    this.service.consultar(this.token).pipe(finalize(() => this.carregando = false)).subscribe({
      next: acompanhamento => {
        this.acompanhamento = acompanhamento;
        this.atualizadoEm = new Date();
      },
      error: () => this.erro = 'Este link é inválido, expirou ou foi revogado.'
    });
  }

  statusLabel(status: StatusOrdemServico): string { return STATUS_OS_LABEL[status]; }
  indiceEtapaAtual(): number { return this.acompanhamento ? this.etapas.indexOf(this.acompanhamento.status) : -1; }
  etapaConcluida(index: number): boolean { return index < this.indiceEtapaAtual(); }
  progresso(): number { return Math.round(((this.indiceEtapaAtual() + 1) / this.etapas.length) * 100); }

  baixarOrcamento(): void {
    const id = this.acompanhamento?.orcamentoId;
    if (!id) return;
    this.processandoOrcamento = true;
    this.service.baixarOrcamento(id, this.token).pipe(finalize(() => this.processandoOrcamento = false)).subscribe({
      next: arquivo => {
        const url = URL.createObjectURL(arquivo);
        const link = document.createElement('a');
        link.href = url;
        link.download = `orcamento-${id}.pdf`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.snackBar.open('Não foi possível baixar o orçamento.', 'Fechar', { duration: 5000 }),
    });
  }

  confirmarAprovacao(): void {
    const id = this.acompanhamento?.orcamentoId;
    if (!id) return;
    const ref = this.dialog.open<ConfirmacaoDialogComponent, ConfirmacaoDialogData, boolean>(ConfirmacaoDialogComponent, {
      width: '430px',
      data: {
        titulo: 'Aprovar orçamento',
        mensagem: 'Confirma a aprovação deste orçamento? Após a aprovação, a oficina poderá iniciar os serviços.',
        labelConfirmar: 'Aprovar orçamento',
      },
    });
    ref.afterClosed().subscribe(confirmado => {
      if (!confirmado) return;
      this.processandoOrcamento = true;
      this.service.aprovarOrcamento(id, this.token).pipe(finalize(() => this.processandoOrcamento = false)).subscribe({
        next: () => {
          this.snackBar.open('Orçamento aprovado com sucesso.', 'Fechar', { duration: 4000 });
          this.consultar();
        },
        error: () => this.snackBar.open('Não foi possível aprovar o orçamento.', 'Fechar', { duration: 5000 }),
      });
    });
  }

  irParaLogin(): void { this.router.navigate(['/login']); }
}
