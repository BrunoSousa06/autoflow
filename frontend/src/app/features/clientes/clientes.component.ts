import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { ClienteService } from './cliente.service';
import {
  ClienteResponse,
  formatarCpfCnpj,
  formatarTelefone,
} from './cliente.model';
import { ClienteFormDialogComponent } from './cliente-form-dialog.component';
import {
  ConfirmacaoDialogComponent,
  ConfirmacaoDialogData,
} from '../../shared/dialogs/confirmacao-dialog.component';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatCardModule,
    MatChipsModule,
    MatDividerModule,
  ],
  template: `
    <div class="page">

      <!-- Cabeçalho -->
      <div class="page-header">
        <div>
          <h1>Clientes</h1>
          @if (!loading() && !erroCarregamento()) {
            <p class="subtitle">{{ clientes().length }} cliente(s) cadastrado(s)</p>
          }
        </div>
        <button mat-raised-button color="primary" (click)="abrirFormulario()">
          <mat-icon>person_add</mat-icon>
          Novo Cliente
        </button>
      </div>

      @if (loading()) {
        <div class="loading-center">
          <mat-spinner diameter="48" />
          <p>Carregando clientes…</p>
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
      } @else if (clientes().length === 0) {
        <mat-card class="estado-card">
          <mat-icon class="icon-vazio">people_outline</mat-icon>
          <p>Nenhum cliente cadastrado.</p>
          <button mat-raised-button color="primary" (click)="abrirFormulario()">
            <mat-icon>person_add</mat-icon>
            Cadastrar primeiro cliente
          </button>
        </mat-card>
      } @else {
        <mat-card class="tabela-card">
          <table mat-table [dataSource]="clientes()" multiTemplateDataRows>

            <!-- Botão expandir -->
            <ng-container matColumnDef="expandir">
              <th mat-header-cell *matHeaderCellDef class="col-expandir"></th>
              <td mat-cell *matCellDef="let c" class="col-expandir">
                <button
                  mat-icon-button
                  (click)="toggleExpand(c, $event)"
                  [matTooltip]="expandido === c ? 'Ocultar veículos' : 'Ver veículos'"
                >
                  <mat-icon>{{ expandido === c ? 'expand_less' : 'expand_more' }}</mat-icon>
                </button>
              </td>
            </ng-container>

            <!-- Nome -->
            <ng-container matColumnDef="nome">
              <th mat-header-cell *matHeaderCellDef>Nome</th>
              <td mat-cell *matCellDef="let c" class="col-nome">{{ c.nome }}</td>
            </ng-container>

            <!-- CPF/CNPJ -->
            <ng-container matColumnDef="cpfCnpj">
              <th mat-header-cell *matHeaderCellDef>CPF / CNPJ</th>
              <td mat-cell *matCellDef="let c" class="mono">{{ fmt(c.cpfCnpj) }}</td>
            </ng-container>

            <!-- Telefone -->
            <ng-container matColumnDef="telefone">
              <th mat-header-cell *matHeaderCellDef>Telefone</th>
              <td mat-cell *matCellDef="let c" class="mono">{{ fmtTel(c.telefone) }}</td>
            </ng-container>

            <!-- E-mail -->
            <ng-container matColumnDef="email">
              <th mat-header-cell *matHeaderCellDef>E-mail</th>
              <td mat-cell *matCellDef="let c">{{ c.email }}</td>
            </ng-container>

            <!-- Ações -->
            <ng-container matColumnDef="acoes">
              <th mat-header-cell *matHeaderCellDef class="col-acoes">Ações</th>
              <td mat-cell *matCellDef="let c" class="col-acoes">
                <button
                  mat-icon-button
                  color="primary"
                  matTooltip="Editar cliente"
                  (click)="abrirFormulario(c)"
                >
                  <mat-icon>edit</mat-icon>
                </button>
                @if (isAdmin) {
                  <button
                    mat-icon-button
                    color="warn"
                    matTooltip="Excluir cliente"
                    (click)="confirmarExclusao(c)"
                  >
                    <mat-icon>delete</mat-icon>
                  </button>
                }
              </td>
            </ng-container>

            <!-- Linha de detalhe — veículos -->
            <ng-container matColumnDef="detalhe">
              <td mat-cell *matCellDef="let c" [attr.colspan]="colunas.length" class="detalhe-cell">
                @if (expandido === c) {
                  <div class="detalhe-content">
                    <mat-divider />
                    <div class="veiculos-section">
                      <span class="veiculos-label">
                        <mat-icon>directions_car</mat-icon>
                        Veículos vinculados
                      </span>
                      @if (c.veiculos?.length) {
                        <mat-chip-set>
                          @for (v of c.veiculos; track v.id) {
                            <mat-chip>
                              {{ v.marca }} {{ v.modelo }} &middot; {{ v.ano }} &middot; {{ v.placa }}
                            </mat-chip>
                          }
                        </mat-chip-set>
                      } @else {
                        <span class="sem-veiculo">Nenhum veículo vinculado a este cliente.</span>
                      }
                    </div>
                  </div>
                }
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="colunas"></tr>
            <tr
              mat-row
              *matRowDef="let row; columns: colunas"
              class="cliente-row"
              [class.expandida]="expandido === row"
            ></tr>
            <tr
              mat-row
              *matRowDef="let row; columns: ['detalhe']"
              class="detalhe-row"
            ></tr>

          </table>
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

    /* ── Estados de loading / erro / vazio ─────── */
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

    .col-expandir {
      width: 48px;
      padding-right: 0;
    }

    .col-nome {
      font-weight: 500;
    }

    .mono {
      font-family: 'Roboto Mono', monospace;
      font-size: 0.875rem;
      letter-spacing: 0.03em;
    }

    .col-acoes {
      width: 120px;
      white-space: nowrap;
    }

    .cliente-row {
      transition: background 0.15s;

      &.expandida td {
        background: #f0f7ff;
      }
    }

    .detalhe-row {
      height: 0;

      td { padding: 0 !important; border: none !important; }

      &:hover { background: transparent !important; }
    }

    .detalhe-cell {
      padding: 0 !important;
      border: none !important;
    }

    /* ── Painel de veículos ─────────────────────── */
    .detalhe-content {
      padding: 0 16px 16px 16px;
      background: #f8fbff;
    }

    .veiculos-section {
      display: flex;
      flex-direction: column;
      gap: 12px;
      padding-top: 14px;
    }

    .veiculos-label {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 0.85rem;
      font-weight: 600;
      color: #333;

      mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
        color: #1565c0;
      }
    }

    .sem-veiculo {
      font-size: 0.875rem;
      color: #888;
      font-style: italic;
    }
  `],
})
export class ClientesComponent implements OnInit {
  private readonly clienteService = inject(ClienteService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly auth = inject(AuthService);

  readonly clientes = signal<ClienteResponse[]>([]);
  readonly loading = signal(true);
  readonly erroCarregamento = signal<string | null>(null);
  readonly isAdmin = this.auth.getRole() === 'ADMIN';

  expandido: ClienteResponse | null = null;

  readonly colunas = ['expandir', 'nome', 'cpfCnpj', 'telefone', 'email', 'acoes'];
  readonly fmt = formatarCpfCnpj;
  readonly fmtTel = formatarTelefone;

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.expandido = null;
    this.loading.set(true);
    this.erroCarregamento.set(null);
    this.clienteService.listarTodos().subscribe({
      next: (data) => {
        this.clientes.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.erroCarregamento.set('Não foi possível carregar os clientes.');
        this.loading.set(false);
      },
    });
  }

