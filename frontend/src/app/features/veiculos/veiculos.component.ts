import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { VeiculoService } from './veiculo.service';
import { VeiculoResponse } from './veiculo.model';
import { VeiculoFormDialogComponent } from './veiculo-form-dialog.component';
import {
  ConfirmacaoDialogComponent,
  ConfirmacaoDialogData,
} from '../../shared/dialogs/confirmacao-dialog.component';

@Component({
  selector: 'app-veiculos',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
  ],
  template: `
    <div class="page">

      <!-- Cabeçalho -->
      <div class="page-header">
        <div>
          <h1>Veículos</h1>
          @if (!loading() && !erroCarregamento()) {
            <p class="subtitle">{{ totalElements() }} veículo(s) encontrado(s)</p>
          }
        </div>
        <button mat-raised-button color="primary" (click)="abrirFormulario()">
          <mat-icon>add</mat-icon>
          Novo Veículo
        </button>
      </div>

      <!-- Filtros -->
      <mat-card class="filtros-card">
        <form [formGroup]="filtrosForm" class="filtros-form">
          <mat-form-field appearance="outline" class="filtro-field">
            <mat-label>Placa</mat-label>
            <mat-icon matPrefix>pin</mat-icon>
            <input matInput formControlName="placa" placeholder="ABC1234" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="filtro-field">
            <mat-label>Marca</mat-label>
            <mat-icon matPrefix>business</mat-icon>
            <input matInput formControlName="marca" placeholder="Toyota" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="filtro-field">
            <mat-label>Modelo</mat-label>
            <mat-icon matPrefix>directions_car</mat-icon>
            <input matInput formControlName="modelo" placeholder="Corolla" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="filtro-field filtro-ano">
            <mat-label>Ano</mat-label>
            <mat-icon matPrefix>calendar_today</mat-icon>
            <input matInput formControlName="ano" type="number" placeholder="2022" />
          </mat-form-field>

          <div class="filtros-acoes">
            <button mat-raised-button color="primary" type="button" (click)="buscar()" [disabled]="loading()">
              <mat-icon>search</mat-icon>
              Buscar
            </button>
            <button mat-stroked-button type="button" (click)="limparFiltros()" [disabled]="loading()">
              <mat-icon>clear</mat-icon>
              Limpar
            </button>
          </div>
        </form>
      </mat-card>

      @if (loading()) {
        <div class="loading-center">
          <mat-spinner diameter="48" />
          <p>Carregando veículos…</p>
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
      } @else if (veiculos().length === 0) {
        <mat-card class="estado-card">
          <mat-icon class="icon-vazio">directions_car</mat-icon>
          <p>Nenhum veículo encontrado.</p>
          <button mat-raised-button color="primary" (click)="abrirFormulario()">
            <mat-icon>add</mat-icon>
            Cadastrar veículo
          </button>
        </mat-card>
      } @else {
        <mat-card class="tabela-card">
          <table mat-table [dataSource]="veiculos()">

            <!-- Placa -->
            <ng-container matColumnDef="placa">
              <th mat-header-cell *matHeaderCellDef>Placa</th>
              <td mat-cell *matCellDef="let v" class="mono placa-cell">{{ v.placa }}</td>
            </ng-container>

            <!-- Marca -->
            <ng-container matColumnDef="marca">
              <th mat-header-cell *matHeaderCellDef>Marca</th>
              <td mat-cell *matCellDef="let v" class="col-nome">{{ v.marca }}</td>
            </ng-container>

            <!-- Modelo -->
            <ng-container matColumnDef="modelo">
              <th mat-header-cell *matHeaderCellDef>Modelo</th>
              <td mat-cell *matCellDef="let v">{{ v.modelo }}</td>
            </ng-container>

            <!-- Ano -->
            <ng-container matColumnDef="ano">
              <th mat-header-cell *matHeaderCellDef>Ano</th>
              <td mat-cell *matCellDef="let v" class="mono">{{ v.ano }}</td>
            </ng-container>

            <!-- Cliente (apenas para ADMIN e ATENDENTE) -->
            @if (!isCliente) {
              <ng-container matColumnDef="cliente">
                <th mat-header-cell *matHeaderCellDef>Cliente</th>
                <td mat-cell *matCellDef="let v">
                  <span class="cliente-nome">{{ v.cliente?.nome ?? '—' }}</span>
                  @if (v.cliente?.cpfCnpj) {
                    <span class="cliente-doc mono">{{ v.cliente.cpfCnpj }}</span>
                  }
                </td>
              </ng-container>
            }

            <!-- Ações -->
            <ng-container matColumnDef="acoes">
              <th mat-header-cell *matHeaderCellDef class="col-acoes">Ações</th>
              <td mat-cell *matCellDef="let v" class="col-acoes">
                <button
                  mat-icon-button
                  color="primary"
                  matTooltip="Editar veículo"
                  (click)="abrirFormulario(v)"
                >
                  <mat-icon>edit</mat-icon>
                </button>
                @if (isAdmin) {
                  <button
                    mat-icon-button
                    color="warn"
                    matTooltip="Excluir veículo"
                    (click)="confirmarExclusao(v)"
                  >
                    <mat-icon>delete</mat-icon>
                  </button>
                }
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="colunas"></tr>
            <tr mat-row *matRowDef="let row; columns: colunas" class="veiculo-row"></tr>

          </table>

          <mat-paginator
            [length]="totalElements()"
            [pageSize]="pageSize"
            [pageSizeOptions]="[10, 20, 50]"
            [pageIndex]="pageIndex()"
            (page)="onPage($event)"
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

    /* ── Cabeçalho ─────────────────────────────── */
    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 24px;

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

    /* ── Filtros ────────────────────────────────── */
    .filtros-card {
      padding: 16px 16px 4px;
      margin-bottom: 24px;
    }

    .filtros-form {
      display: flex;
      flex-wrap: wrap;
      gap: 0 16px;
      align-items: flex-start;
    }

    .filtro-field {
      flex: 1 1 180px;
      min-width: 160px;
    }

    .filtro-ano {
      flex: 0 1 120px;
    }

    .filtros-acoes {
      display: flex;
      gap: 8px;
      align-items: center;
      padding-top: 4px;
      padding-bottom: 20px;

      button {
        display: flex;
        align-items: center;
        gap: 4px;
        height: 40px;
      }
    }

    /* ── Loading / Erro / Vazio ─────────────────── */
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

    /* ── Tabela ────────────────────────────────── */
    .tabela-card {
      padding: 0;
      overflow: hidden;
    }

    table {
      width: 100%;
    }

    .mono {
      font-family: 'Roboto Mono', monospace;
      font-size: 0.875rem;
      letter-spacing: 0.03em;
    }

    .placa-cell {
      font-weight: 600;
      color: #1565c0;
      letter-spacing: 0.05em;
    }

    .col-nome {
      font-weight: 500;
    }

    .col-acoes {
      width: 120px;
      white-space: nowrap;
    }

    .cliente-nome {
      display: block;
      font-weight: 500;
    }

    .cliente-doc {
      display: block;
      font-size: 0.8rem;
      color: #666;
    }

    .veiculo-row {
      transition: background 0.15s;

      &:hover {
        background: #f5f5f5;
      }
    }
  `],
})
export class VeiculosComponent implements OnInit {
  private readonly veiculoService = inject(VeiculoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly veiculos = signal<VeiculoResponse[]>([]);
  readonly loading = signal(true);
  readonly erroCarregamento = signal<string | null>(null);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = 20;

  readonly isAdmin = this.auth.getRole() === 'ADMIN';
  readonly isCliente = this.auth.getRole() === 'CLIENTE';

  readonly colunas = this.isCliente
    ? ['placa', 'marca', 'modelo', 'ano', 'acoes']
    : ['placa', 'marca', 'modelo', 'ano', 'cliente', 'acoes'];

  readonly filtrosForm = this.fb.group({
    placa:  [''],
    marca:  [''],
    modelo: [''],
    ano:    [null as number | null],
  });

  ngOnInit(): void {
    this.carregar();
  }

  carregar(page = this.pageIndex()): void {
    this.loading.set(true);
    this.erroCarregamento.set(null);

    const raw = this.filtrosForm.value;
    this.veiculoService
      .listar(
        {
          placa:  raw.placa?.trim() || undefined,
          marca:  raw.marca?.trim() || undefined,
          modelo: raw.modelo?.trim() || undefined,
          ano:    raw.ano ?? undefined,
        },
        page,
        this.pageSize
      )
      .subscribe({
        next: (pagina) => {
          this.veiculos.set(pagina.content);
          this.totalElements.set(pagina.totalElements);
          this.pageIndex.set(pagina.number);
          this.loading.set(false);
        },
        error: () => {
          this.erroCarregamento.set('Não foi possível carregar os veículos.');
          this.loading.set(false);
        },
      });
  }

  onPage(event: PageEvent): void {
    this.carregar(event.pageIndex);
  }

  buscar(): void {
    this.pageIndex.set(0);
    this.carregar(0);
  }

  limparFiltros(): void {
    this.filtrosForm.reset();
    this.pageIndex.set(0);
    this.carregar(0);
  }

  abrirFormulario(veiculo?: VeiculoResponse): void {
    const ref = this.dialog.open(VeiculoFormDialogComponent, {
      width: '560px',
      disableClose: true,
      data: { veiculo: veiculo ?? null },
    });
    ref.afterClosed().subscribe((salvo: boolean) => {
      if (salvo) this.carregar();
    });
  }

  confirmarExclusao(veiculo: VeiculoResponse): void {
    const ref = this.dialog.open<
      ConfirmacaoDialogComponent,
      ConfirmacaoDialogData,
      boolean
    >(ConfirmacaoDialogComponent, {
      width: '400px',
      data: {
        titulo: 'Excluir veículo',
        mensagem: `Tem certeza que deseja excluir "${veiculo.marca} ${veiculo.modelo} — ${veiculo.placa}"? Esta ação não pode ser desfeita.`,
        labelConfirmar: 'Excluir',
      },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) return;
      this.veiculoService.deletar(veiculo.id).subscribe({
        next: () => {
          this.snackBar.open('Veículo excluído com sucesso.', 'Fechar', { duration: 3000 });
          this.carregar();
        },
        error: (err) => {
          const msg = err?.error?.erro ?? 'Erro ao excluir o veículo.';
          this.snackBar.open(msg, 'Fechar', { duration: 4000 });
        },
      });
    });
  }
}
