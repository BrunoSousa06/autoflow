import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { ServicoService } from './servico.service';
import { ServicoResponse } from './servico.model';
import { ServicoFormDialogComponent } from './servico-form-dialog.component';
import {
  ConfirmacaoDialogComponent,
  ConfirmacaoDialogData,
} from '../../shared/dialogs/confirmacao-dialog.component';
import { normalizePage } from '../../core/utils/pagination.util';

@Component({
  selector: 'app-servicos',
  standalone: true,
  imports: [
    CommonModule,
    CurrencyPipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatCardModule,
    MatPaginatorModule,
  ],
  templateUrl: './servicos.component.html',
  styleUrl: './servicos.component.scss',
})
export class ServicosComponent implements OnInit {
  private readonly servicoService = inject(ServicoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly auth = inject(AuthService);

  readonly servicos = signal<ServicoResponse[]>([]);
  readonly loading = signal(true);
  readonly erroCarregamento = signal<string | null>(null);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);

  readonly isAdmin = this.auth.getRole() === 'ADMIN';
  readonly podeGerenciar = ['ADMIN', 'MECANICO'].includes(this.auth.getRole() ?? '');

  readonly colunas = ['nome', 'descricao', 'valor', 'acoes'];

  ngOnInit(): void {
    this.carregar();
  }

  carregar(page = this.pageIndex(), size = this.pageSize()): void {
    this.loading.set(true);
    this.erroCarregamento.set(null);
    this.servicoService.listar(page, size).subscribe({
      next: (pagina) => {
        const p = normalizePage<ServicoResponse>(pagina, this.pageSize());
        this.servicos.set(p.content);
        this.totalElements.set(p.totalElements);
        this.pageIndex.set(p.pageNumber);
        this.pageSize.set(p.pageSize);
        this.loading.set(false);
      },
      error: () => {
        this.erroCarregamento.set('Não foi possível carregar os serviços.');
        this.loading.set(false);
      },
    });
  }

  onPage(event: PageEvent): void {
    this.carregar(event.pageIndex, event.pageSize);
  }

  abrirFormulario(servico?: ServicoResponse): void {
    const ref = this.dialog.open(ServicoFormDialogComponent, {
      width: '560px',
      disableClose: true,
      data: { servico: servico ?? null },
    });
    ref.afterClosed().subscribe((salvo: boolean) => {
      if (salvo) this.carregar();
    });
  }

  confirmarInativacao(servico: ServicoResponse): void {
    const ref = this.dialog.open<
      ConfirmacaoDialogComponent,
      ConfirmacaoDialogData,
      boolean
    >(ConfirmacaoDialogComponent, {
      width: '400px',
      data: {
        titulo: 'Inativar serviço',
        mensagem: `Tem certeza que deseja inativar "${servico.nome}"? O serviço deixará de estar disponível para novas ordens de serviço.`,
        labelConfirmar: 'Inativar',
      },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) return;
      this.servicoService.deletar(servico.id).subscribe({
        next: () => {
          this.snackBar.open('Serviço inativado com sucesso.', 'Fechar', { duration: 3000 });
          this.carregar();
        },
        error: (err) => {
          const msg = err?.error?.erro ?? 'Erro ao inativar o serviço.';
          this.snackBar.open(msg, 'Fechar', { duration: 4000 });
        },
      });
    });
  }
}
