import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-nav',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <nav class="skeleton-nav">
      <span>Autoflow</span>
    </nav>
  `,
  styles: [`.skeleton-nav { padding: 1rem; background: #1976d2; color: white; }`]
})
export class NavComponent {}
