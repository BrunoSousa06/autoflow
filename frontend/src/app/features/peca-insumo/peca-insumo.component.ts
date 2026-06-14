import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-peca-insumo',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="skeleton-page">
      <h2>Peças e Insumos</h2>
      <p>Gestão de peças e insumos — em desenvolvimento.</p>
    </div>
  `,
  styles: [`.skeleton-page { padding: 2rem; }`]
})
export class PecaInsumoComponent {}
