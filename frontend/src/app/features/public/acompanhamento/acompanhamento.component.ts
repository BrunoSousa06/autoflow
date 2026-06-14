import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-acompanhamento',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="skeleton-page">
      <h2>Acompanhamento Público</h2>
      <p>Acompanhamento público de orçamento — em desenvolvimento.</p>
    </div>
  `,
  styles: [`.skeleton-page { padding: 2rem; }`]
})
export class AcompanhamentoComponent {}
