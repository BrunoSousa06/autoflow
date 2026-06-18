import { Component, inject, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { OrdemServicoService } from './ordem-servico.service';
import { OrdemServicoFiltro, OrdemServicoResponse, STATUS_OS_LABEL, StatusOrdemServico } from './ordem-servico.model';

@Component({
  selector: 'app-ordens-servico',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatPaginatorModule,
  ],
  templateUrl: './ordens-servico.component.html',
  styleUrl: './ordens-servico.component.scss',
})
export class OrdensServicoComponent implements OnInit, OnDestroy {
  private readonly service = inject(OrdemServicoService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly destroy$ = new Subject<void>();

  readonly ordens = signal<OrdemServicoResponse[]>([]);
  readonly loading = signal(true);
  readonly erroCarregamento = signal<string | null>(null);
  readonly totalElements = signal(0);
  readonly pageSize = signal(10);
  readonly pageIndex = signal(0);

  readonly podeCriar = ['ADMIN', 'ATENDENTE'].includes(this.auth.getRole() ?? '');
  readonly colunas = ['numeroOs', 'cliente', 'status', 'dataAbertura', 'servicos'];
  readonly statusOptions: StatusOrdemServico[] = [
    'RECEBIDA', 'EM_DIAGNOSTICO', 'AGUARDANDO_APROVACAO', 'EM_EXECUCAO', 'FINALIZADA', 'ENTREGUE',
  ];

  readonly filtroForm = new FormGroup({
    cliente: new FormControl(''),
    numeroOs: new FormControl(''),
    status: new FormControl<StatusOrdemServico | ''>(''),
  });

  ngOnInit(): void {
    this.filtroForm.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
      takeUntil(this.destroy$),
    ).subscribe(() => {
      this.pageIndex.set(0);
      this.carregar();
    });
    this.carregar();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  carregar(): void {
    this.loading.set(true);
    this.erroCarregamento.set(null);

    const val = this.filtroForm.value;
    const filtro: OrdemServicoFiltro = {
      cliente: val.cliente || undefined,
      numeroOs: val.numeroOs || undefined,
      status: (val.status as StatusOrdemServico) || undefined,
      page: this.pageIndex(),
      size: this.pageSize(),
    };

    this.service.listar(filtro).subscribe({
      next: (page) => {
        this.ordens.set(page.content);
        this.totalElements.set(page.page.totalElements);
        this.loading.set(false);
      },
      error: (err) => {
        const msg = err?.error?.erro ?? err?.error ?? 'Não foi possível carregar as ordens de serviço.';
        this.erroCarregamento.set(typeof msg === 'string' ? msg : 'Erro ao carregar ordens de serviço.');
        this.loading.set(false);
      },
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.carregar();
  }

  limparFiltros(): void {
    this.filtroForm.reset({ cliente: '', numeroOs: '', status: '' });
  }

  temFiltrosAtivos(): boolean {
    const val = this.filtroForm.value;
    return !!(val.cliente || val.numeroOs || val.status);
  }

  novaOs(): void {
    this.router.navigate(['/ordens-servico/nova']);
  }

  verDetalhe(os: OrdemServicoResponse): void {
    this.router.navigate(['/ordens-servico', os.numeroOs]);
  }

  labelStatus(status: StatusOrdemServico): string {
    return STATUS_OS_LABEL[status] ?? status;
  }
}
