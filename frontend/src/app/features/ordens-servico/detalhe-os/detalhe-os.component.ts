import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../../core/services/auth.service';
import { OrdemServicoService } from '../ordem-servico.service';
import {
  OrdemServicoDetalheResponse,
  StatusOrdemServico,
  StatusServicoOs,
  StatusOrcamento,
  STATUS_OS_LABEL,
  STATUS_SERVICO_OS_LABEL,
  STATUS_ORCAMENTO_LABEL,
} from '../ordem-servico.model';
import {
  AtribuirMecanicoDialogComponent,
  AtribuirMecanicoDialogResult,
} from './atribuir-mecanico-dialog.component';
import {
  ConfirmacaoDialogComponent,
  ConfirmacaoDialogData,
} from '../../../shared/dialogs/confirmacao-dialog.component';
import {
  RegistrarLaudoDialogComponent,
  RegistrarLaudoDialogResult,
} from './registrar-laudo-dialog.component';
import {
  ItensServicoDialogComponent,
} from './itens-servico-dialog.component';
import {
  AdicionarServicoDiagnosticoDialogComponent,
} from './adicionar-servico-dialog.component';
import { ItensNecessariosRequest, ServicoOsResponse, ServicoSolicitadoRequest } from '../ordem-servico.model';
import { OrcamentoService } from '../../orcamentos/orcamento.service';
import { RecusarOrcamentoDialogComponent } from '../../orcamentos/recusar-orcamento-dialog.component';
import { ReparoAdicionalService } from '../../reparos-adicionais/reparo-adicional.service';
import {
  CriarReparoAdicionalDialogComponent,
  CriarReparoAdicionalDialogData,
} from '../../reparos-adicionais/criar-reparo-adicional-dialog.component';
import { CriarReparoAdicionalRequest } from '../../reparos-adicionais/reparo-adicional.model';

type PassoTimeline = 'concluido' | 'atual' | 'pendente';

