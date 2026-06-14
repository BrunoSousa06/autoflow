import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  template: `
    <div class="shell">
      <router-outlet />
    </div>
  `,
  styles: [`.shell { min-height: 100vh; }`]
})
export class ShellComponent {}
