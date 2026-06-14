import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'clientes',
    loadComponent: () =>
      import('./features/clientes/clientes.component').then(m => m.ClientesComponent)
  },
  {
    path: 'veiculos',
    loadComponent: () =>
      import('./features/veiculos/veiculos.component').then(m => m.VeiculosComponent)
  },
  {
    path: 'servicos',
    loadComponent: () =>
      import('./features/servicos/servicos.component').then(m => m.ServicosComponent)
  },
  {
    path: 'peca-insumo',
    loadComponent: () =>
      import('./features/peca-insumo/peca-insumo.component').then(m => m.PecaInsumoComponent)
  },
  {
    path: 'ordens-servico',
    loadComponent: () =>
      import('./features/ordens-servico/ordens-servico.component').then(m => m.OrdensServicoComponent)
  },
  {
    path: 'orcamentos',
    loadComponent: () =>
      import('./features/orcamentos/orcamentos.component').then(m => m.OrcamentosComponent)
  },
  {
    path: 'reparos-adicionais',
    loadComponent: () =>
      import('./features/reparos-adicionais/reparos-adicionais.component').then(m => m.ReparosAdicionaisComponent)
  },
  {
    path: 'minha-conta',
    loadComponent: () =>
      import('./features/minha-conta/minha-conta.component').then(m => m.MinhaContaComponent),
    children: [
      {
        path: 'minhas-ordens',
        loadComponent: () =>
          import('./features/minha-conta/minhas-ordens/minhas-ordens.component').then(m => m.MinhasOrdensComponent)
      }
    ]
  },
  {
    path: 'public/acompanhamento',
    loadComponent: () =>
      import('./features/public/acompanhamento/acompanhamento.component').then(m => m.AcompanhamentoComponent)
  },

  { path: '**', redirectTo: 'login' }
];