@Component({
  selector: 'app-detalhe-os',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    CurrencyPipe,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatDividerModule,
  ],
  templateUrl: './detalhe-os.component.html',
  styleUrl: './detalhe-os.component.scss',
})
export class DetalheOsComponent implements OnInit {
  private readonly service = inject(OrdemServicoService);
  private readonly orcamentoService = inject(OrcamentoService);
  private readonly reparoAdicionalService = inject(ReparoAdicionalService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly os = signal<OrdemServicoDetalheResponse | null>(null);
  readonly loading = signal(true);
  readonly erro = signal<string | null>(null);
  readonly salvando = signal(false);

  readonly role = this.auth.getRole();
  readonly loggedEmail = this.auth.getUsuarioLogado()?.email ?? null;
  readonly podeVerPainelAtendente = ['ADMIN', 'ATENDENTE'].includes(this.role ?? '');
  readonly podeVerPainelMecanico  = ['ADMIN', 'MECANICO'].includes(this.role ?? '');
  readonly podeVerPainelCliente   = ['ADMIN', 'CLIENTE'].includes(this.role ?? '');

  // ADMIN sempre pode; MECANICO só pode se for o mecânico atribuído à OS
  readonly podeAlterarDiagnostico = computed(() => {
    if (this.role === 'ADMIN') return true;
    if (this.role !== 'MECANICO') return false;
    const mecEmail = this.os()?.diagnostico?.mecanicoEmail ?? null;
    return mecEmail !== null && this.loggedEmail !== null && mecEmail.toLowerCase() === this.loggedEmail.toLowerCase();
  });

  readonly ORDEM_STATUS: StatusOrdemServico[] = [
    'RECEBIDA', 'EM_DIAGNOSTICO', 'AGUARDANDO_APROVACAO',
    'EM_EXECUCAO', 'FINALIZADA', 'ENTREGUE',
  ];

  readonly STATUS_INFO: Record<StatusOrdemServico, { label: string; icon: string }> = {
    RECEBIDA:             { label: 'Recebida',           icon: 'inbox' },
    EM_DIAGNOSTICO:       { label: 'Em Diagnóstico',     icon: 'search' },
    AGUARDANDO_APROVACAO: { label: 'Aguard. Aprovação',  icon: 'pending' },
    EM_EXECUCAO:          { label: 'Em Execução',        icon: 'build' },
    FINALIZADA:           { label: 'Finalizada',         icon: 'done_all' },
    ENTREGUE:             { label: 'Entregue',           icon: 'local_shipping' },
  };

  ngOnInit(): void {
    const numeroOs = this.route.snapshot.paramMap.get('numeroOs');
    if (!numeroOs) {
      this.router.navigate(['/ordens-servico']);
      return;
    }

    this.service.buscarPorNumeroOs(numeroOs).subscribe({
      next: (os) => {
        this.os.set(os);
        this.loading.set(false);
      },
      error: (err) => {
        const raw = err?.error?.erro ?? err?.error ?? 'Não foi possível carregar a ordem de serviço.';
        this.erro.set(typeof raw === 'string' ? raw : 'Erro ao carregar ordem de serviço.');
        this.loading.set(false);
      },
    });
  }

  passoStatus(step: StatusOrdemServico): PassoTimeline {
    const osAtual = this.os();
    if (!osAtual) return 'pendente';
    const idx = this.ORDEM_STATUS.indexOf(osAtual.status);
    const i   = this.ORDEM_STATUS.indexOf(step);
    if (i < idx) return 'concluido';
    if (i === idx) return 'atual';
    return 'pendente';
  }

  labelStatusOs(status: StatusOrdemServico): string {
    return STATUS_OS_LABEL[status] ?? status;
  }

  labelStatusServico(status: StatusServicoOs): string {
    return STATUS_SERVICO_OS_LABEL[status] ?? status;
  }

  labelStatusOrcamento(status: StatusOrcamento): string {
    return STATUS_ORCAMENTO_LABEL[status] ?? status;
  }

  tooltipDiagnostico(statusNecessario: StatusOrdemServico): string {
    const os = this.os();
    if (!os) return '';
    if (os.status !== statusNecessario) return '';
    if (this.podeAlterarDiagnostico()) return '';

    if (!os.diagnostico?.mecanicoEmail) {
      return 'Atribua um mecânico antes de alterar o diagnóstico.';
    }

    return 'Somente o mecânico atribuído pode alterar o diagnóstico.';
  }

  iniciarDiagnostico(): void {
    const ref = this.dialog.open<ConfirmacaoDialogComponent, ConfirmacaoDialogData, boolean>(
      ConfirmacaoDialogComponent,
      {
        width: '400px',
        data: {
          titulo: 'Iniciar Diagnóstico',
          mensagem: `Confirma o início do diagnóstico da OS ${this.os()!.numeroOs}? O status será alterado para Em Diagnóstico.`,
          labelConfirmar: 'Iniciar',
        },
      },
    );
    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) return;
      this.salvando.set(true);
      this.service.iniciarDiagnostico(this.os()!.numeroOs).subscribe({
        next: () => {
          this.snackBar.open('Diagnóstico iniciado.', 'Fechar', { duration: 3000 });
          this.recarregar();
        },
        error: (err) => {
          const raw = err?.error?.erro ?? err?.error?.message ?? 'Erro ao iniciar diagnóstico.';
          this.snackBar.open(typeof raw === 'string' ? raw : 'Erro ao iniciar diagnóstico.', 'Fechar', { duration: 5000 });
          this.salvando.set(false);
        },
      });
    });
  }

  abrirRegistrarLaudo(): void {
    const ref = this.dialog.open(RegistrarLaudoDialogComponent, {
      width: '540px',
      disableClose: true,
      data: {
        numeroOs: this.os()!.numeroOs,
        laudoAtual: this.os()!.diagnostico?.laudo ?? null,
      },
    });
    ref.afterClosed().subscribe((resultado: RegistrarLaudoDialogResult | null) => {
      if (!resultado) return;
      this.salvando.set(true);
      this.service.registrarLaudo(this.os()!.numeroOs, { laudo: resultado.laudo }).subscribe({
        next: () => {
          this.snackBar.open('Laudo registrado com sucesso.', 'Fechar', { duration: 3000 });
          this.recarregar();
        },
        error: (err) => {
          const raw = err?.error?.erro ?? err?.error?.message ?? 'Erro ao registrar laudo.';
          this.snackBar.open(typeof raw === 'string' ? raw : 'Erro ao registrar laudo.', 'Fechar', { duration: 5000 });
          this.salvando.set(false);
        },
      });
    });
  }

  abrirAdicionarServico(): void {
    const os = this.os()!;
    const servicosJaAdicionados = os.servicos.map((s) => s.servicoId);
    const ref = this.dialog.open(AdicionarServicoDiagnosticoDialogComponent, {
      width: '480px',
      disableClose: true,
      data: { numeroOs: os.numeroOs, servicosJaAdicionados },
    });
    ref.afterClosed().subscribe((servicos: ServicoSolicitadoRequest[] | null) => {
      if (!servicos || servicos.length === 0) return;
      this.salvando.set(true);
      this.service.incluirServicos(os.numeroOs, servicos).subscribe({
        next: () => {
          this.snackBar.open('Serviço(s) adicionado(s) com sucesso.', 'Fechar', { duration: 3000 });
          this.recarregar();
        },
        error: (err) => {
          const raw = err?.error?.erro ?? err?.error?.message ?? 'Erro ao adicionar serviços.';
          this.snackBar.open(typeof raw === 'string' ? raw : 'Erro ao adicionar serviços.', 'Fechar', { duration: 5000 });
          this.salvando.set(false);
        },
      });
    });
  }

  abrirItensServico(srv: ServicoOsResponse): void {
    const ref = this.dialog.open(ItensServicoDialogComponent, {
      width: '600px',
      disableClose: true,
      data: {
        numeroOs: this.os()!.numeroOs,
        servicoId: srv.servicoId,
        nomeServico: srv.nome,
        itensAtuais: srv.itensNecessarios,
      },
    });
    ref.afterClosed().subscribe((itens: ItensNecessariosRequest[] | null) => {
      if (!itens) return;
      this.salvando.set(true);
      this.service.registrarItensServico(this.os()!.numeroOs, srv.servicoId, itens).subscribe({
        next: () => {
          this.snackBar.open('Itens registrados com sucesso.', 'Fechar', { duration: 3000 });
          this.recarregar();
        },
        error: (err) => {
          const raw = err?.error?.erro ?? err?.error?.message ?? 'Erro ao registrar itens.';
          this.snackBar.open(typeof raw === 'string' ? raw : 'Erro ao registrar itens.', 'Fechar', { duration: 5000 });
          this.salvando.set(false);
        },
      });
    });
  }

  confirmarFinalizarDiagnostico(): void {
    const ref = this.dialog.open<ConfirmacaoDialogComponent, ConfirmacaoDialogData, boolean>(
      ConfirmacaoDialogComponent,
      {
        width: '440px',
        data: {
          titulo: 'Finalizar Diagnóstico',
          mensagem: 'Isso irá gerar o orçamento automaticamente e notificar o cliente por e-mail. O laudo deve estar registrado. Confirma?',
          labelConfirmar: 'Finalizar',
        },
      },
    );
    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) return;
      this.salvando.set(true);
      this.service.finalizarDiagnostico(this.os()!.numeroOs).subscribe({
        next: () => {
          this.snackBar.open(
            `Diagnóstico finalizado. Orçamento gerado e enviado ao cliente.`,
            'Fechar',
            { duration: 5000 },
          );
          this.recarregar();
        },
        error: (err) => {
          const raw = err?.error?.erro ?? err?.error?.message ?? 'Erro ao finalizar diagnóstico.';
          this.snackBar.open(typeof raw === 'string' ? raw : 'Erro ao finalizar diagnóstico.', 'Fechar', { duration: 5000 });
          this.salvando.set(false);
        },
      });
    });
  }

  abrirAtribuirMecanico(): void {
    const ref = this.dialog.open(AtribuirMecanicoDialogComponent, {
      width: '440px',
      disableClose: true,
      data: { numeroOs: this.os()!.numeroOs },
    });

    ref.afterClosed().subscribe((resultado: AtribuirMecanicoDialogResult | null) => {
      if (!resultado) return;
      this.salvando.set(true);
      this.service.atribuirMecanico(this.os()!.numeroOs, { mecanicoId: resultado.mecanicoId }).subscribe({
        next: () => {
          this.snackBar.open('Mecânico atribuído com sucesso.', 'Fechar', { duration: 3000 });
          this.recarregar();
        },
        error: (err) => {
          const raw = err?.error?.erro ?? 'Erro ao atribuir mecânico.';
          this.snackBar.open(typeof raw === 'string' ? raw : 'Erro ao atribuir mecânico.', 'Fechar', { duration: 4000 });
          this.salvando.set(false);
        },
      });
    });
  }

  confirmarEntrega(): void {
    const os = this.os()!;
    const ref = this.dialog.open<ConfirmacaoDialogComponent, ConfirmacaoDialogData, boolean>(
      ConfirmacaoDialogComponent,
      {
        width: '400px',
        data: {
          titulo: 'Confirmar entrega',
          mensagem: `Confirma a entrega do veículo ${os.veiculo.placa} ao cliente ${os.cliente.nome}?`,
          labelConfirmar: 'Entregar',
        },
      }
    );

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) return;
      this.salvando.set(true);
      this.service.entregar(os.numeroOs).subscribe({
        next: () => {
          this.snackBar.open('Veículo entregue com sucesso.', 'Fechar', { duration: 3000 });
          this.recarregar();
        },
        error: (err) => {
          const raw = err?.error?.erro ?? 'Erro ao registrar entrega.';
          this.snackBar.open(typeof raw === 'string' ? raw : 'Erro ao registrar entrega.', 'Fechar', { duration: 4000 });
          this.salvando.set(false);
        },
      });
    });
  }

  iniciarExecucaoServico(srv: ServicoOsResponse): void {
    const ref = this.dialog.open<ConfirmacaoDialogComponent, ConfirmacaoDialogData, boolean>(
      ConfirmacaoDialogComponent,
      {
        width: '400px',
        data: {
          titulo: 'Iniciar Serviço',
          mensagem: `Confirma o início da execução de "${srv.nome}"?`,
          labelConfirmar: 'Iniciar',
        },
      },
    );
    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) return;
      this.salvando.set(true);
      this.service.iniciarServico(this.os()!.numeroOs, srv.servicoId).subscribe({
        next: () => {
          this.snackBar.open(`Serviço "${srv.nome}" iniciado.`, 'Fechar', { duration: 3000 });
          this.recarregar();
        },
        error: (err) => {
          const raw = err?.error?.erro ?? err?.error?.message ?? 'Erro ao iniciar serviço.';
          this.snackBar.open(typeof raw === 'string' ? raw : 'Erro ao iniciar serviço.', 'Fechar', { duration: 5000 });
          this.salvando.set(false);
        },
      });
    });
  }

  finalizarExecucaoServico(srv: ServicoOsResponse): void {
    const ref = this.dialog.open<ConfirmacaoDialogComponent, ConfirmacaoDialogData, boolean>(
      ConfirmacaoDialogComponent,
      {
        width: '400px',
        data: {
          titulo: 'Finalizar Serviço',
          mensagem: `Confirma a conclusão de "${srv.nome}"? Se for o último serviço, a OS será finalizada automaticamente.`,
          labelConfirmar: 'Finalizar',
        },
      },
    );
    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) return;
      this.salvando.set(true);
      this.service.finalizarServico(this.os()!.numeroOs, srv.servicoId).subscribe({
        next: (result) => {
          const osStatus = result.servicos.every((s) => s.status === 'FINALIZADO')
            ? 'Serviço finalizado. OS finalizada automaticamente.'
            : `Serviço "${srv.nome}" finalizado.`;
          this.snackBar.open(osStatus, 'Fechar', { duration: 4000 });
          this.recarregar();
        },
        error: (err) => {
          const raw = err?.error?.erro ?? err?.error?.message ?? 'Erro ao finalizar serviço.';
          this.snackBar.open(typeof raw === 'string' ? raw : 'Erro ao finalizar serviço.', 'Fechar', { duration: 5000 });
          this.salvando.set(false);
        },
      });
    });
  }

  baixarPdfOrcamento(orcamentoId: number): void {
    this.salvando.set(true);
    this.orcamentoService.baixarPdf(orcamentoId).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `orcamento-${orcamentoId}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
        this.salvando.set(false);
      },
      error: (err) => {
        const raw = err?.error?.erro ?? err?.error?.message ?? 'Erro ao baixar PDF.';
        this.snackBar.open(typeof raw === 'string' ? raw : 'Erro ao baixar PDF.', 'Fechar', { duration: 5000 });
        this.salvando.set(false);
      },
    });
  }

  aprovarOrcamento(orcamentoId: number): void {
    const ref = this.dialog.open<ConfirmacaoDialogComponent, ConfirmacaoDialogData, boolean>(
      ConfirmacaoDialogComponent,
      {
        width: '400px',
        data: {
          titulo: 'Aprovar Orçamento',
          mensagem: 'Confirma a aprovação do orçamento? A OS entrará em execução.',
          labelConfirmar: 'Aprovar',
        },
      },
    );
    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) return;
      this.salvando.set(true);
      this.orcamentoService.aprovar(orcamentoId).subscribe({
        next: () => {
          this.snackBar.open('Orçamento aprovado. OS em execução.', 'Fechar', { duration: 3000 });
          this.recarregar();
        },
        error: (err) => {
          const raw = err?.error?.erro ?? err?.error?.message ?? 'Erro ao aprovar orçamento.';
          this.snackBar.open(typeof raw === 'string' ? raw : 'Erro ao aprovar orçamento.', 'Fechar', { duration: 5000 });
          this.salvando.set(false);
        },
      });
    });
  }

  recusarOrcamento(orcamentoId: number): void {
    const ref = this.dialog.open(RecusarOrcamentoDialogComponent, {
      data: { numeroOs: this.os()!.numeroOs },
      width: '520px',
    });
    ref.afterClosed().subscribe((motivo: string | null | undefined) => {
      if (motivo === null || motivo === undefined) return;
      this.salvando.set(true);
      this.orcamentoService.recusar(orcamentoId, motivo ?? null).subscribe({
        next: () => {
          this.snackBar.open('Orçamento recusado.', 'Fechar', { duration: 3000 });
          this.recarregar();
        },
        error: (err) => {
          const raw = err?.error?.erro ?? err?.error?.message ?? 'Erro ao recusar orçamento.';
          this.snackBar.open(typeof raw === 'string' ? raw : 'Erro ao recusar orçamento.', 'Fechar', { duration: 5000 });
          this.salvando.set(false);
        },
      });
    });
  }

  criarReparoAdicional(): void {
    const os = this.os();
    if (!os) return;

    const ref = this.dialog.open(CriarReparoAdicionalDialogComponent, {
      data: { numeroOs: os.numeroOs } as CriarReparoAdicionalDialogData,
      width: '580px',
    });

    ref.afterClosed().subscribe((req: CriarReparoAdicionalRequest | null) => {
      if (!req) return;

      this.salvando.set(true);
      this.reparoAdicionalService.criar(os.numeroOs, req).subscribe({
        next: (resultado) => {
          this.salvando.set(false);
          this.snackBar.open(
            `Reparo adicional criado. Orçamento #${resultado.orcamentoId} enviado para aprovação do cliente.`,
            'Fechar',
            { duration: 6000 },
          );
          this.recarregar();
        },
        error: (err) => {
          const raw = err?.error?.erro ?? err?.error?.message ?? 'Erro ao criar reparo adicional.';
          this.snackBar.open(typeof raw === 'string' ? raw : 'Erro ao criar reparo adicional.', 'Fechar', { duration: 5000 });
          this.salvando.set(false);
        },
      });
    });
  }

  voltar(): void {
    this.router.navigate(['/ordens-servico']);
  }

  private recarregar(): void {
    this.service.buscarPorNumeroOs(this.os()!.numeroOs).subscribe({
      next: (os) => {
        this.os.set(os);
        this.salvando.set(false);
      },
      error: () => this.salvando.set(false),
    });
  }
}
