import { Component, inject, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { OrdemServicoService } from './ordem-servico.service';
import { OrdemServicoFiltro, OrdemServicoResponse, STATUS_OS_LABEL, StatusOrdemServico } from './ordem-servico.model';

@Component({
  selector: 'app-ordens-servico',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatPaginatorModule,
  ],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h1>Ordens de Serviço</h1>
          @if (!loading() && !erroCarregamento()) {
            <p class="subtitle">{{ totalElements() }} ordem(ns) encontrada(s)</p>
          }
        </div>
        @if (podeCriar) {
          <button mat-raised-button color="primary" (click)="novaOs()">
            <mat-icon>add</mat-icon>
            Criar OS
          </button>
        }
      </div>

      <mat-card class="filtros-card">
        <form [formGroup]="filtroForm" class="filtros-form">
          <mat-form-field appearance="outline" class="filtro-campo">
            <mat-label>Cliente (nome ou CPF/CNPJ)</mat-label>
            <input matInput formControlName="cliente" placeholder="Buscar cliente..." />
            <mat-icon matSuffix>person_search</mat-icon>
          </mat-form-field>

          <mat-form-field appearance="outline" class="filtro-campo">
            <mat-label>Nº OS</mat-label>
            <input matInput formControlName="numeroOs" placeholder="Ex: OS-17..." />
            <mat-icon matSuffix>search</mat-icon>
          </mat-form-field>

          <mat-form-field appearance="outline" class="filtro-status">
            <mat-label>Status</mat-label>
            <mat-select formControlName="status">
              <mat-option value="">Todos</mat-option>
              @for (s of statusOptions; track s) {
                <mat-option [value]="s">{{ labelStatus(s) }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          <button mat-stroked-button type="button" (click)="limparFiltros()" class="btn-limpar">
            <mat-icon>clear</mat-icon>
            Limpar
          </button>
        </form>
      </mat-card>

      @if (loading()) {
        <div class="loading-center">
          <mat-spinner diameter="48" />
          <p>Carregando ordens de serviço...</p>
        </div>
      } @else if (erroCarregamento()) {
        <mat-card class="estado-card">
          <mat-icon color="warn">error_outline</mat-icon>
          <p>{{ erroCarregamento() }}</p>
          <button mat-stroked-button (click)="carregar()">
            <mat-icon>refresh</mat-icon>
            Tentar novamente
          </button>
        </mat-card>
      } @else if (ordens().length === 0) {
        <mat-card class="estado-card">
          <mat-icon class="icon-vazio">assignment</mat-icon>
          <p>Nenhuma ordem de serviço encontrada.</p>
          @if (podeCriar && !temFiltrosAtivos()) {
            <button mat-raised-button color="primary" (click)="novaOs()">
              <mat-icon>add</mat-icon>
              Criar OS
            </button>
          }
        </mat-card>
      } @else {
        <mat-card class="tabela-card">
          <table mat-table [dataSource]="ordens()">

            <ng-container matColumnDef="numeroOs">
              <th mat-header-cell *matHeaderCellDef>Nº OS</th>
              <td mat-cell *matCellDef="let os" class="col-numero">{{ os.numeroOs }}</td>
            </ng-container>

            <ng-container matColumnDef="cliente">
              <th mat-header-cell *matHeaderCellDef>Cliente</th>
              <td mat-cell *matCellDef="let os" class="col-cliente">
                <span class="cliente-nome">{{ os.clienteNome }}</span>
                <span class="cliente-doc">{{ os.clienteCpfCnpj }}</span>
              </td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let os">
                <mat-chip [class]="'status-' + os.status">
                  {{ labelStatus(os.status) }}
                </mat-chip>
              </td>
            </ng-container>

            <ng-container matColumnDef="dataAbertura">
              <th mat-header-cell *matHeaderCellDef>Abertura</th>
              <td mat-cell *matCellDef="let os" class="col-data">
                {{ os.dataAbertura | date:'dd/MM/yyyy HH:mm' }}
              </td>
            </ng-container>

            <ng-container matColumnDef="servicos">
              <th mat-header-cell *matHeaderCellDef>Serviços</th>
              <td mat-cell *matCellDef="let os" class="col-servicos">
                {{ os.servicos.length }} serviço(s)
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="colunas"></tr>
            <tr mat-row *matRowDef="let row; columns: colunas" class="os-row" (click)="verDetalhe(row)"></tr>
          </table>

          <mat-paginator
            [length]="totalElements()"
            [pageSize]="pageSize()"
            [pageIndex]="pageIndex()"
            [pageSizeOptions]="[5, 10, 20, 50]"
            (page)="onPageChange($event)"
            showFirstLastButtons
          />
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .page {
      padding: 24px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;

      h1 {
        margin: 0 0 4px;
        font-size: 1.5rem;
        font-weight: 600;
        color: #1a1a1a;
      }

      .subtitle {
        margin: 0;
        color: #666;
        font-size: 0.875rem;
      }

      button {
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }

    .filtros-card {
      margin-bottom: 16px;
      padding: 16px 16px 8px;
    }

    .filtros-form {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      flex-wrap: wrap;
    }

    .filtro-campo {
      flex: 1;
      min-width: 200px;
    }

    .filtro-status {
      min-width: 190px;
      max-width: 220px;
    }

    .btn-limpar {
      margin-top: 4px;
      height: 56px;
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .loading-center {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 64px 24px;
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

      mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
      }

      .icon-vazio { color: #ccc; }

      p {
        margin: 0;
        color: #555;
        font-size: 1rem;
      }

      button {
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }

    .tabela-card {
      padding: 0;
      overflow: hidden;
    }

    table { width: 100%; }

    .col-numero {
      font-weight: 600;
      font-family: monospace;
      font-size: 0.9rem;
      color: #1565c0;
      width: 160px;
    }

    .col-cliente {
      display: flex;
      flex-direction: column;
      padding-top: 10px;
      padding-bottom: 10px;

      .cliente-nome {
        font-weight: 500;
        font-size: 0.875rem;
      }

      .cliente-doc {
        font-size: 0.75rem;
        color: #888;
        margin-top: 2px;
      }
    }

    .col-data {
      width: 150px;
      color: #555;
      font-size: 0.875rem;
    }

    .col-servicos {
      width: 120px;
      color: #555;
      font-size: 0.875rem;
    }

    .os-row {
      transition: background 0.15s;
      cursor: pointer;
      &:hover { background: #f5f5f5; }
    }

    mat-chip {
      font-size: 0.75rem;
      font-weight: 500;

      &.status-RECEBIDA             { background: #e3f2fd; color: #1565c0; }
      &.status-EM_DIAGNOSTICO       { background: #fff3e0; color: #e65100; }
      &.status-AGUARDANDO_APROVACAO { background: #fce4ec; color: #c62828; }
      &.status-EM_EXECUCAO          { background: #f3e5f5; color: #6a1b9a; }
      &.status-FINALIZADA           { background: #e8f5e9; color: #2e7d32; }
      &.status-ENTREGUE             { background: #e8f5e9; color: #1b5e20; }
    }
  `],
})
export class OrdensServicoComponent implements OnInit, OnDestroy {
  private readonly service = inject(OrdemServicoService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly destroy$ = new Subject<void>();

  readonly ordens = signal<OrdemServicoResponse[]>([]);
  readonly loading = signal(true);
  readonly erroCarregamento = signal<string | null>(null);
  readonly totalElements = signal(0);
  readonly pageSize = signal(10);
  readonly pageIndex = signal(0);

  readonly podeCriar = ['ADMIN', 'ATENDENTE'].includes(this.auth.getRole() ?? '');
  readonly colunas = ['numeroOs', 'cliente', 'status', 'dataAbertura', 'servicos'];
  readonly statusOptions: StatusOrdemServico[] = [
    'RECEBIDA', 'EM_DIAGNOSTICO', 'AGUARDANDO_APROVACAO', 'EM_EXECUCAO', 'FINALIZADA', 'ENTREGUE',
  ];

  readonly filtroForm = new FormGroup({
    cliente: new FormControl(''),
    numeroOs: new FormControl(''),
    status: new FormControl<StatusOrdemServico | ''>(''),
  });

  ngOnInit(): void {
    this.filtroForm.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
      takeUntil(this.destroy$),
    ).subscribe(() => {
      this.pageIndex.set(0);
      this.carregar();
    });
    this.carregar();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  carregar(): void {
    this.loading.set(true);
    this.erroCarregamento.set(null);

    const val = this.filtroForm.value;
    const filtro: OrdemServicoFiltro = {
      cliente: val.cliente || undefined,
      numeroOs: val.numeroOs || undefined,
      status: (val.status as StatusOrdemServico) || undefined,
      page: this.pageIndex(),
      size: this.pageSize(),
    };

    this.service.listar(filtro).subscribe({
      next: (page) => {
        this.ordens.set(page.content);
        this.totalElements.set(page.page.totalElements);
        this.loading.set(false);
      },
      error: (err) => {
        const msg = err?.error?.erro ?? err?.error ?? 'Não foi possível carregar as ordens de serviço.';
        this.erroCarregamento.set(typeof msg === 'string' ? msg : 'Erro ao carregar ordens de serviço.');
        this.loading.set(false);
      },
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.carregar();
  }

  limparFiltros(): void {
    this.filtroForm.reset({ cliente: '', numeroOs: '', status: '' });
  }

  temFiltrosAtivos(): boolean {
    const val = this.filtroForm.value;
    return !!(val.cliente || val.numeroOs || val.status);
  }

  novaOs(): void {
    this.router.navigate(['/ordens-servico/nova']);
  }

  verDetalhe(os: OrdemServicoResponse): void {
    this.router.navigate(['/ordens-servico', os.numeroOs]);
  }

  labelStatus(status: StatusOrdemServico): string {
    return STATUS_OS_LABEL[status] ?? status;
  }
}
