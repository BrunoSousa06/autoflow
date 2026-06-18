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
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
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
