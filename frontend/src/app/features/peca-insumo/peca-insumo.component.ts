import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { PecaInsumoService } from './peca-insumo.service';
import { PecaInsumoResponse } from './peca-insumo.model';
import { PecaInsumoFormDialogComponent } from './peca-insumo-form-dialog.component';

@Component({
  selector: 'app-peca-insumo',
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
    MatChipsModule,
    MatDividerModule,
    MatPaginatorModule,
  ],
  template: `
    <div class="page">
      <div class="page-header">
        <div>
          <h1>Pecas e Insumos</h1>
          @if (!loading() && !erroCarregamento()) {
            <p class="subtitle">{{ totalElements() }} item(ns) em estoque</p>
          }
        </div>
        @if (podeGerenciar) {
          <button mat-raised-button color="primary" (click)="abrirFormulario()">
            <mat-icon>add</mat-icon>
            Novo Item
          </button>
        }
      </div>

      @if (loading()) {
        <div class="loading-center">
          <mat-spinner diameter="48" />
          <p>Carregando pecas e insumos...</p>
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
      } @else if (itens().length === 0) {
        <mat-card class="estado-card">
          <mat-icon class="icon-vazio">inventory_2</mat-icon>
          <p>Nenhuma peca ou insumo cadastrado.</p>
          @if (podeGerenciar) {
            <button mat-raised-button color="primary" (click)="abrirFormulario()">
              <mat-icon>add</mat-icon>
              Cadastrar item
            </button>
          }
        </mat-card>
      } @else {
        <mat-card class="tabela-card">
          <table mat-table [dataSource]="itens()" multiTemplateDataRows class="tabela-peca">
            <ng-container matColumnDef="expandir">
              <th mat-header-cell *matHeaderCellDef class="col-expandir"></th>
              <td mat-cell *matCellDef="let item" class="col-expandir">
                <button
                  mat-icon-button
                  (click)="toggleDetalhe(item, $event)"
                  [matTooltip]="detalhe()?.id === item.id ? 'Ocultar detalhe' : 'Ver detalhe'"
                >
                  <mat-icon>{{ detalhe()?.id === item.id ? 'expand_less' : 'expand_more' }}</mat-icon>
                </button>
              </td>
            </ng-container>

            <ng-container matColumnDef="nome">
              <th mat-header-cell *matHeaderCellDef>Nome</th>
              <td mat-cell *matCellDef="let item" class="col-nome">{{ item.nome }}</td>
            </ng-container>

            <ng-container matColumnDef="tipo">
              <th mat-header-cell *matHeaderCellDef>Tipo</th>
              <td mat-cell *matCellDef="let item">
                <mat-chip>{{ item.tipo }}</mat-chip>
              </td>
            </ng-container>

            <ng-container matColumnDef="valor">
              <th mat-header-cell *matHeaderCellDef>Valor</th>
              <td mat-cell *matCellDef="let item" class="col-valor">
                @if (item.valor != null) {
                  {{ item.valor | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}
                } @else {
                  <span class="sem-valor">-</span>
                }
              </td>
            </ng-container>

            <ng-container matColumnDef="quantidade">
              <th mat-header-cell *matHeaderCellDef>Quantidade</th>
              <td mat-cell *matCellDef="let item" class="col-quantidade">{{ item.quantidade }}</td>
            </ng-container>

            <ng-container matColumnDef="acoes">
              <th mat-header-cell *matHeaderCellDef class="col-acoes">Acoes</th>
              <td mat-cell *matCellDef="let item" class="col-acoes">
                @if (podeGerenciar) {
                  <button
                    mat-icon-button
                    color="primary"
                    matTooltip="Editar item"
                    (click)="abrirFormulario(item)"
                  >
                    <mat-icon>edit</mat-icon>
                  </button>
                }
              </td>
            </ng-container>

            <ng-container matColumnDef="detalhe">
              <td mat-cell *matCellDef="let item" [attr.colspan]="colunas.length" class="detalhe-cell">
                @if (detalhe()?.id === item.id) {
                  <div class="detalhe-content">
                    <mat-divider />
                    @if (loadingDetalhe()) {
                      <div class="detalhe-loading">
                        <mat-spinner diameter="24" />
                        <span>Carregando detalhe...</span>
                      </div>
                    } @else {
                      <div class="detalhe-grid">
                        <span><strong>ID:</strong> {{ detalhe()?.id }}</span>
                        <span><strong>Nome:</strong> {{ detalhe()?.nome }}</span>
                        <span><strong>Tipo:</strong> {{ detalhe()?.tipo }}</span>
                        <span><strong>Quantidade:</strong> {{ detalhe()?.quantidade }}</span>
                        <span>
                          <strong>Valor:</strong>
                          @if (detalhe()?.valor != null) {
                            {{ detalhe()?.valor | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}
                          } @else {
                            -
                          }
                        </span>
                      </div>
                    }
                  </div>
                }
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="colunas"></tr>
            <tr
              mat-row
              *matRowDef="let row; columns: colunas"
              class="item-row"
              [class.expandida]="detalhe()?.id === row.id"
            ></tr>
            <tr mat-row *matRowDef="let row; columns: ['detalhe']" class="detalhe-row"></tr>
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

    .col-expandir {
      width: 48px;
      padding-right: 0;
    }

    .col-nome { font-weight: 500; }

    .col-valor {
      width: 140px;
      font-variant-numeric: tabular-nums;
      font-weight: 500;
      color: #2e7d32;
    }

    .col-quantidade {
      width: 120px;
      font-variant-numeric: tabular-nums;
      font-weight: 500;
    }

    .sem-valor { color: #bbb; }

    .col-acoes {
      width: 80px;
      white-space: nowrap;
    }

    .item-row {
      transition: background 0.15s;

      &.expandida td { background: #f0f7ff; }
      &:hover { background: #f5f5f5; }
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

    .detalhe-content {
      padding: 0 16px 16px 16px;
      background: #f8fbff;
    }

    .detalhe-loading {
      display: flex;
      align-items: center;
      gap: 10px;
      padding-top: 14px;
      color: #666;
      font-size: 0.875rem;
    }

    .detalhe-grid {
      display: grid;
      grid-template-columns: repeat(5, minmax(120px, 1fr));
      gap: 12px;
      padding-top: 14px;
      font-size: 0.875rem;
      color: #444;
    }

    @media (max-width: 900px) {
      .detalhe-grid { grid-template-columns: repeat(2, minmax(120px, 1fr)); }
    }
  `],
})
export class PecaInsumoComponent implements OnInit {
  private readonly service = inject(PecaInsumoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly auth = inject(AuthService);

  readonly itens = signal<PecaInsumoResponse[]>([]);
  readonly loading = signal(true);
  readonly loadingDetalhe = signal(false);
  readonly erroCarregamento = signal<string | null>(null);
  readonly detalhe = signal<PecaInsumoResponse | null>(null);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);

