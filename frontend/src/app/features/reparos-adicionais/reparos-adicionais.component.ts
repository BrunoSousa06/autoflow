import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { OrcamentoService } from '../orcamentos/orcamento.service';
import { OrcamentoResponse, StatusOrcamento, STATUS_ORCAMENTO_LABEL } from '../orcamentos/orcamento.model';

@Component({
  selector: 'app-reparos-adicionais',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  template: `
    <section class="page">
      <header class="header">
        <div>
          <h1>Reparos Adicionais</h1>
          <p>Orçamentos adicionais gerados durante a execução das ordens de serviço.</p>
        </div>
        <button mat-stroked-button color="primary" type="button" (click)="carregar()">
          <mat-icon>refresh</mat-icon>
          Atualizar
        </button>
      </header>

      <form class="filters" (ngSubmit)="carregar()">
        <mat-form-field appearance="outline">
          <mat-label>Status do orçamento</mat-label>
          <mat-select name="status" [(ngModel)]="filtroStatus">
            <mat-option value="">Todos</mat-option>
            <mat-option *ngFor="let s of statusOptions" [value]="s">
              {{ statusLabel(s) }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Número da OS</mat-label>
          <input matInput name="numeroOs" [(ngModel)]="filtroNumeroOs">
        </mat-form-field>

        <div class="filter-actions">
          <button mat-raised-button color="primary" type="submit">Buscar</button>
          <button mat-button type="button" (click)="limpar()">Limpar</button>
        </div>
      </form>

      <div *ngIf="loading()" class="loading">
        <mat-spinner diameter="36"></mat-spinner>
      </div>

      <div *ngIf="!loading() && !itens().length" class="empty">
        <mat-icon class="icon-vazio">construction</mat-icon>
        <p>Nenhum reparo adicional encontrado.</p>
      </div>

      <div *ngIf="!loading() && itens().length" class="list">
        <mat-card *ngFor="let item of itens()" class="card">
          <mat-card-header>
            <mat-icon mat-card-avatar>construction</mat-icon>
            <mat-card-title>OS {{ item.numeroOs }}</mat-card-title>
            <mat-card-subtitle>Orçamento Adicional — ID #{{ item.id }}</mat-card-subtitle>
          </mat-card-header>

          <mat-card-content>
            <span class="status" [ngClass]="item.status.toLowerCase()">{{ statusLabel(item.status) }}</span>
            <dl>
              <div><dt>Serviços</dt><dd>{{ item.totalServicos | currency:'BRL' }}</dd></div>
              <div><dt>Peças/Insumos</dt><dd>{{ item.totalItens | currency:'BRL' }}</dd></div>
              <div><dt>Total</dt><dd>{{ item.totalGeral | currency:'BRL' }}</dd></div>
              <div><dt>Criado em</dt><dd>{{ item.criadoEm | date:'short' }}</dd></div>
            </dl>
          </mat-card-content>

          <mat-card-actions align="end">
            <button mat-button color="primary" type="button" (click)="verOrcamento(item.id)">
              <mat-icon>receipt_long</mat-icon>
              Ver orçamento
            </button>
            <button mat-button color="primary" type="button" (click)="verOs(item.numeroOs)">
              <mat-icon>assignment</mat-icon>
              Ver OS
            </button>
          </mat-card-actions>
        </mat-card>
      </div>
    </section>
  `,
  styles: [`
    .page { padding: 24px; display: grid; gap: 16px; }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 16px;
    }
    h1 { margin: 0; font-size: 1.6rem; }
    p { margin: 4px 0 0; color: #666; }

    .filters {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      align-items: flex-end;
    }
    .filters mat-form-field { min-width: 180px; }
    .filter-actions { display: flex; gap: 8px; padding-bottom: 4px; }

    .loading { display: flex; justify-content: center; padding: 32px; }

    .empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 48px 16px;
      color: #999;
      gap: 12px;
    }
    .icon-vazio { font-size: 48px; width: 48px; height: 48px; color: #ccc; }

    .list { display: grid; gap: 12px; }

    .card { border-radius: 8px; }

    dl {
      margin: 12px 0 0;
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
      gap: 8px;
    }
    dt { color: #666; font-size: 0.8rem; }
    dd { margin: 2px 0 0; font-weight: 600; }

    .status {
      display: inline-block;
      padding: 4px 10px;
      border-radius: 999px;
      font-weight: 700;
      font-size: 0.8rem;
      background: #eef2f7;
      margin-top: 4px;
    }
    .disponivel { background: #e8f0fe; color: #174ea6; }
    .aprovado   { background: #e6f4ea; color: #137333; }
    .reprovado  { background: #fce8e6; color: #a50e0e; }
    .substituido{ background: #f1f3f4; color: #5f6368; }

    @media (max-width: 600px) {
      .header, .filter-actions { flex-direction: column; align-items: stretch; }
      .page { padding: 16px; }
    }
  `],
})
export class ReparosAdicionaisComponent implements OnInit {
  private readonly orcamentoService = inject(OrcamentoService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly loading = signal(false);
  readonly itens = signal<OrcamentoResponse[]>([]);

  filtroStatus: StatusOrcamento | '' = '';
  filtroNumeroOs = '';

  readonly statusOptions: StatusOrcamento[] = ['DISPONIVEL', 'APROVADO', 'REPROVADO', 'SUBSTITUIDO'];

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.orcamentoService.listar({
      tipo: 'ADICIONAL',
      statusOrcamento: this.filtroStatus || undefined,
      numeroOs: this.filtroNumeroOs.trim() || undefined,
    }).subscribe({
      next: lista => {
        this.itens.set(lista);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Não foi possível carregar os reparos adicionais.', 'Fechar', { duration: 5000 });
        this.loading.set(false);
      },
    });
  }

  limpar(): void {
    this.filtroStatus = '';
    this.filtroNumeroOs = '';
    this.carregar();
  }

  statusLabel(status: StatusOrcamento): string {
    return STATUS_ORCAMENTO_LABEL[status] ?? status;
  }

  verOrcamento(id: number): void {
    this.router.navigate(['/orcamentos', id]);
  }

  verOs(numeroOs: string): void {
    this.router.navigate(['/ordens-servico', numeroOs]);
  }
}