  toggleExpand(cliente: ClienteResponse, event: Event): void {
    event.stopPropagation();
    this.expandido = this.expandido === cliente ? null : cliente;
  }

  abrirFormulario(cliente?: ClienteResponse): void {
    const ref = this.dialog.open(ClienteFormDialogComponent, {
      width: '540px',
      disableClose: true,
      data: { cliente: cliente ?? null },
    });
    ref.afterClosed().subscribe((salvo: boolean) => {
      if (salvo) this.carregar();
    });
  }

  confirmarExclusao(cliente: ClienteResponse): void {
    const ref = this.dialog.open<
      ConfirmacaoDialogComponent,
      ConfirmacaoDialogData,
      boolean
    >(ConfirmacaoDialogComponent, {
      width: '400px',
      data: {
        titulo: 'Excluir cliente',
        mensagem: `Tem certeza que deseja excluir "${cliente.nome}"? Esta ação não pode ser desfeita.`,
        labelConfirmar: 'Excluir',
      },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) return;
      this.clienteService.deletar(cliente.id).subscribe({
        next: () => {
          this.snackBar.open('Cliente excluído com sucesso.', 'Fechar', { duration: 3000 });
          this.carregar();
        },
        error: (err) => {
          const msg = err?.error?.erro ?? 'Erro ao excluir o cliente.';
          this.snackBar.open(msg, 'Fechar', { duration: 4000 });
        },
      });
    });
  }
}
