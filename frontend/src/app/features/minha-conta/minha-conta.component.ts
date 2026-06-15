import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { ClienteLogadoResponse } from './minha-conta.model';
import { MinhaContaService } from './minha-conta.service';

@Component({
  selector: 'app-minha-conta',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <section class="page">
      <header class="header">
        <div>
          <h1>Minha conta</h1>
          <p>Dados cadastrais e acompanhamento das suas ordens de servico.</p>
        </div>
        <nav>
          <a mat-button routerLink="/minha-conta" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">
            <mat-icon>person</mat-icon>
            Dados
          </a>
          <a mat-button routerLink="/minha-conta/minhas-ordens" routerLinkActive="active">
            <mat-icon>list_alt</mat-icon>
            Minhas ordens
          </a>
        </nav>
      </header>

      <mat-card class="profile">
        <mat-card-content>
          <div *ngIf="loading()" class="loading">
            <mat-spinner diameter="32"></mat-spinner>
          </div>

          <ng-container *ngIf="!loading() && cliente() as item">
            <div class="identity">
              <mat-icon>account_circle</mat-icon>
              <div>
                <h2>{{ item.nome }}</h2>
                <p>{{ item.email }}</p>
              </div>
            </div>

            <dl>
              <div><dt>CPF/CNPJ</dt><dd>{{ item.cpfCnpj }}</dd></div>
              <div><dt>Telefone</dt><dd>{{ item.telefone || '-' }}</dd></div>
              <div><dt>Veiculos</dt><dd>{{ item.veiculos.length }}</dd></div>
            </dl>

            <div *ngIf="item.veiculos.length" class="vehicles">
              <span *ngFor="let veiculo of item.veiculos">
                {{ veiculo.placa }} - {{ veiculo.marca || '-' }} {{ veiculo.modelo || '' }}
              </span>
            </div>
          </ng-container>
        </mat-card-content>
      </mat-card>

      <router-outlet />
    </section>
  `,
  styles: [`
    .page { padding: 24px; display: grid; gap: 16px; }
    .header { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; }
    h1, h2 { margin: 0; }
    h1 { font-size: 1.6rem; }
    h2 { font-size: 1.15rem; }
    p { margin: 4px 0 0; color: #666; }
    nav { display: flex; gap: 8px; flex-wrap: wrap; }
    a.active { background: #e3f2fd; color: #1565c0; }
    .profile { border-radius: 8px; }
    .loading { display: flex; justify-content: center; padding: 20px; }
    .identity { display: flex; gap: 12px; align-items: center; margin-bottom: 14px; }
    .identity mat-icon { width: 40px; height: 40px; font-size: 40px; color: #607d8b; }
    dl { margin: 0; display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; }
    dt { color: #666; font-size: .8rem; }
    dd { margin: 3px 0 0; font-weight: 600; }
    .vehicles { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 14px; }
    .vehicles span { padding: 6px 10px; border-radius: 999px; background: #eef2f7; }
    @media (max-width: 700px) { .header { flex-direction: column; } .page { padding: 16px; } }
  `],
})
export class MinhaContaComponent implements OnInit {
  private readonly service = inject(MinhaContaService);
  private readonly snackBar = inject(MatSnackBar);

  readonly cliente = signal<ClienteLogadoResponse | null>(null);
  readonly loading = signal(false);

  ngOnInit(): void {
    this.loading.set(true);
    this.service.buscarPerfil()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: cliente => this.cliente.set(cliente),
        error: erro => this.exibirErro(erro, 'Nao foi possivel carregar seus dados.'),
      });
  }

  private exibirErro(erro: any, fallback: string): void {
    const mensagem = erro?.error?.erro ?? erro?.error?.message ?? erro?.error;
    this.snackBar.open(typeof mensagem === 'string' ? mensagem : fallback, 'Fechar', { duration: 5000 });
  }
}
