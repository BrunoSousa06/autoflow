import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import {
  OrcamentoFiltro,
  OrcamentoResponse,
  STATUS_ORCAMENTO_LABEL,
  StatusOrcamento,
  TIPO_ORCAMENTO_LABEL,
  TipoOrcamento,
} from './orcamento.model';
import { OrcamentoService } from './orcamento.service';
import { RecusarOrcamentoDialogComponent } from './recusar-orcamento-dialog.component';

@Component({
  selector: 'app-orcamentos',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  templateUrl: './orcamentos.component.html',
  styleUrl: './orcamentos.component.scss',
})
export class OrcamentosComponent implements OnInit {
  private readonly service = inject(OrcamentoService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly dialog = inject(MatDialog);

  readonly statusOptions: StatusOrcamento[] = ['DISPONIVEL', 'APROVADO', 'REPROVADO', 'SUBSTITUIDO'];
  readonly tipoOptions: TipoOrcamento[] = ['PRINCIPAL', 'ADICIONAL'];
  readonly orcamentos = signal<OrcamentoResponse[]>([]);
  readonly loading = signal(false);
  readonly acaoId = signal<number | null>(null);
  readonly isCliente = computed(() => this.auth.getRole() === 'CLIENTE');

  filtro: OrcamentoFiltro = {};

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    if (this.isCliente()) {
      delete this.filtro.clienteEmail;
      delete this.filtro.clienteDocumento;
    }

    this.loading.set(true);
    this.service.listar(this.filtro)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: orcamentos => this.orcamentos.set(orcamentos),
        error: erro => this.exibirErro(erro, 'Não foi possível carregar os orçamentos.'),
      });
  }

  limpar(): void {
    this.filtro = {};
    this.carregar();
  }

  detalhar(id: number): void {
    this.router.navigate(['/orcamentos', id]);
  }

  podeAprovarRecusar(orcamento: OrcamentoResponse): boolean {
    const role = this.auth.getRole();
    return orcamento.status === 'DISPONIVEL' && (role === 'ADMIN' || role === 'CLIENTE');
  }

  aprovar(orcamento: OrcamentoResponse): void {
    this.acaoId.set(orcamento.id);
    this.service.aprovar(orcamento.id)
      .pipe(finalize(() => this.acaoId.set(null)))
      .subscribe({
        next: atualizado => {
          this.atualizarNaLista(atualizado);
          this.snackBar.open('Orçamento aprovado com sucesso.', 'Fechar', { duration: 3000 });
        },
        error: erro => this.exibirErro(erro, 'Não foi possível aprovar o orçamento.'),
      });
  }

  recusar(orcamento: OrcamentoResponse): void {
    const ref = this.dialog.open(RecusarOrcamentoDialogComponent, {
      data: { numeroOs: orcamento.numeroOs },
      width: '520px',
    });

    ref.afterClosed().subscribe((motivo: string | null) => {
      if (motivo === null || motivo === undefined) return;

      this.acaoId.set(orcamento.id);
      this.service.recusar(orcamento.id, motivo)
        .pipe(finalize(() => this.acaoId.set(null)))
        .subscribe({
          next: atualizado => {
            this.atualizarNaLista(atualizado);
            this.snackBar.open('Orçamento recusado com sucesso.', 'Fechar', { duration: 3000 });
          },
          error: erro => this.exibirErro(erro, 'Não foi possível recusar o orçamento.'),
        });
    });
  }

  statusLabel(status: StatusOrcamento): string {
    return STATUS_ORCAMENTO_LABEL[status];
  }

  tipoLabel(tipo: TipoOrcamento): string {
    return TIPO_ORCAMENTO_LABEL[tipo];
  }

  private atualizarNaLista(atualizado: OrcamentoResponse): void {
    this.orcamentos.update(lista => lista.map(item => item.id === atualizado.id ? atualizado : item));
  }

  private exibirErro(erro: unknown, fallback: string): void {
    const mensagem = this.extrairMensagemErro(erro) ?? fallback;
    this.snackBar.open(mensagem, 'Fechar', { duration: 5000 });
  }

  private extrairMensagemErro(erro: any): string | null {
    const payload = erro?.error?.erro ?? erro?.error?.message ?? erro?.error;
    return typeof payload === 'string' ? payload : null;
  }
}