  readonly role = this.auth.getRole();
  readonly podeGerenciar = ['ADMIN', 'ATENDENTE'].includes(this.role ?? '');
  readonly colunas = ['expandir', 'nome', 'tipo', 'valor', 'quantidade', 'acoes'];

  ngOnInit(): void {
    this.carregar();
  }

  carregar(page = this.pageIndex(), size = this.pageSize()): void {
    this.detalhe.set(null);
    this.loading.set(true);
    this.erroCarregamento.set(null);
    this.service.listar(page, size).subscribe({
      next: (pagina) => {
        this.itens.set(pagina.content);
        this.totalElements.set(pagina.page.totalElements);
        this.pageIndex.set(pagina.page.number);
        this.pageSize.set(pagina.page.size);
        this.loading.set(false);
      },
      error: (err) => {
        this.erroCarregamento.set(this.extrairMensagemErro(err, 'Nao foi possivel carregar pecas e insumos.'));
        this.loading.set(false);
      },
    });
  }

  onPage(event: PageEvent): void {
    this.carregar(event.pageIndex, event.pageSize);
  }

  abrirFormulario(item?: PecaInsumoResponse): void {
    const ref = this.dialog.open(PecaInsumoFormDialogComponent, {
      width: '580px',
      disableClose: true,
      data: { item: item ?? null },
    });

    ref.afterClosed().subscribe((salvo: boolean) => {
      if (salvo) this.carregar();
    });
  }

  toggleDetalhe(item: PecaInsumoResponse, event: Event): void {
    event.stopPropagation();
    if (this.detalhe()?.id === item.id) {
      this.detalhe.set(null);
      return;
    }

    this.detalhe.set(item);
    this.loadingDetalhe.set(true);
    this.service.buscarPorId(item.id).subscribe({
      next: (data) => {
        this.detalhe.set(data);
        this.loadingDetalhe.set(false);
      },
      error: (err) => {
        this.loadingDetalhe.set(false);
        this.detalhe.set(null);
        this.snackBar.open(this.extrairMensagemErro(err, 'Erro ao carregar detalhe.'), 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  private extrairMensagemErro(err: any, fallback: string): string {
    const body = err?.error;
    if (typeof body === 'string') return body;
    if (body?.erro) return body.erro;
    if (body && typeof body === 'object') {
      const mensagens = Object.values(body).filter((msg): msg is string => typeof msg === 'string');
      if (mensagens.length) return mensagens.join(' ');
    }
    return fallback;
  }
}
