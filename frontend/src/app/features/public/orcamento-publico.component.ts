import {CurrencyPipe, DatePipe} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {finalize} from 'rxjs';
import {OrcamentoPublico, OrcamentoPublicoService, StatusOrcamentoPublico} from './orcamento-publico.service';

@Component({
  selector: 'app-orcamento-publico',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, FormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressSpinnerModule, MatSnackBarModule],
  templateUrl: './orcamento-publico.component.html',
  styleUrl: './orcamento-publico.component.scss',
})
export class OrcamentoPublicoComponent implements OnInit {
  orcamento: OrcamentoPublico | null = null;
  carregando = false;
  processando = false;
  erro = '';
  nome = '';
  motivo = '';
  private id = 0;
  private token = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly service: OrcamentoPublicoService,
    private readonly snackBar: MatSnackBar,
  ) {
  }

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.id || !this.token) {
      this.erro = 'Este link está incompleto.';
      return;
    }
    this.consultar();
  }

  consultar(): void {
    this.carregando = true;
    this.erro = '';
    this.service.consultar(this.id, this.token).pipe(finalize(() => this.carregando = false)).subscribe({
      next: orcamento => this.orcamento = orcamento,
      error: () => this.erro = 'Este link é inválido ou expirou.',
    });
  }

  baixarPdf(): void {
    this.service.baixarPdf(this.id, this.token).subscribe({
      next: arquivo => {
        const url = URL.createObjectURL(arquivo);
        const link = document.createElement('a');
        link.href = url;
        link.download = `orcamento-${this.id}.pdf`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.snackBar.open('Não foi possível baixar o orçamento.', 'Fechar', {duration: 5000}),
    });
  }

  aprovar(): void {
    this.processando = true;
    this.service.aprovar(this.id, this.token, this.nome).pipe(finalize(() => this.processando = false)).subscribe({
      next: orcamento => {
        this.orcamento = orcamento;
        this.snackBar.open('Orçamento aprovado com sucesso.', 'Fechar', {duration: 4000});
      },
      error: () => this.snackBar.open('Não foi possível aprovar o orçamento.', 'Fechar', {duration: 5000}),
    });
  }

  recusar(): void {
    this.processando = true;
    this.service.recusar(this.id, this.token, this.motivo, this.nome).pipe(finalize(() => this.processando = false)).subscribe({
      next: orcamento => {
        this.orcamento = orcamento;
        this.snackBar.open('Recusa registrada com sucesso.', 'Fechar', {duration: 4000});
      },
      error: () => this.snackBar.open('Não foi possível recusar o orçamento.', 'Fechar', {duration: 5000}),
    });
  }

  statusLabel(status: StatusOrcamentoPublico): string {
    return {
      DISPONIVEL: 'Aguardando sua decisão',
      APROVADO: 'Aprovado',
      REPROVADO: 'Recusado',
      SUBSTITUIDO: 'Substituído',
    }[status];
  }

  irParaLogin(): void {
    this.router.navigate(['/login']);
  }
}
