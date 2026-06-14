import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="skeleton-page">
      <h2>Dashboard</h2>
      <p>Painel principal — em desenvolvimento.</p>
    </div>
  `,
  styles: [`.skeleton-page { padding: 2rem; }`]
})
export class DashboardComponent {}
