import { Component, inject, OnInit, signal } from '@angular/core';
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
  ],
  template: `
    <div class="page">

      @if (loading()) {
        <div class="loading-center">
          <mat-spinner diameter="48" />
          <p>Carregando ordem de serviço...</p>
        </div>
      } @else if (erro()) {
        <mat-card class="estado-card">
          <mat-icon color="warn">error_outline</mat-icon>
          <p>{{ erro() }}</p>
          <button mat-stroked-button (click)="voltar()">
            <mat-icon>arrow_back</mat-icon>
            Voltar para lista
          </button>
        </mat-card>
      } @else if (os()) {

        <!-- Header -->
        <div class="page-header">
          <button mat-icon-button (click)="voltar()" matTooltip="Voltar para lista">
            <mat-icon>arrow_back</mat-icon>
          </button>
          <div class="header-info">
            <h1>{{ os()!.numeroOs }}</h1>
            <span class="data-abertura">
              Aberta em {{ os()!.dataAbertura | date:'dd/MM/yyyy HH:mm' }}
              &nbsp;·&nbsp;
              Atualizada em {{ os()!.ultimaAtualizacao | date:'dd/MM/yyyy HH:mm' }}
            </span>
          </div>
          <mat-chip [class]="'status-os-' + os()!.status">
            {{ labelStatusOs(os()!.status) }}
          </mat-chip>
        </div>

        <!-- Timeline -->
        <mat-card class="timeline-card">
          <div class="timeline">
            @for (step of ORDEM_STATUS; track step; let last = $last) {
              <div class="timeline-step" [class]="passoStatus(step)">
                <div class="step-dot">
                  @if (passoStatus(step) === 'concluido') {
                    <mat-icon>check</mat-icon>
                  } @else {
                    <mat-icon>{{ STATUS_INFO[step].icon }}</mat-icon>
                  }
                </div>
                <span class="step-label">{{ STATUS_INFO[step].label }}</span>
              </div>
              @if (!last) {
                <div class="timeline-connector" [class.feito]="passoStatus(step) === 'concluido'"></div>
              }
            }
          </div>
        </mat-card>

        <!-- Cards de informações -->
        <div class="cards-row">

          <!-- Cliente -->
          <mat-card class="info-card">
            <mat-card-header>
              <mat-card-title class="card-title-icon">
                <mat-icon>person</mat-icon>
                Cliente
              </mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <dl class="info-dl">
                <dt>Nome</dt><dd>{{ os()!.cliente.nome }}</dd>
                <dt>CPF/CNPJ</dt><dd>{{ os()!.cliente.cpfCnpj }}</dd>
                <dt>E-mail</dt><dd>{{ os()!.cliente.email }}</dd>
                <dt>Telefone</dt><dd>{{ os()!.cliente.telefone }}</dd>
              </dl>
            </mat-card-content>
          </mat-card>

          <!-- Veículo -->
          <mat-card class="info-card">
            <mat-card-header>
              <mat-card-title class="card-title-icon">
                <mat-icon>directions_car</mat-icon>
                Veículo
              </mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <dl class="info-dl">
                <dt>Placa</dt><dd class="placa">{{ os()!.veiculo.placa }}</dd>
                <dt>Marca</dt><dd>{{ os()!.veiculo.marca ?? '—' }}</dd>
                <dt>Modelo</dt><dd>{{ os()!.veiculo.modelo ?? '—' }}</dd>
                <dt>Ano</dt><dd>{{ os()!.veiculo.ano }}</dd>
              </dl>
            </mat-card-content>
          </mat-card>

          <!-- Orçamento -->
          @if (os()!.orcamentoAtual; as orc) {
            <mat-card class="info-card">
              <mat-card-header>
                <mat-card-title class="card-title-icon">
                  <mat-icon>receipt_long</mat-icon>
                  Orçamento{{ orc.tipo === 'ADICIONAL' ? ' Adicional' : '' }} v{{ orc.versao }}
                </mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <div class="orc-status-chip">
                  <mat-chip [class]="'status-orc-' + orc.status">
                    {{ labelStatusOrcamento(orc.status) }}
                  </mat-chip>
                </div>
                <dl class="info-dl">
                  <dt>Serviços</dt><dd>{{ orc.totalServicos | currency:'BRL' }}</dd>
                  <dt>Peças/Insumos</dt><dd>{{ orc.totalItens | currency:'BRL' }}</dd>
                  <dt>Total Geral</dt><dd class="total-geral">{{ orc.totalGeral | currency:'BRL' }}</dd>
                </dl>
                @if (orc.mensagem) {
                  <p class="orc-mensagem">{{ orc.mensagem }}</p>
                }
              </mat-card-content>
            </mat-card>
          }

        </div>

        <!-- Serviços -->
        <mat-card class="servicos-card">
          <mat-card-header>
            <mat-card-title class="card-title-icon">
              <mat-icon>build</mat-icon>
              Serviços Solicitados ({{ os()!.servicos.length }})
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            @for (srv of os()!.servicos; track srv.id) {
              <div class="servico-item">
                <div class="servico-header">
                  <span class="servico-nome">{{ srv.nome }}</span>
                  <mat-chip [class]="'status-srv-' + srv.status" class="chip-sm">
                    {{ labelStatusServico(srv.status) }}
                  </mat-chip>
                  <span class="servico-valor">{{ srv.valor | currency:'BRL' }}</span>
                </div>

                @if (srv.iniciadoEm || srv.finalizadoEm) {
                  <div class="servico-datas">
                    @if (srv.iniciadoEm) {
                      <span class="data-tag">
                        <mat-icon class="icon-xs">play_arrow</mat-icon>
                        Início: {{ srv.iniciadoEm | date:'dd/MM/yyyy HH:mm' }}
                      </span>
                    }
                    @if (srv.finalizadoEm) {
                      <span class="data-tag">
                        <mat-icon class="icon-xs">stop</mat-icon>
                        Fim: {{ srv.finalizadoEm | date:'dd/MM/yyyy HH:mm' }}
                      </span>
                    }
                  </div>
                }

                @if (srv.itensNecessarios.length) {
                  <div class="itens-necessarios">
                    <p class="itens-titulo">Itens / Peças necessários</p>
                    @for (item of srv.itensNecessarios; track item.pecaInsumoId) {
                      <div class="item-linha">
                        <span class="item-nome">{{ item.nome }}</span>
                        <span class="item-tipo">{{ item.tipo }}</span>
                        <span class="item-qtd">{{ item.quantidade }}x</span>
                        <span class="item-valor">{{ item.valorTotal | currency:'BRL' }}</span>
                        <mat-chip [class]="'status-item-' + item.status" class="chip-xs">
                          {{ item.mensagemStatus ?? item.status }}
                        </mat-chip>
                      </div>
                    }
                  </div>
                }
              </div>
            }
          </mat-card-content>
        </mat-card>

        <!-- Painéis por role -->
        <div class="paineis-row">

          @if (podeVerPainelAtendente) {
            <mat-card class="painel-card">
              <mat-card-header>
                <mat-card-title class="card-title-icon">
                  <mat-icon>support_agent</mat-icon>
                  Ações do Atendente
                </mat-card-title>
              </mat-card-header>
              <mat-card-content class="painel-acoes">
                <button
                  mat-stroked-button
                  [disabled]="salvando()"
                  (click)="abrirAtribuirMecanico()"
                >
                  @if (salvando()) {
                    <mat-spinner diameter="16" class="btn-spinner" />
                  } @else {
                    <mat-icon>engineering</mat-icon>
                  }
                  Atribuir Mecânico
                </button>
                <button
                  mat-stroked-button
                  color="primary"
                  [disabled]="os()!.status !== 'FINALIZADA' || salvando()"
                  [matTooltip]="os()!.status !== 'FINALIZADA' ? 'Disponível somente quando a OS estiver Finalizada' : ''"
                  (click)="confirmarEntrega()"
                >
                  @if (salvando()) {
                    <mat-spinner diameter="16" class="btn-spinner" />
                  } @else {
                    <mat-icon>local_shipping</mat-icon>
                  }
                  Entregar Veículo
                </button>
              </mat-card-content>
            </mat-card>
          }

          @if (podeVerPainelMecanico) {
            <mat-card class="painel-card">
              <mat-card-header>
                <mat-card-title class="card-title-icon">
                  <mat-icon>construction</mat-icon>
                  Ações do Mecânico
                </mat-card-title>
                <mat-card-subtitle>Ações disponíveis em breve</mat-card-subtitle>
              </mat-card-header>
              <mat-card-content class="painel-acoes">
                <button mat-stroked-button disabled>
                  <mat-icon>manage_search</mat-icon>
                  Iniciar Diagnóstico
                </button>
                <button mat-stroked-button disabled>
                  <mat-icon>description</mat-icon>
                  Registrar Laudo
                </button>
                <button mat-stroked-button disabled>
                  <mat-icon>fact_check</mat-icon>
                  Finalizar Diagnóstico
                </button>
                <button mat-stroked-button disabled>
                  <mat-icon>play_circle</mat-icon>
                  Iniciar / Finalizar Serviço
                </button>
              </mat-card-content>
            </mat-card>
          }

          @if (podeVerPainelCliente) {
            <mat-card class="painel-card">
              <mat-card-header>
                <mat-card-title class="card-title-icon">
                  <mat-icon>account_balance_wallet</mat-icon>
                  Aprovação de Orçamento
                </mat-card-title>
                <mat-card-subtitle>Ações disponíveis em breve</mat-card-subtitle>
              </mat-card-header>
              <mat-card-content class="painel-acoes">
                @if (os()!.orcamentoAtual; as orc) {
                  <div class="orc-resumo-cliente">
                    <span>Status: <strong>{{ labelStatusOrcamento(orc.status) }}</strong></span>
                    <span>Total: <strong>{{ orc.totalGeral | currency:'BRL' }}</strong></span>
                  </div>
                } @else {
                  <p class="sem-info">Aguardando geração do orçamento.</p>
                }
                <button mat-stroked-button color="primary" disabled>
                  <mat-icon>thumb_up</mat-icon>
                  Aprovar Orçamento
                </button>
                <button mat-stroked-button color="warn" disabled>
                  <mat-icon>thumb_down</mat-icon>
                  Reprovar Orçamento
                </button>
              </mat-card-content>
            </mat-card>
          }

        </div>

      }
    </div>
  `,
  styles: [`
    .page {
      padding: 24px;
      max-width: 1100px;
      margin: 0 auto;
    }

    /* ── Loading / erro ── */
    .loading-center {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 80px 24px;
      gap: 16px;
      color: #666;
      p { margin: 0; }
    }

    .estado-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 48px 24px;
      gap: 14px;
      text-align: center;
      mat-icon { font-size: 48px; width: 48px; height: 48px; }
      p { margin: 0; color: #555; }
      button { display: flex; align-items: center; gap: 6px; }
    }

    /* ── Header ── */
    .page-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;
    }

    .header-info {
      flex: 1;
      h1 {
        margin: 0 0 2px;
        font-size: 1.5rem;
        font-weight: 700;
        font-family: monospace;
        color: #1a1a1a;
      }
      .data-abertura {
        font-size: 0.8rem;
        color: #777;
      }
    }

    /* ── Timeline ── */
    .timeline-card {
      margin-bottom: 20px;
      padding: 20px 24px 16px;
    }

    .timeline {
      display: flex;
      align-items: flex-start;
      overflow-x: auto;
      padding-bottom: 4px;
    }

    .timeline-step {
      display: flex;
      flex-direction: column;
      align-items: center;
      min-width: 90px;
      text-align: center;
      flex-shrink: 0;
    }

    .step-dot {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 8px;
      transition: all 0.2s;

      mat-icon {
        font-size: 20px;
        width: 20px;
        height: 20px;
        color: white;
      }
    }

    .timeline-step.concluido .step-dot { background: #4caf50; }
    .timeline-step.atual .step-dot {
      background: #1976d2;
      box-shadow: 0 0 0 5px rgba(25, 118, 210, 0.18);
    }
    .timeline-step.pendente .step-dot {
      background: #e0e0e0;
      mat-icon { color: #9e9e9e; }
    }

    .step-label {
      font-size: 0.68rem;
      font-weight: 500;
      line-height: 1.3;
      max-width: 82px;
    }
    .timeline-step.concluido .step-label { color: #4caf50; }
    .timeline-step.atual .step-label     { color: #1976d2; font-weight: 700; }
    .timeline-step.pendente .step-label  { color: #aaa; }

    .timeline-connector {
      flex: 1;
      height: 3px;
      background: #e0e0e0;
      min-width: 16px;
      margin-top: 18px;
      margin-bottom: 34px;
      border-radius: 2px;
      transition: background 0.2s;
      &.feito { background: #4caf50; }
    }

    /* ── Cards de info ── */
    .cards-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(270px, 1fr));
      gap: 16px;
      margin-bottom: 20px;
    }

    .info-card mat-card-content { padding-top: 4px; }

    .card-title-icon {
      display: flex;
      align-items: center;
      gap: 8px;
      mat-icon { font-size: 20px; width: 20px; height: 20px; color: #555; }
    }

    .info-dl {
      display: grid;
      grid-template-columns: auto 1fr;
      gap: 5px 16px;
      margin: 8px 0 0;
      font-size: 0.875rem;

      dt {
        color: #888;
        font-weight: 500;
        white-space: nowrap;
      }
      dd {
        margin: 0;
        color: #222;
        word-break: break-word;
      }
    }

    .placa {
      font-family: monospace;
      font-weight: 700;
      letter-spacing: 1px;
      color: #1565c0 !important;
    }

    .orc-status-chip { margin: 6px 0 8px; }

    .total-geral {
      font-weight: 700;
      font-size: 1rem;
      color: #1565c0 !important;
    }

    .orc-mensagem {
      margin: 8px 0 0;
      font-size: 0.8rem;
      color: #555;
      font-style: italic;
    }

    /* ── Serviços ── */
    .servicos-card {
      margin-bottom: 20px;
      mat-card-content { padding-top: 4px; }
    }

    .servico-item {
      padding: 12px 0;
      border-bottom: 1px solid #f2f2f2;
      &:last-child { border-bottom: none; }
    }

    .servico-header {
      display: flex;
      align-items: center;
      gap: 10px;
      .servico-nome { flex: 1; font-weight: 500; font-size: 0.9rem; }
      .servico-valor { font-weight: 700; color: #1565c0; font-size: 0.9rem; white-space: nowrap; }
    }

    .servico-datas {
      display: flex;
      gap: 16px;
      margin-top: 6px;
    }

    .data-tag {
      display: flex;
      align-items: center;
      gap: 3px;
      font-size: 0.75rem;
      color: #888;
    }

    .icon-xs {
      font-size: 14px !important;
      width: 14px !important;
      height: 14px !important;
    }

    .itens-necessarios {
      margin-top: 10px;
      padding-left: 16px;
      border-left: 3px solid #e8e8e8;
    }

    .itens-titulo {
      margin: 0 0 6px;
      font-size: 0.72rem;
      font-weight: 600;
      color: #999;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .item-linha {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 4px 0;
      font-size: 0.825rem;
      border-bottom: 1px dotted #f0f0f0;
      &:last-child { border-bottom: none; }

      .item-nome { flex: 1; color: #333; }
      .item-tipo { color: #999; font-size: 0.75rem; min-width: 60px; }
      .item-qtd  { color: #666; min-width: 28px; font-weight: 500; }
      .item-valor { font-weight: 600; color: #444; min-width: 80px; text-align: right; }
    }

    /* ── Painéis por role ── */
    .paineis-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(270px, 1fr));
      gap: 16px;
    }

    .painel-card {
      mat-card-subtitle { font-size: 0.75rem; color: #aaa; }
    }

    .painel-acoes {
      display: flex;
      flex-direction: column;
      gap: 10px;
      padding-top: 8px;

      button {
        display: flex;
        align-items: center;
        gap: 6px;
        justify-content: flex-start;
        text-align: left;
      }
    }

    .btn-spinner {
      display: inline-block;
    }

    .orc-resumo-cliente {
      display: flex;
      flex-direction: column;
      gap: 4px;
      padding: 8px 12px;
      background: #f5f5f5;
      border-radius: 6px;
      font-size: 0.875rem;
      color: #444;
      strong { color: #1a1a1a; }
    }

    .sem-info {
      margin: 0;
      font-size: 0.85rem;
      color: #999;
      font-style: italic;
    }

    /* ── Chips ── */
    mat-chip {
      font-size: 0.75rem;
      font-weight: 500;

      /* OS status */
      &.status-os-RECEBIDA             { background: #e3f2fd; color: #1565c0; }
      &.status-os-EM_DIAGNOSTICO       { background: #fff3e0; color: #e65100; }
      &.status-os-AGUARDANDO_APROVACAO { background: #fce4ec; color: #c62828; }
      &.status-os-EM_EXECUCAO          { background: #f3e5f5; color: #6a1b9a; }
      &.status-os-FINALIZADA           { background: #e8f5e9; color: #2e7d32; }
      &.status-os-ENTREGUE             { background: #e0f2f1; color: #00695c; }

      /* Serviço status */
      &.status-srv-AGUARDANDO  { background: #fffde7; color: #f57f17; }
      &.status-srv-EM_EXECUCAO { background: #f3e5f5; color: #6a1b9a; }
      &.status-srv-FINALIZADO  { background: #e8f5e9; color: #2e7d32; }
      &.status-srv-CANCELADO   { background: #ffebee; color: #b71c1c; }

      /* Orçamento status */
      &.status-orc-DISPONIVEL  { background: #e3f2fd; color: #1565c0; }
      &.status-orc-APROVADO    { background: #e8f5e9; color: #2e7d32; }
      &.status-orc-REPROVADO   { background: #ffebee; color: #b71c1c; }
      &.status-orc-SUBSTITUIDO { background: #f5f5f5; color: #757575; }

      /* Item status */
      &.status-item-DISPONIVEL { background: #e8f5e9; color: #2e7d32; }
      &.status-item-PENDENTE   { background: #fff3e0; color: #e65100; }
      &.status-item-UTILIZADO  { background: #e3f2fd; color: #1565c0; }
      &.status-item-CANCELADO  { background: #ffebee; color: #b71c1c; }

      &.chip-sm  { font-size: 0.72rem; }
      &.chip-xs  { font-size: 0.68rem; padding: 0 6px; height: 20px; min-height: 20px; }
    }
  `],
})
export class DetalheOsComponent implements OnInit {
  private readonly service = inject(OrdemServicoService);
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
  readonly podeVerPainelAtendente = ['ADMIN', 'ATENDENTE'].includes(this.role ?? '');
  readonly podeVerPainelMecanico  = ['ADMIN', 'MECANICO'].includes(this.role ?? '');
  readonly podeVerPainelCliente   = ['ADMIN', 'CLIENTE'].includes(this.role ?? '');

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
