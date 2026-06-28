import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { VeiculoService } from './veiculo.service';
import { VeiculoResponse } from './veiculo.model';
import { normalizePage } from '../../core/utils/pagination.util';
import { VeiculoFormDialogComponent } from './veiculo-form-dialog.component';
import {
  ConfirmacaoDialogComponent,
  ConfirmacaoDialogData,
} from '../../shared/dialogs/confirmacao-dialog.component';

@Component({
  selector: 'app-veiculos',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
  ],
  templateUrl: './veiculos.component.html',
  styleUrl: './veiculos.component.scss',
})
export class VeiculosComponent implements OnInit {
  private readonly veiculoService = inject(VeiculoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly veiculos = signal<VeiculoResponse[]>([]);
  readonly loading = signal(true);
  readonly erroCarregamento = signal<string | null>(null);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = 20;

  readonly isAdmin = this.auth.getRole() === 'ADMIN';
  readonly isCliente = this.auth.getRole() === 'CLIENTE';

  readonly colunas = this.isCliente
    ? ['placa', 'marca', 'modelo', 'ano', 'acoes']
    : ['placa', 'marca', 'modelo', 'ano', 'cliente', 'acoes'];

  readonly filtrosForm = this.fb.group({
    placa:  [''],
    marca:  [''],
    modelo: [''],
    ano:    [null as number | null],
  });

  ngOnInit(): void {
    this.carregar();
  }

  carregar(page = this.pageIndex()): void {
    this.loading.set(true);
    this.erroCarregamento.set(null);

    const raw = this.filtrosForm.value;
    this.veiculoService
      .listar(
        {
          placa:  raw.placa?.trim() || undefined,
          marca:  raw.marca?.trim() || undefined,
          modelo: raw.modelo?.trim() || undefined,
          ano:    raw.ano ?? undefined,
        },
        page,
        this.pageSize
      )
      .subscribe({
        next: (pagina) => {
            const p = normalizePage<VeiculoResponse>(pagina, this.pageSize);
            this.veiculos.set(p.content);
            this.totalElements.set(p.totalElements);
            this.pageIndex.set(p.pageNumber);
            this.loading.set(false);
          },
        error: () => {
          this.erroCarregamento.set('Não foi possível carregar os veículos.');
          this.loading.set(false);
        },
      });
  }

  onPage(event: PageEvent): void {
    this.carregar(event.pageIndex);
  }

  buscar(): void {
    this.pageIndex.set(0);
    this.carregar(0);
  }

  limparFiltros(): void {
    this.filtrosForm.reset();
    this.pageIndex.set(0);
    this.carregar(0);
  }

  abrirFormulario(veiculo?: VeiculoResponse): void {
    const ref = this.dialog.open(VeiculoFormDialogComponent, {
      width: '560px',
      disableClose: true,
      data: { veiculo: veiculo ?? null },
    });
    ref.afterClosed().subscribe((salvo: boolean) => {
      if (salvo) this.carregar();
    });
  }

  confirmarExclusao(veiculo: VeiculoResponse): void {
    const ref = this.dialog.open<
      ConfirmacaoDialogComponent,
      ConfirmacaoDialogData,
      boolean
    >(ConfirmacaoDialogComponent, {
      width: '400px',
      data: {
        titulo: 'Excluir veículo',
        mensagem: `Tem certeza que deseja excluir "${veiculo.marca} ${veiculo.modelo} — ${veiculo.placa}"? Esta ação não pode ser desfeita.`,
        labelConfirmar: 'Excluir',
      },
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) return;
      this.veiculoService.deletar(veiculo.id).subscribe({
        next: () => {
          this.snackBar.open('Veículo excluído com sucesso.', 'Fechar', { duration: 3000 });
          this.carregar();
        },
        error: (err) => {
          const msg = err?.error?.erro ?? 'Erro ao excluir o veículo.';
          this.snackBar.open(msg, 'Fechar', { duration: 4000 });
        },
      });
    });
  }
}
