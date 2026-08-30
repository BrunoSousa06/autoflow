import {Routes} from '@angular/router';
import {authGuard} from './core/guards/auth.guard';
import {roleGuard} from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },

  // Rota pública — sem authGuard e sem shell
  {
    path: 'public/acompanhamento',
    loadComponent: () =>
      import('./features/public/acompanhamento/acompanhamento.component').then(m => m.AcompanhamentoComponent)
  },
  {
    path: 'public/orcamentos/:id',
    loadComponent: () =>
      import('./features/public/orcamento-publico.component').then(m => m.OrcamentoPublicoComponent)
  },

  // Rotas autenticadas — dentro do Shell (sidenav)
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/shell/shell.component').then(m => m.ShellComponent),
    children: [
      {
        path: 'dashboard',
        canActivate: [roleGuard(['ADMIN', 'ATENDENTE', 'MECANICO'])],
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'clientes',
        canActivate: [roleGuard(['ADMIN', 'ATENDENTE'])],
        loadComponent: () =>
          import('./features/clientes/clientes.component').then(m => m.ClientesComponent)
      },
      {
        path: 'usuarios',
        canActivate: [roleGuard(['ADMIN', 'ATENDENTE'])],
        loadComponent: () =>
          import('./features/usuarios/usuarios.component').then(m => m.UsuariosComponent)
      },
      {
        path: 'veiculos',
        canActivate: [roleGuard(['ADMIN', 'ATENDENTE', 'CLIENTE'])],
        loadComponent: () =>
          import('./features/veiculos/veiculos.component').then(m => m.VeiculosComponent)
      },
      {
        path: 'servicos',
        canActivate: [roleGuard(['ADMIN', 'ATENDENTE', 'MECANICO'])],
        loadComponent: () =>
          import('./features/servicos/servicos.component').then(m => m.ServicosComponent)
      },
      {
        path: 'peca-insumo',
        canActivate: [roleGuard(['ADMIN', 'ATENDENTE', 'MECANICO'])],
        loadComponent: () =>
          import('./features/peca-insumo/peca-insumo.component').then(m => m.PecaInsumoComponent)
      },
      {
        path: 'ordens-servico',
        canActivate: [roleGuard(['ADMIN', 'ATENDENTE', 'MECANICO'])],
        children: [
          {
            path: '',
            pathMatch: 'full',
            loadComponent: () =>
              import('./features/ordens-servico/ordens-servico.component').then(m => m.OrdensServicoComponent)
          },
          {
            path: 'nova',
            canActivate: [roleGuard(['ADMIN', 'ATENDENTE'])],
            loadComponent: () =>
              import('./features/ordens-servico/nova-os/criar-os.component').then(m => m.CriarOsComponent)
          },
          {
            path: ':numeroOs',
            loadComponent: () =>
              import('./features/ordens-servico/detalhe-os/detalhe-os.component').then(m => m.DetalheOsComponent)
          },
        ]
      },
      {
        path: 'orcamentos',
        children: [
          {
            path: '',
            pathMatch: 'full',
            canActivate: [roleGuard(['ADMIN', 'ATENDENTE'])],
            loadComponent: () =>
              import('./features/orcamentos/orcamentos.component').then(m => m.OrcamentosComponent)
          },
          {
            path: ':id',
            canActivate: [roleGuard(['ADMIN', 'ATENDENTE', 'CLIENTE'])],
            loadComponent: () =>
              import('./features/orcamentos/orcamento-detalhe.component').then(m => m.OrcamentoDetalheComponent)
          },
        ]
      },
      {
        path: 'reparos-adicionais',
        canActivate: [roleGuard(['ADMIN', 'ATENDENTE'])],
        loadComponent: () =>
          import('./features/reparos-adicionais/reparos-adicionais.component').then(m => m.ReparosAdicionaisComponent)
      },
      {
        path: 'minha-conta',
        canActivate: [roleGuard(['CLIENTE'])],
        loadComponent: () =>
          import('./features/minha-conta/minha-conta.component').then(m => m.MinhaContaComponent),
        children: [
          {
            path: 'minhas-ordens',
            loadComponent: () =>
              import('./features/minha-conta/minhas-ordens/minhas-ordens.component').then(m => m.MinhasOrdensComponent)
          },
          {
            path: 'minhas-ordens/:numeroOs',
            loadComponent: () =>
              import('./features/minha-conta/minhas-ordens/minha-ordem-detalhe.component').then(m => m.MinhaOrdemDetalheComponent)
          }
        ]
      },
    ]
  },

  { path: '**', redirectTo: 'login' }
];
