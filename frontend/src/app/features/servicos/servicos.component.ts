import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-servicos',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="skeleton-page">
      <h2>Serviços</h2>
      <p>Gestão de serviços — em desenvolvimento.</p>
    </div>
  `,
  styles: [`.skeleton-page { padding: 2rem; }`]
})
export class ServicosComponent {}
