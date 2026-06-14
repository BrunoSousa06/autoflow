import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { ServicoService } from './servico.service';
import { ServicoResponse } from './servico.model';
import { ServicoFormDialogComponent } from './servico-form-dialog.component';
import {
  ConfirmacaoDialogComponent,
  ConfirmacaoDialogData,
} from '../../shared/dialogs/confirmacao-dialog.component';

@Component({
  selector: 'app-servicos',
  standalone: true,
  imports: [
    CommonModule,
    CurrencyPipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatCardModule,
    MatPaginatorModule,
  ],
  template: `
    <div class="page">

      <div class="page-header">
        <div>
          <h1>Serviços</h1>
          @if (!loading() && !erroCarregamento()) {
            <p class="subtitle">{{ totalElements() }} serviço(s) cadastrado(s)</p>
          }
        </div>
        @if (podeGerenciar) {
          <button mat-raised-button color="primary" (click)="abrirFormulario()">
            <mat-icon>add</mat-icon>
            Novo Serviço
          </button>
        }
      </div>

      @if (loading()) {
        <div class="loading-center">
          <mat-spinner diameter="48" />
          <p>Carregando serviços…</p>
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
      } @else if (servicos().length === 0) {
        <mat-card class="estado-card">
          <mat-icon class="icon-vazio">build</mat-icon>
          <p>Nenhum serviço cadastrado.</p>
          @if (podeGerenciar) {
            <button mat-raised-button color="primary" (click)="abrirFormulario()">
              <mat-icon>add</mat-icon>
              Cadastrar serviço
            </button>
          }
        </mat-card>
      } @else {
        <mat-card class="tabela-card">
          <table mat-table [dataSource]="servicos()">

            <ng-container matColumnDef="nome">
              <th mat-header-cell *matHeaderCellDef>Nome</th>
              <td mat-cell *matCellDef="let s" class="col-nome">{{ s.nome }}</td>
            </ng-container>

            <ng-container matColumnDef="descricao">
              <th mat-header-cell *matHeaderCellDef>Descrição</th>
              <td mat-cell *matCellDef="let s" class="col-descricao">{{ s.descricao }}</td>
            </ng-container>

            <ng-container matColumnDef="valor">
              <th mat-header-cell *matHeaderCellDef>Valor</th>
              <td mat-cell *matCellDef="let s" class="col-valor">
                @if (s.valor != null) {
                  {{ s.valor | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}
                } @else {
                  <span class="sem-valor">—</span>
                }
              </td>
            </ng-container>

            <ng-container matColumnDef="acoes">
              <th mat-header-cell *matHeaderCellDef class="col-acoes">Ações</th>
              <td mat-cell *matCellDef="let s" class="col-acoes">
                @if (podeGerenciar) {
                  <button
                    mat-icon-button
                    color="primary"
                    matTooltip="Editar serviço"
                    (click)="abrirFormulario(s)"
                  >
                    <mat-icon>edit</mat-icon>
                  </button>
                }
                @if (isAdmin) {
                  <button
                    mat-icon-button
                    color="warn"
                    matTooltip="Excluir serviço"
                    (click)="confirmarExclusao(s)"
                  >
                    <mat-icon>delete</mat-icon>
                  </button>
                }
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="colunas"></tr>
            <tr mat-row *matRowDef="let row; columns: colunas" class="servico-row"></tr>

          </table>

          <mat-paginator
            [length]="totalElements()"
            [pageSize]="pageSize()"
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
      max-width: 1100px;
      margin: 0 auto;
    }

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

    .col-nome {
      font-weight: 500;
      width: 220px;
    }

    .col-descricao {
      color: #555;
      font-size: 0.9rem;
    }

    .col-valor {
      width: 130px;
      font-variant-numeric: tabular-nums;
      font-weight: 500;
      color: #2e7d32;
    }

    .sem-valor { color: #bbb; }

    .col-acoes {
      width: 100px;
      white-space: nowrap;
    }

    .servico-row {
      transition: background 0.15s;

      &:hover { background: #f5f5f5; }
    }
  `],
})
export class ServicosComponent implements OnInit {
  private readonly servicoService = inject(ServicoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly auth = inject(AuthService);

  readonly servicos = signal<ServicoResponse[]>([]);
  readonly loading = signal(true);
  readonly erroCarregamento = signal<string | null>(null);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);

  readonly isAdmin = this.auth.getRole() === 'ADMIN';
  readonly podeGerenciar = ['ADMIN', 'MECANICO'].includes(this.auth.getRole() ?? '');

  readonly colunas = ['nome', 'descricao', 'valor', 'acoes'];

  ngOnInit(): void {
    this.carregar();
  }

  carregar(page = this.pageIndex(), size = this.pageSize()): void {
    this.loading.set(true);
    this.erroCarregamento.set(null);
    this.servicoService.listar(page, size).subscribe({
      next: (pagina) => {
        this.servicos.set(pagina.content);
        this.totalElements.set(pagina.totalElements);
        this.pageIndex.set(pagina.number);
        this.pageSize.set(pagina.size);
        this.loading.set(false);
      },
      error: () => {
        this.erroCarregamento.set('Não foi possível carregar os serviços.');
        this.loading.set(false);
      },
    });
  }

  onPage(event: PageEvent): void {
    this.carregar(event.pageIndex, event.pageSize);
  }

  abrirFormulario(servico?: ServicoResponse): void {
    const ref = this.dialog.open(ServicoFormDialogComponent, {
      width: '560px',
      disableClose: true,
      data: { servico: servico ?? null },
    });
    ref.afterClosed().subscribe((salvo: boolean) => {
      if (salvo) this.carregar();
    });
  }

  confirmarExclusao(servico: ServicoResponse): void {
    const ref = this.dialog.open<
      ConfirmacaoDialogComponent,
      ConfirmacaoDialogData,
      boolean
    >(ConfirmacaoDialogComponent, {
      width: '400px',
      data: {
        titulo: 'Excluir serviço',
        mensagem: `Tem certeza que deseja excluir "${servico.nome}"? Esta ação não pode ser desfeita.`,
        labelConfirmar: 'Excluir',
      },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) return;
      this.servicoService.deletar(servico.id).subscribe({
        next: () => {
          this.snackBar.open('Serviço excluído com sucesso.', 'Fechar', { duration: 3000 });
          this.carregar();
        },
        error: (err) => {
          const msg = err?.error?.erro ?? 'Erro ao excluir o serviço.';
          this.snackBar.open(msg, 'Fechar', { duration: 4000 });
        },
      });
    });
  }
}
