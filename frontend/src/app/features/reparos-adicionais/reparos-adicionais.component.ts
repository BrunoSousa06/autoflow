import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { OrcamentoService } from '../orcamentos/orcamento.service';
import { OrcamentoResponse, StatusOrcamento, STATUS_ORCAMENTO_LABEL } from '../orcamentos/orcamento.model';

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

  readonly loading = signal(false);
  readonly itens = signal<OrcamentoResponse[]>([]);

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
    }).subscribe({
      next: lista => {
        this.itens.set(lista);
        this.loading.set(false);
      },
      error: () => {
        this.snackBar.open('Não foi possível carregar os reparos adicionais.', 'Fechar', { duration: 5000 });
        this.loading.set(false);
      },
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

  verOrcamento(id: number): void {
    this.router.navigate(['/orcamentos', id]);
  }

  verOs(numeroOs: string): void {
    this.router.navigate(['/ordens-servico', numeroOs]);
  }
}
