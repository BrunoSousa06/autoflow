import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-acompanhamento',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatCardModule],
  templateUrl: './acompanhamento.component.html',
  styleUrl: './acompanhamento.component.scss',
})
export class AcompanhamentoComponent {
  constructor(private readonly router: Router) {}

  irParaLogin(): void {
    this.router.navigate(['/login']);
  }
}
