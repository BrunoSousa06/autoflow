import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PecaInsumoResponse } from '../../../features/peca-insumo/peca-insumo.model';
import { PecaInsumoService } from '../../../features/peca-insumo/peca-insumo.service';
import { ItemNecessarioOs, ItensNecessariosRequest } from '../ordem-servico.model';

export interface ItensServicoDialogData {
  numeroOs: string;
  servicoId: number;
  nomeServico: string;
  itensAtuais: ItemNecessarioOs[];
}

interface LinhaItem {
  uid: number;
  pecaInsumoId: number | null;
  quantidade: number;
}

@Component({
  selector: 'app-itens-servico-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './itens-servico-dialog.component.html',
  styleUrl: './itens-servico-dialog.component.scss',
})
export class ItensServicoDialogComponent implements OnInit {
  private readonly pecaInsumoService = inject(PecaInsumoService);
  private readonly dialogRef = inject(MatDialogRef<ItensServicoDialogComponent>);
  readonly data = inject<ItensServicoDialogData>(MAT_DIALOG_DATA);

  readonly pecas = signal<PecaInsumoResponse[]>([]);
  readonly carregando = signal(true);
  readonly erroCarregamento = signal<string | null>(null);

  linhas: LinhaItem[] = [];
  private nextUid = 0;

  ngOnInit(): void {
    this.linhas = this.data.itensAtuais.map(item => ({
      uid: this.nextUid++,
      pecaInsumoId: item.pecaInsumoId,
      quantidade: item.quantidade,
    }));

    this.pecaInsumoService.listar(0, 200).subscribe({
      next: (page) => {
        this.pecas.set(page.content);
        this.carregando.set(false);
      },
      error: (err) => {
        const raw = err?.error?.erro ?? 'Erro ao carregar catálogo de peças e insumos.';
        this.erroCarregamento.set(typeof raw === 'string' ? raw : 'Erro ao carregar catálogo.');
        this.carregando.set(false);
      },
    });
  }

  adicionarLinha(): void {
    this.linhas = [...this.linhas, { uid: this.nextUid++, pecaInsumoId: null, quantidade: 1 }];
  }

  removerLinha(uid: number): void {
    this.linhas = this.linhas.filter(l => l.uid !== uid);
  }

  linhasValidas(): ItensNecessariosRequest[] {
    return this.linhas
      .filter(l => l.pecaInsumoId !== null && l.quantidade >= 1)
      .map(l => ({ pecaInsumoId: l.pecaInsumoId!, quantidade: l.quantidade }));
  }

  confirmar(): void {
    const validas = this.linhasValidas();
    this.dialogRef.close(validas);
  }
}
