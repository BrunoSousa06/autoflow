import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { UsuarioAdminService } from './usuario.service';
import { UsuarioResponse, ROLE_LABELS, ROLE_COLORS } from './usuario.model';
import { UsuarioFormDialogComponent } from './usuario-form-dialog.component';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatCardModule,
  ],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.scss',
})
export class UsuariosComponent implements OnInit {
  private readonly service = inject(UsuarioAdminService);
  private readonly dialog = inject(MatDialog);

  readonly usuarios = signal<UsuarioResponse[]>([]);
  readonly loading = signal(true);
  readonly erroCarregamento = signal<string | null>(null);

  readonly colunas = ['nome', 'email', 'role'];
  readonly roleLabels = ROLE_LABELS;
  readonly roleColors = ROLE_COLORS;

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.erroCarregamento.set(null);
    this.service.listar().subscribe({
      next: (data) => {
        this.usuarios.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.erroCarregamento.set('Não foi possível carregar os usuários.');
        this.loading.set(false);
      },
    });
  }

  corRole(role: string): { bg: string; text: string } {
    return ROLE_COLORS[role] ?? { bg: '#f5f5f5', text: '#616161' };
  }

  labelRole(role: string): string {
    return ROLE_LABELS[role] ?? role;
  }

  abrirFormulario(): void {
    const ref = this.dialog.open(UsuarioFormDialogComponent, {
      width: '540px',
      disableClose: true,
      data: {},
    });
    ref.afterClosed().subscribe((salvo: boolean) => {
      if (salvo) this.carregar();
    });
  }
}
