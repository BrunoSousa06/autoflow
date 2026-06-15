import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import {
  OrcamentoFiltro,
  OrcamentoResponse,
  STATUS_ORCAMENTO_LABEL,
  StatusOrcamento,
  TIPO_ORCAMENTO_LABEL,
  TipoOrcamento,
} from './orcamento.model';
import { OrcamentoService } from './orcamento.service';
import { RecusarOrcamentoDialogComponent } from './recusar-orcamento-dialog.component';

@Component({
  selector: 'app-orcamentos',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  template: `
    <section class="page">
      <header class="header">
        <div>
          <h1>{{ isCliente() ? 'Meus orcamentos' : 'Orcamentos' }}</h1>
          <p>{{ isCliente() ? 'Orcamentos vinculados as suas ordens de servico.' : 'Consulta administrativa de orcamentos.' }}</p>
        </div>
        <button mat-stroked-button color="primary" type="button" (click)="carregar()">
          <mat-icon>refresh</mat-icon>
          Atualizar
        </button>
      </header>

      <form class="filters" (ngSubmit)="carregar()">
        <mat-form-field appearance="outline">
          <mat-label>Status</mat-label>
          <mat-select name="statusOrcamento" [(ngModel)]="filtro.statusOrcamento">
            <mat-option value="">Todos</mat-option>
            <mat-option *ngFor="let status of statusOptions" [value]="status">
              {{ statusLabel(status) }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Tipo</mat-label>
          <mat-select name="tipo" [(ngModel)]="filtro.tipo">
            <mat-option value="">Todos</mat-option>
            <mat-option *ngFor="let tipo of tipoOptions" [value]="tipo">
              {{ tipoLabel(tipo) }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Numero da OS</mat-label>
          <input matInput name="numeroOs" [(ngModel)]="filtro.numeroOs">
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Placa</mat-label>
          <input matInput name="placa" [(ngModel)]="filtro.placa">
        </mat-form-field>

        <ng-container *ngIf="!isCliente()">
          <mat-form-field appearance="outline">
            <mat-label>Email do cliente</mat-label>
            <input matInput name="clienteEmail" [(ngModel)]="filtro.clienteEmail">
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Documento do cliente</mat-label>
            <input matInput name="clienteDocumento" [(ngModel)]="filtro.clienteDocumento">
          </mat-form-field>
        </ng-container>

        <div class="filter-actions">
          <button mat-raised-button color="primary" type="submit">Buscar</button>
          <button mat-button type="button" (click)="limpar()">Limpar</button>
        </div>
      </form>

      <div *ngIf="loading()" class="loading">
        <mat-spinner diameter="36"></mat-spinner>
      </div>

      <div *ngIf="!loading() && !orcamentos().length" class="empty">
        Nenhum orcamento encontrado.
      </div>

      <div *ngIf="!loading() && orcamentos().length" class="list">
        <mat-card *ngFor="let orcamento of orcamentos()" class="card">
          <mat-card-header>
            <mat-card-title>OS {{ orcamento.numeroOs }}</mat-card-title>
            <mat-card-subtitle>{{ tipoLabel(orcamento.tipo) }} v{{ orcamento.versao }}</mat-card-subtitle>
          </mat-card-header>

          <mat-card-content>
            <span class="status" [ngClass]="orcamento.status.toLowerCase()">{{ statusLabel(orcamento.status) }}</span>
            <dl>
              <div><dt>Servicos</dt><dd>{{ orcamento.totalServicos | currency:'BRL' }}</dd></div>
              <div><dt>Pecas/Insumos</dt><dd>{{ orcamento.totalItens | currency:'BRL' }}</dd></div>
              <div><dt>Total</dt><dd>{{ orcamento.totalGeral | currency:'BRL' }}</dd></div>
              <div><dt>Disponivel em</dt><dd>{{ orcamento.disponibilizadoEm ? (orcamento.disponibilizadoEm | date:'short') : '-' }}</dd></div>
            </dl>
          </mat-card-content>

          <mat-card-actions align="end">
            <button mat-button color="primary" type="button" (click)="detalhar(orcamento.id)">Detalhes</button>
            <button
              *ngIf="podeAprovarRecusar(orcamento)"
              mat-button
              color="primary"
              type="button"
              [disabled]="acaoId() === orcamento.id"
              (click)="aprovar(orcamento)">
              Aprovar
            </button>
            <button
              *ngIf="podeAprovarRecusar(orcamento)"
              mat-button
              color="warn"
              type="button"
              [disabled]="acaoId() === orcamento.id"
              (click)="recusar(orcamento)">
              Recusar
            </button>
          </mat-card-actions>
        </mat-card>
      </div>
    </section>
  `,
  styles: [`
    .page { padding: 24px; display: grid; gap: 18px; }
    .header { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; }
    h1 { margin: 0; font-size: 1.6rem; }
    p { margin: 4px 0 0; color: #666; }
    .filters { display: grid; grid-template-columns: repeat(auto-fit, minmax(190px, 1fr)); gap: 12px; align-items: start; }
    .filter-actions { display: flex; gap: 8px; align-items: center; min-height: 56px; }
    .loading, .empty { display: flex; justify-content: center; padding: 32px; color: #666; }
    .list { display: grid; grid-template-columns: repeat(auto-fit, minmax(290px, 1fr)); gap: 14px; }
    .card { border-radius: 8px; }
    .status { display: inline-flex; margin: 8px 0 14px; padding: 4px 10px; border-radius: 999px; font-size: .78rem; font-weight: 700; background: #eef2f7; }
    .aprovado { background: #e6f4ea; color: #137333; }
    .reprovado { background: #fce8e6; color: #a50e0e; }
    .disponivel { background: #e8f0fe; color: #174ea6; }
    .substituido { background: #f1f3f4; color: #5f6368; }
    dl { margin: 0; display: grid; gap: 8px; }
    dl div { display: flex; justify-content: space-between; gap: 12px; }
    dt { color: #666; }
    dd { margin: 0; font-weight: 600; text-align: right; }
    @media (max-width: 700px) { .header { flex-direction: column; } .page { padding: 16px; } }
  `],
})
export class OrcamentosComponent implements OnInit {
  private readonly service = inject(OrcamentoService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly dialog = inject(MatDialog);

  readonly statusOptions: StatusOrcamento[] = ['DISPONIVEL', 'APROVADO', 'REPROVADO', 'SUBSTITUIDO'];
  readonly tipoOptions: TipoOrcamento[] = ['PRINCIPAL', 'ADICIONAL'];
  readonly orcamentos = signal<OrcamentoResponse[]>([]);
  readonly loading = signal(false);
  readonly acaoId = signal<number | null>(null);
  readonly isCliente = computed(() => this.auth.getRole() === 'CLIENTE');

  filtro: OrcamentoFiltro = {};

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    if (this.isCliente()) {
      delete this.filtro.clienteEmail;
      delete this.filtro.clienteDocumento;
    }

    this.loading.set(true);
    this.service.listar(this.filtro)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: orcamentos => this.orcamentos.set(orcamentos),
        error: erro => this.exibirErro(erro, 'Nao foi possivel carregar os orcamentos.'),
      });
  }

  limpar(): void {
    this.filtro = {};
    this.carregar();
  }

  detalhar(id: number): void {
    this.router.navigate(['/orcamentos', id]);
  }

  podeAprovarRecusar(orcamento: OrcamentoResponse): boolean {
    const role = this.auth.getRole();
    return orcamento.status === 'DISPONIVEL' && (role === 'ADMIN' || role === 'CLIENTE');
  }

  aprovar(orcamento: OrcamentoResponse): void {
    this.acaoId.set(orcamento.id);
    this.service.aprovar(orcamento.id)
      .pipe(finalize(() => this.acaoId.set(null)))
      .subscribe({
        next: atualizado => {
          this.atualizarNaLista(atualizado);
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

      this.acaoId.set(orcamento.id);
      this.service.recusar(orcamento.id, motivo)
        .pipe(finalize(() => this.acaoId.set(null)))
        .subscribe({
          next: atualizado => {
            this.atualizarNaLista(atualizado);
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

  private atualizarNaLista(atualizado: OrcamentoResponse): void {
    this.orcamentos.update(lista => lista.map(item => item.id === atualizado.id ? atualizado : item));
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
