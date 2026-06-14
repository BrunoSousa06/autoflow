import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-orcamentos',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="skeleton-page">
      <h2>Orçamentos</h2>
      <p>Gestão de orçamentos — em desenvolvimento.</p>
    </div>
  `,
  styles: [`.skeleton-page { padding: 2rem; }`]
})
export class OrcamentosComponent {}
