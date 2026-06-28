import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { ClienteLogadoResponse } from './minha-conta.model';
import { MinhaContaService } from './minha-conta.service';

@Component({
  selector: 'app-minha-conta',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './minha-conta.component.html',
  styleUrl: './minha-conta.component.scss',
})
export class MinhaContaComponent implements OnInit {
  private readonly service = inject(MinhaContaService);
  private readonly snackBar = inject(MatSnackBar);

  readonly cliente = signal<ClienteLogadoResponse | null>(null);
  readonly loading = signal(false);

  ngOnInit(): void {
    this.loading.set(true);
    this.service.buscarPerfil()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: cliente => this.cliente.set(cliente),
        error: erro => this.exibirErro(erro, 'Nao foi possivel carregar seus dados.'),
      });
  }

  private exibirErro(erro: any, fallback: string): void {
    const mensagem = erro?.error?.erro ?? erro?.error?.message ?? erro?.error;
    this.snackBar.open(typeof mensagem === 'string' ? mensagem : fallback, 'Fechar', { duration: 5000 });
  }
}
