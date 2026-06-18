import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles: string[];
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard',         icon: 'dashboard',       route: '/dashboard',                   roles: ['ADMIN', 'ATENDENTE', 'MECANICO'] },
  { label: 'Clientes',          icon: 'people',          route: '/clientes',                    roles: ['ADMIN', 'ATENDENTE'] },
  { label: 'Usuários',          icon: 'manage_accounts', route: '/usuarios',                    roles: ['ADMIN', 'ATENDENTE'] },
  { label: 'Veículos',          icon: 'directions_car',  route: '/veiculos',                    roles: ['ADMIN', 'ATENDENTE', 'CLIENTE'] },
  { label: 'Ordens de Serviço', icon: 'assignment',      route: '/ordens-servico',              roles: ['ADMIN', 'ATENDENTE', 'MECANICO'] },
  { label: 'Orçamentos',        icon: 'receipt_long',    route: '/orcamentos',                  roles: ['ADMIN', 'ATENDENTE'] },
  { label: 'Reparos Adicionais',icon: 'construction',    route: '/reparos-adicionais',          roles: ['ADMIN', 'ATENDENTE'] },
  { label: 'Serviços',          icon: 'build',           route: '/servicos',                    roles: ['ADMIN', 'ATENDENTE', 'MECANICO'] },
  { label: 'Peças & Insumos',   icon: 'inventory_2',     route: '/peca-insumo',                 roles: ['ADMIN', 'ATENDENTE', 'MECANICO'] },
  { label: 'Minha Conta',       icon: 'account_circle',  route: '/minha-conta',                 roles: ['CLIENTE'] },
  { label: 'Minhas Ordens',     icon: 'list_alt',        route: '/minha-conta/minhas-ordens',   roles: ['CLIENTE'] },
];

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatDividerModule,
  ],
  template: `
    <mat-sidenav-container class="shell-container">

      <!-- ── Sidenav ─────────────────────────────────────────────────── -->
      <mat-sidenav mode="side" opened class="shell-sidenav">

        <div class="sidenav-header">
          <mat-icon class="logo-icon">directions_car</mat-icon>
          <span class="logo-text">AutoFlow</span>
        </div>

        <mat-divider />

        <mat-nav-list>
          @for (item of navItems; track item.route) {
            <a
              mat-list-item
              [routerLink]="item.route"
              routerLinkActive="active-link"
              [matTooltip]="item.label"
              matTooltipPosition="right"
            >
              <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
              <span matListItemTitle>{{ item.label }}</span>
            </a>
          }
        </mat-nav-list>

        <div class="sidenav-footer">
          <mat-divider />
          <div class="user-info">
            <mat-icon>account_circle</mat-icon>
            <div class="user-details">
              <span class="user-email">{{ usuario?.email }}</span>
              <span class="user-role">{{ roleLabel }}</span>
            </div>
          </div>
          <button
            mat-stroked-button
            class="logout-btn"
            (click)="logout()"
          >
            <mat-icon>logout</mat-icon>
            Sair
          </button>
        </div>

      </mat-sidenav>

      <!-- ── Conteúdo principal ──────────────────────────────────────── -->
      <mat-sidenav-content class="shell-content">
        <router-outlet />
      </mat-sidenav-content>

    </mat-sidenav-container>
  `,
  styles: [`
    .shell-container {
      height: 100vh;
    }

    /* ── Sidenav ──────────────────────────────────────── */
    .shell-sidenav {
      width: 240px;
      display: flex;
      flex-direction: column;
      border-right: 1px solid #e0e0e0;
      background: #fff;
    }

    .sidenav-header {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 20px 16px 16px;

      .logo-icon {
        color: #1565c0;
        font-size: 28px;
        width: 28px;
        height: 28px;
      }

      .logo-text {
        font-size: 1.25rem;
        font-weight: 700;
        color: #1a1a1a;
        letter-spacing: -0.3px;
      }
    }

    mat-nav-list {
      flex: 1;
      padding-top: 8px;
    }

    a[mat-list-item] {
      border-radius: 0 24px 24px 0;
      margin-right: 12px;
      margin-bottom: 2px;
      color: #444;

      mat-icon { color: #666; }

      &.active-link {
        background: #e3f2fd;
        color: #1565c0;
        font-weight: 600;

        mat-icon { color: #1565c0; }
      }

      &:hover:not(.active-link) {
        background: #f5f5f5;
      }
    }

    /* ── Footer do sidenav ────────────────────────────── */
    .sidenav-footer {
      padding: 8px 12px 16px;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 4px;
      color: #555;

      mat-icon {
        font-size: 32px;
        width: 32px;
        height: 32px;
        color: #9e9e9e;
      }

      .user-details {
        display: flex;
        flex-direction: column;
        overflow: hidden;
      }

      .user-email {
        font-size: 0.8rem;
        font-weight: 500;
        color: #333;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .user-role {
        font-size: 0.7rem;
        color: #888;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }
    }

    .logout-btn {
      width: 100%;
      margin-top: 4px;
      color: #c62828;
      border-color: #ffcdd2;

      mat-icon { font-size: 18px; }

      &:hover {
        background: #fdecea;
      }
    }

    /* ── Conteúdo ─────────────────────────────────────── */
    .shell-content {
      background: #f5f7fa;
    }
  `]
})
export class ShellComponent {
  private readonly auth = inject(AuthService);

  readonly usuario = this.auth.getUsuarioLogado();

  readonly navItems = NAV_ITEMS.filter(item =>
    this.usuario?.role && item.roles.includes(this.usuario.role)
  );

  readonly roleLabel = this.getRoleLabel(this.usuario?.role);

  logout(): void {
    this.auth.logout();
  }

  private getRoleLabel(role?: string): string {
    const labels: Record<string, string> = {
      ADMIN: 'Administrador',
      ATENDENTE: 'Atendente',
      MECANICO: 'Mecânico',
      CLIENTE: 'Cliente',
    };
    return role ? (labels[role] ?? role) : '';
  }
}
