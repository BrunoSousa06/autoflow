import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { OrcamentoService } from '../orcamentos/orcamento.service';
import { OrcamentoResponse, StatusOrcamento, STATUS_ORCAMENTO_LABEL } from '../orcamentos/orcamento.model';
import { RecusarOrcamentoDialogComponent } from '../orcamentos/recusar-orcamento-dialog.component';

@Component({
  selector: 'app-reparos-adicionais',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './reparos-adicionais.component.html',
  styleUrl: './reparos-adicionais.component.scss',
})
export class ReparosAdicionaisComponent implements OnInit {
  private readonly orcamentoService = inject(OrcamentoService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly auth = inject(AuthService);
  private readonly dialog = inject(MatDialog);

  readonly loading = signal(false);
  readonly itens = signal<OrcamentoResponse[]>([]);
  readonly acaoId = signal<number | null>(null);

  filtroStatus: StatusOrcamento | '' = '';
  filtroNumeroOs = '';

  readonly statusOptions: StatusOrcamento[] = ['DISPONIVEL', 'APROVADO', 'REPROVADO', 'SUBSTITUIDO'];

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading.set(true);
    this.orcamentoService.listar({
      tipo: 'ADICIONAL',
      statusOrcamento: this.filtroStatus || undefined,
      numeroOs: this.filtroNumeroOs.trim() || undefined,
    }).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: lista => this.itens.set(lista),
      error: () => this.snackBar.open('Não foi possível carregar os reparos adicionais.', 'Fechar', { duration: 5000 }),
    });
  }

  limpar(): void {
    this.filtroStatus = '';
    this.filtroNumeroOs = '';
    this.carregar();
  }

  statusLabel(status: StatusOrcamento): string {
    return STATUS_ORCAMENTO_LABEL[status] ?? status;
  }

  podeAprovarRecusar(item: OrcamentoResponse): boolean {
    const role = this.auth.getRole();
    return item.status === 'DISPONIVEL' && (role === 'ADMIN' || role === 'CLIENTE');
  }

  aprovar(item: OrcamentoResponse): void {
    this.acaoId.set(item.id);
    this.orcamentoService.aprovar(item.id)
      .pipe(finalize(() => this.acaoId.set(null)))
      .subscribe({
        next: atualizado => {
          this.atualizarNaLista(atualizado);
          this.snackBar.open('Orçamento aprovado com sucesso.', 'Fechar', { duration: 3000 });
        },
        error: () => this.snackBar.open('Não foi possível aprovar o orçamento.', 'Fechar', { duration: 5000 }),
      });
  }

  recusar(item: OrcamentoResponse): void {
    const ref = this.dialog.open(RecusarOrcamentoDialogComponent, {
      data: { numeroOs: item.numeroOs },
      width: '520px',
    });

    ref.afterClosed().subscribe((motivo: string | null) => {
      if (motivo === null || motivo === undefined) return;

      this.acaoId.set(item.id);
      this.orcamentoService.recusar(item.id, motivo)
        .pipe(finalize(() => this.acaoId.set(null)))
        .subscribe({
          next: atualizado => {
            this.atualizarNaLista(atualizado);
            this.snackBar.open('Orçamento recusado.', 'Fechar', { duration: 3000 });
          },
          error: () => this.snackBar.open('Não foi possível recusar o orçamento.', 'Fechar', { duration: 5000 }),
        });
    });
  }

  verOrcamento(id: number): void {
    this.router.navigate(['/orcamentos', id]);
  }

  verOs(numeroOs: string): void {
    this.router.navigate(['/ordens-servico', numeroOs]);
  }

  private atualizarNaLista(atualizado: OrcamentoResponse): void {
    this.itens.update(lista => lista.map(i => i.id === atualizado.id ? atualizado : i));
  }
}
