import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { UsuarioAdminService } from './usuario.service';
import { UsuarioResponse, ROLE_LABELS, ROLE_COLORS } from './usuario.model';
import { UsuarioFormDialogComponent } from './usuario-form-dialog.component';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatCardModule,
  ],
  template: `
    <div class="page">

      <!-- Cabeçalho -->
      <div class="page-header">
        <div>
          <h1>Usuários</h1>
          @if (!loading() && !erroCarregamento()) {
            <p class="subtitle">{{ usuarios().length }} usuário(s) cadastrado(s)</p>
          }
        </div>
        <button mat-raised-button color="primary" (click)="abrirFormulario()">
          <mat-icon>person_add</mat-icon>
          Novo Usuário
        </button>
      </div>

      @if (loading()) {
        <div class="loading-center">
          <mat-spinner diameter="48" />
          <p>Carregando usuários…</p>
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
      } @else if (usuarios().length === 0) {
        <mat-card class="estado-card">
          <mat-icon class="icon-vazio">group_off</mat-icon>
          <p>Nenhum usuário cadastrado.</p>
          <button mat-raised-button color="primary" (click)="abrirFormulario()">
            <mat-icon>person_add</mat-icon>
            Cadastrar primeiro usuário
          </button>
        </mat-card>
      } @else {
        <mat-card class="tabela-card">
          <table mat-table [dataSource]="usuarios()">

            <!-- Nome -->
            <ng-container matColumnDef="nome">
              <th mat-header-cell *matHeaderCellDef>Nome</th>
              <td mat-cell *matCellDef="let u" class="col-nome">{{ u.nome }}</td>
            </ng-container>

            <!-- E-mail -->
            <ng-container matColumnDef="email">
              <th mat-header-cell *matHeaderCellDef>E-mail</th>
              <td mat-cell *matCellDef="let u">{{ u.email }}</td>
            </ng-container>

            <!-- Função -->
            <ng-container matColumnDef="role">
              <th mat-header-cell *matHeaderCellDef>Função</th>
              <td mat-cell *matCellDef="let u">
                <span
                  class="role-chip"
                  [style.background]="corRole(u.role).bg"
                  [style.color]="corRole(u.role).text"
                >
                  {{ labelRole(u.role) }}
                </span>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="colunas"></tr>
            <tr mat-row *matRowDef="let row; columns: colunas"></tr>

          </table>
        </mat-card>
      }

    </div>
  `,
  styles: [`
    .page {
      padding: 24px;
      max-width: 1000px;
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

      p { margin: 0; color: #555; font-size: 1rem; }

      button { display: flex; align-items: center; gap: 6px; }
    }

    .tabela-card {
      padding: 0;
      overflow: hidden;
    }

    table { width: 100%; }

    .col-nome { font-weight: 500; }

    .role-chip {
      display: inline-block;
      padding: 3px 10px;
      border-radius: 12px;
      font-size: 0.78rem;
      font-weight: 600;
      letter-spacing: 0.3px;
    }
  `],
})
export class UsuariosComponent implements OnInit {
  private readonly service = inject(UsuarioAdminService);
  private readonly dialog = inject(MatDialog);

  readonly usuarios = signal<UsuarioResponse[]>([]);
  readonly loading = signal(true);
  readonly erroCarregamento = signal<string | null>(null);

  readonly colunas = ['nome', 'email', 'role'];
  readonly roleLabels = ROLE_LABELS;
  readonly roleColors = ROLE_COLORS;

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.erroCarregamento.set(null);
    this.service.listar().subscribe({
      next: (data) => {
        this.usuarios.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.erroCarregamento.set('Não foi possível carregar os usuários.');
        this.loading.set(false);
      },
    });
  }

  corRole(role: string): { bg: string; text: string } {
    return ROLE_COLORS[role] ?? { bg: '#f5f5f5', text: '#616161' };
  }

  labelRole(role: string): string {
    return ROLE_LABELS[role] ?? role;
  }

  abrirFormulario(): void {
    const ref = this.dialog.open(UsuarioFormDialogComponent, {
      width: '540px',
      disableClose: true,
      data: {},
    });
    ref.afterClosed().subscribe((salvo: boolean) => {
      if (salvo) this.carregar();
    });
  }
}
