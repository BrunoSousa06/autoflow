import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-minha-conta',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  template: `
    <div class="skeleton-page">
      <h2>Minha Conta</h2>
      <p>Dados da conta do usuário — em desenvolvimento.</p>
      <router-outlet />
    </div>
  `,
  styles: [`.skeleton-page { padding: 2rem; }`]
})
export class MinhaContaComponent {}
