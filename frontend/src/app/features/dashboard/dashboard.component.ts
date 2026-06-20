import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import {
  DashboardService,
  TempoMedioOsResponse,
  TempoMedioServicoResponse,
} from '../../core/services/dashboard.service';

export interface ShortcutCard {
  label: string;
  icon: string;
  route: string;
  description: string;
}

const SHORTCUTS: Record<string, ShortcutCard[]> = {
  ADMIN: [
    { label: 'Clientes',          icon: 'people',         route: '/clientes',           description: 'Gerenciar cadastro de clientes'     },
    { label: 'Veículos',          icon: 'directions_car', route: '/veiculos',           description: 'Consultar veículos cadastrados'     },
    { label: 'Ordens de Serviço', icon: 'assignment',     route: '/ordens-servico',     description: 'Acompanhar todas as ordens'         },
    { label: 'Orçamentos',        icon: 'receipt_long',   route: '/orcamentos',         description: 'Gerenciar orçamentos'               },
    { label: 'Reparos Adicionais',icon: 'construction',   route: '/reparos-adicionais', description: 'Reparos não previstos em contrato'  },
    { label: 'Serviços',          icon: 'build',          route: '/servicos',           description: 'Catálogo de serviços da oficina'    },
    { label: 'Peças & Insumos',   icon: 'inventory_2',    route: '/peca-insumo',        description: 'Controle de peças e materiais'      },
  ],
  ATENDENTE: [
    { label: 'Clientes',          icon: 'people',         route: '/clientes',           description: 'Consultar e cadastrar clientes'     },
    { label: 'Veículos',          icon: 'directions_car', route: '/veiculos',           description: 'Consultar veículos cadastrados'     },
    { label: 'Ordens de Serviço', icon: 'assignment',     route: '/ordens-servico',     description: 'Abrir e acompanhar ordens'          },
    { label: 'Orçamentos',        icon: 'receipt_long',   route: '/orcamentos',         description: 'Enviar e acompanhar orçamentos'     },
    { label: 'Reparos Adicionais',icon: 'construction',   route: '/reparos-adicionais', description: 'Registrar reparos extras'           },
  ],
  MECANICO: [
    { label: 'Ordens de Serviço', icon: 'assignment',     route: '/ordens-servico',     description: 'Ver e executar ordens atribuídas'  },
    { label: 'Serviços',          icon: 'build',          route: '/servicos',           description: 'Consultar catálogo de serviços'    },
  ],
};

const ROLE_LABELS: Record<string, string> = {
  ADMIN: 'Administrador',
  ATENDENTE: 'Atendente',
  MECANICO: 'Mecânico',
  CLIENTE: 'Cliente',
};

const ROLE_CONTEXT: Record<string, string> = {
  ATENDENTE: 'Use os atalhos abaixo para acessar os módulos disponíveis para o seu perfil.',
  MECANICO: 'Acesse as ordens de serviço atribuídas a você pelo atendente ou administrador.',
};

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly dashboardService = inject(DashboardService);

  readonly usuario = this.auth.getUsuarioLogado();
  readonly role = this.usuario?.role ?? '';
  readonly roleLabel = ROLE_LABELS[this.role] ?? this.role;
  readonly greeting = this.getGreeting();
  readonly shortcuts = SHORTCUTS[this.role] ?? [];
  readonly roleContext = ROLE_CONTEXT[this.role] ?? '';

  // Métricas — apenas ADMIN
  metricsLoading = signal(false);
  metricsError = signal(false);
  tempoMedioOs = signal<TempoMedioOsResponse | null>(null);
  topServicos = signal<TempoMedioServicoResponse[]>([]);

  get isAdmin(): boolean {
    return this.role === 'ADMIN';
  }

  ngOnInit(): void {
    if (this.isAdmin) {
      this.carregarMetricas();
    }
  }

  private carregarMetricas(): void {
    this.metricsLoading.set(true);
    this.metricsError.set(false);

    forkJoin({
      os: this.dashboardService.getTempoMedioOs(),
      servicos: this.dashboardService.getTempoMedioPorServico(),
    }).subscribe({
      next: ({ os, servicos }) => {
        this.tempoMedioOs.set(os);
        this.topServicos.set(
          [...servicos]
            .sort((a, b) => b.quantidadeExecucoes - a.quantidadeExecucoes)
            .slice(0, 3)
        );
        this.metricsLoading.set(false);
      },
      error: () => {
        this.metricsError.set(true);
        this.metricsLoading.set(false);
      },
    });
  }

  formatHoras(horas: number): string {
    if (horas < 1) {
      return `${Math.round(horas * 60)} min`;
    }
    return `${horas.toFixed(1)} h`;
  }

  private getGreeting(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Bom dia';
    if (h < 18) return 'Boa tarde';
    return 'Boa noite';
  }
}
