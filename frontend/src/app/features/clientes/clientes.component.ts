import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { ClienteService } from './cliente.service';
import {
  ClienteResponse,
  formatarCpfCnpj,
  formatarTelefone,
} from './cliente.model';
import { ClienteFormDialogComponent } from './cliente-form-dialog.component';
import {
  ConfirmacaoDialogComponent,
  ConfirmacaoDialogData,
} from '../../shared/dialogs/confirmacao-dialog.component';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatCardModule,
    MatChipsModule,
    MatDividerModule,
  ],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.scss',
})
export class ClientesComponent implements OnInit {
  private readonly clienteService = inject(ClienteService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly auth = inject(AuthService);

  readonly clientes = signal<ClienteResponse[]>([]);
  readonly loading = signal(true);
  readonly erroCarregamento = signal<string | null>(null);
  readonly isAdmin = this.auth.getRole() === 'ADMIN';

  expandido: ClienteResponse | null = null;

  readonly colunas = ['expandir', 'nome', 'cpfCnpj', 'telefone', 'email', 'acoes'];
  readonly fmt = formatarCpfCnpj;
  readonly fmtTel = formatarTelefone;

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.expandido = null;
    this.loading.set(true);
    this.erroCarregamento.set(null);
    this.clienteService.listarTodos().subscribe({
      next: (data) => {
        this.clientes.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.erroCarregamento.set('Não foi possível carregar os clientes.');
        this.loading.set(false);
      },
    });
  }

  toggleExpand(cliente: ClienteResponse, event: Event): void {
    event.stopPropagation();
    this.expandido = this.expandido === cliente ? null : cliente;
  }

  abrirFormulario(cliente?: ClienteResponse): void {
    const ref = this.dialog.open(ClienteFormDialogComponent, {
      width: '540px',
      disableClose: true,
      data: { cliente: cliente ?? null },
    });
    ref.afterClosed().subscribe((salvo: boolean) => {
      if (salvo) this.carregar();
    });
  }

  confirmarExclusao(cliente: ClienteResponse): void {
    const ref = this.dialog.open<
      ConfirmacaoDialogComponent,
      ConfirmacaoDialogData,
      boolean
    >(ConfirmacaoDialogComponent, {
      width: '400px',
      data: {
        titulo: 'Excluir cliente',
        mensagem: `Tem certeza que deseja excluir "${cliente.nome}"? Esta ação não pode ser desfeita.`,
        labelConfirmar: 'Excluir',
      },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) return;
      this.clienteService.deletar(cliente.id).subscribe({
        next: () => {
          this.snackBar.open('Cliente excluído com sucesso.', 'Fechar', { duration: 3000 });
          this.carregar();
        },
        error: (err) => {
          const msg = err?.error?.erro ?? 'Erro ao excluir o cliente.';
          this.snackBar.open(msg, 'Fechar', { duration: 4000 });
        },
      });
    });
  }
}
