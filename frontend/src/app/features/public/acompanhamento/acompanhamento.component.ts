import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-acompanhamento',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatCardModule],
  template: `
    <div class="page">
      <mat-card class="card">
        <mat-icon class="icon">info_outline</mat-icon>
        <h1>Acompanhamento de OS</h1>
        <p>
          O acompanhamento público de ordens de serviço não está disponível.
          Para consultar o status da sua OS ou aprovar orçamentos, faça login com sua conta de cliente.
        </p>
        <button mat-flat-button color="primary" (click)="irParaLogin()">
          <mat-icon>login</mat-icon>
          Fazer login
        </button>
      </mat-card>
    </div>
  `,
  styles: [`
    .page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
      padding: 24px;
    }
    .card {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
      padding: 48px 40px;
      max-width: 480px;
      text-align: center;
    }
    .icon {
      font-size: 56px;
      width: 56px;
      height: 56px;
      color: #1976d2;
    }
    h1 { margin: 0; font-size: 1.5rem; color: #1a1a1a; }
    p { margin: 0; color: #555; line-height: 1.6; }
    button { display: flex; align-items: center; gap: 6px; }
  `],
})
export class AcompanhamentoComponent {
  constructor(private readonly router: Router) {}

  irParaLogin(): void {
    this.router.navigate(['/login']);
  }
}
