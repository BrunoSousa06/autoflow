import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { PecaInsumoService } from './peca-insumo.service';
import { CategoriaPecaInsumo, PecaInsumoResponse } from './peca-insumo.model';
import { PecaInsumoFormDialogComponent } from './peca-insumo-form-dialog.component';

@Component({
  selector: 'app-peca-insumo',
  standalone: true,
  imports: [
    CommonModule,
    CurrencyPipe,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTooltipModule,
    MatCardModule,
    MatChipsModule,
    MatDividerModule,
    MatPaginatorModule,
  ],
  templateUrl: './peca-insumo.component.html',
  styleUrl: './peca-insumo.component.scss',
})
export class PecaInsumoComponent implements OnInit {
  private readonly service = inject(PecaInsumoService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly auth = inject(AuthService);

  readonly itens = signal<PecaInsumoResponse[]>([]);
  readonly loading = signal(true);
  readonly loadingDetalhe = signal(false);
  readonly erroCarregamento = signal<string | null>(null);
  readonly detalhe = signal<PecaInsumoResponse | null>(null);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);

  filtroNome = '';
  filtroTipo: CategoriaPecaInsumo | '' = '';

  get temFiltrosAtivos(): boolean {
    return !!(this.filtroNome.trim() || this.filtroTipo);
  }

  readonly role = this.auth.getRole();
  readonly podeGerenciar = ['ADMIN', 'ATENDENTE'].includes(this.role ?? '');
  readonly colunas = ['expandir', 'nome', 'tipo', 'valor', 'quantidade', 'acoes'];

  ngOnInit(): void {
    this.carregar();
  }

  carregar(page = this.pageIndex(), size = this.pageSize()): void {
    this.detalhe.set(null);
    this.loading.set(true);
    this.erroCarregamento.set(null);
    this.service.listar(page, size, this.filtroNome, this.filtroTipo).subscribe({
      next: (pagina) => {
        this.itens.set(pagina.content);
        this.totalElements.set(pagina.page.totalElements);
        this.pageIndex.set(pagina.page.number);
        this.pageSize.set(pagina.page.size);
        this.loading.set(false);
      },
      error: (err) => {
        this.erroCarregamento.set(this.extrairMensagemErro(err, 'Nao foi possivel carregar pecas e insumos.'));
        this.loading.set(false);
      },
    });
  }

  onPage(event: PageEvent): void {
    this.carregar(event.pageIndex, event.pageSize);
  }

  buscar(): void {
    this.carregar(0, this.pageSize());
  }

  limparFiltros(): void {
    this.filtroNome = '';
    this.filtroTipo = '';
    this.carregar(0, this.pageSize());
  }

  abrirFormulario(item?: PecaInsumoResponse): void {
    const ref = this.dialog.open(PecaInsumoFormDialogComponent, {
      width: '580px',
      disableClose: true,
      data: { item: item ?? null },
    });

    ref.afterClosed().subscribe((salvo: boolean) => {
      if (salvo) this.carregar();
    });
  }

  toggleDetalhe(item: PecaInsumoResponse, event: Event): void {
    event.stopPropagation();
    if (this.detalhe()?.id === item.id) {
      this.detalhe.set(null);
      return;
    }

    this.detalhe.set(item);
    this.loadingDetalhe.set(true);
    this.service.buscarPorId(item.id).subscribe({
      next: (data) => {
        this.detalhe.set(data);
        this.loadingDetalhe.set(false);
      },
      error: (err) => {
        this.loadingDetalhe.set(false);
        this.detalhe.set(null);
        this.snackBar.open(this.extrairMensagemErro(err, 'Erro ao carregar detalhe.'), 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  private extrairMensagemErro(err: any, fallback: string): string {
    const body = err?.error;
    if (typeof body === 'string') return body;
    if (body?.erro) return body.erro;
    if (body && typeof body === 'object') {
      const mensagens = Object.values(body).filter((msg): msg is string => typeof msg === 'string');
      if (mensagens.length) return mensagens.join(' ');
    }
    return fallback;
  }
}
