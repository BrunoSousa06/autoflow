import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { CurrencyPipe } from '@angular/common';
import { ServicoService } from '../../servicos/servico.service';
import { ServicoResponse } from '../../servicos/servico.model';
import { ServicoSolicitadoRequest } from '../ordem-servico.model';

export interface AdicionarServicoDiagnosticoDialogData {
  numeroOs: string;
  servicosJaAdicionados: number[];
}

@Component({
  selector: 'app-adicionar-servico-dialog',
  standalone: true,
  imports: [
    CommonModule,
    CurrencyPipe,
    MatDialogModule,
    MatButtonModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatDividerModule,
  ],
  templateUrl: './adicionar-servico-dialog.component.html',
  styleUrl: './adicionar-servico-dialog.component.scss',
})
export class AdicionarServicoDiagnosticoDialogComponent implements OnInit {
  private readonly dialogRef = inject(MatDialogRef<AdicionarServicoDiagnosticoDialogComponent>);
  private readonly data = inject<AdicionarServicoDiagnosticoDialogData>(MAT_DIALOG_DATA);
  private readonly servicoService = inject(ServicoService);

  readonly carregando = signal(true);
  readonly servicos = signal<ServicoResponse[]>([]);
  readonly selecionados = signal<Set<number>>(new Set());

  ngOnInit(): void {
    this.servicoService.listar(0, 100).subscribe({
      next: (page) => {
        const disponiveis = page.content.filter(
          (s) => !this.data.servicosJaAdicionados.includes(s.id),
        );
        this.servicos.set(disponiveis);
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }

  toggle(id: number): void {
    const set = new Set(this.selecionados());
    if (set.has(id)) {
      set.delete(id);
    } else {
      set.add(id);
    }
    this.selecionados.set(set);
  }

  confirmar(): void {
    const servicos: ServicoSolicitadoRequest[] = [...this.selecionados()].map((id) => ({ servicoId: id }));
    this.dialogRef.close(servicos);
  }

  cancelar(): void {
    this.dialogRef.close(null);
  }
}
