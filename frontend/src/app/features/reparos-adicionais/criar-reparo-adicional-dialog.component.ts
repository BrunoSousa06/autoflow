import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { CurrencyPipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { ServicoService } from '../servicos/servico.service';
import { ServicoResponse } from '../servicos/servico.model';
import { PecaInsumoService } from '../peca-insumo/peca-insumo.service';
import { PecaInsumoResponse } from '../peca-insumo/peca-insumo.model';
import { CriarReparoAdicionalRequest } from './reparo-adicional.model';

export interface CriarReparoAdicionalDialogData {
  numeroOs: string;
  servicosJaNaOs: number[];
}

interface ItemRow {
  pecaInsumoId: number | null;
  quantidade: number;
}

@Component({
  selector: 'app-criar-reparo-adicional-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CurrencyPipe,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDividerModule,
  ],
  templateUrl: './criar-reparo-adicional-dialog.component.html',
  styleUrl: './criar-reparo-adicional-dialog.component.scss',
})
export class CriarReparoAdicionalDialogComponent implements OnInit {
  readonly dialogRef = inject(MatDialogRef<CriarReparoAdicionalDialogComponent>);
  readonly data = inject<CriarReparoAdicionalDialogData>(MAT_DIALOG_DATA);

  private readonly servicoService = inject(ServicoService);
  private readonly pecaInsumoService = inject(PecaInsumoService);

  readonly carregando = signal(true);
  readonly servicos = signal<ServicoResponse[]>([]);
  readonly pecasInsumos = signal<PecaInsumoResponse[]>([]);

  servicoId: number | null = null;
  itens: ItemRow[] = [{ pecaInsumoId: null, quantidade: 1 }];

  get podeSalvar(): boolean {
    return (
      this.servicoId != null &&
      this.itens.length > 0 &&
      this.itens.every(i => i.pecaInsumoId != null && i.quantidade >= 1)
    );
  }

  ngOnInit(): void {
    forkJoin({
      servicos: this.servicoService.listar(0, 100),
      pecas: this.pecaInsumoService.listar(0, 100),
    }).subscribe({
      next: ({ servicos, pecas }) => {
        const jaAdicionados = new Set(this.data.servicosJaNaOs);
        this.servicos.set(servicos.content.filter(s => !jaAdicionados.has(s.id)));
        this.pecasInsumos.set(pecas.content);
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }

  adicionarItem(): void {
    this.itens = [...this.itens, { pecaInsumoId: null, quantidade: 1 }];
  }

  removerItem(index: number): void {
    this.itens = this.itens.filter((_, i) => i !== index);
  }

  confirmar(): void {
    if (!this.servicoId || !this.podeSalvar) return;

    const req: CriarReparoAdicionalRequest = {
      servicos: [{
        servicoId: this.servicoId,
        itensNecessarios: this.itens
          .filter(i => i.pecaInsumoId != null)
          .map(i => ({ pecaInsumoId: i.pecaInsumoId!, quantidade: i.quantidade })),
      }],
    };

    this.dialogRef.close(req);
  }

  cancelar(): void {
    this.dialogRef.close(null);
  }
}
