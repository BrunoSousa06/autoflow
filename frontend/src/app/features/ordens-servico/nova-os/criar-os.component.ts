import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatStepperModule } from '@angular/material/stepper';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { OrdemServicoService } from '../ordem-servico.service';
import { CriarOrdemServicoRequest } from '../ordem-servico.model';
import { ServicoService } from '../../servicos/servico.service';
import { ServicoResponse } from '../../servicos/servico.model';
import { cpfCnpjValidator, formatarCpfCnpj } from '../../clientes/cliente.model';

@Component({
  selector: 'app-nova-os',
  standalone: true,
  imports: [
    CommonModule,
    CurrencyPipe,
    ReactiveFormsModule,
    MatStepperModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatDividerModule,
  ],
  templateUrl: './criar-os.component.html',
  styleUrl: './criar-os.component.scss',
})
export class CriarOsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly osService = inject(OrdemServicoService);
  private readonly servicoService = inject(ServicoService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly anoMax = new Date().getFullYear() + 1;

  readonly formCliente = this.fb.group({
    cpfCnpj: ['', [Validators.required, cpfCnpjValidator()]],
  });

  readonly formVeiculo = this.fb.group({
    placa: ['', [Validators.required, Validators.pattern(/^[A-Z]{3}\d{4}$|^[A-Z]{3}\d[A-Z]\d{2}$/)]],
    marca: [''],
    modelo: [''],
    ano: [null as number | null, [Validators.min(1900), Validators.max(this.anoMax)]],
  });

  readonly servicos = signal<ServicoResponse[]>([]);
  readonly loadingServicos = signal(false);
  readonly erroServicos = signal<string | null>(null);
  readonly servicosSelecionados = signal<Set<number>>(new Set());
  readonly loading = signal(false);
  readonly erroBackend = signal<string | null>(null);

  get fc() { return this.formCliente.controls; }
  get fv() { return this.formVeiculo.controls; }

  ngOnInit(): void {
    this.carregarServicos();
  }

  carregarServicos(): void {
    this.loadingServicos.set(true);
    this.erroServicos.set(null);
    this.servicoService.listar(0, 100).subscribe({
      next: (pagina) => {
        this.servicos.set(pagina.content);
        this.loadingServicos.set(false);
      },
      error: () => {
        this.erroServicos.set('Não foi possível carregar o catálogo de serviços.');
        this.loadingServicos.set(false);
      },
    });
  }

  onCpfCnpjInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const formatted = formatarCpfCnpj(input.value);
    this.formCliente.controls.cpfCnpj.setValue(formatted, { emitEvent: false });
    input.value = formatted;
  }

  onPlacaInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const upper = input.value.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 7);
    this.formVeiculo.controls.placa.setValue(upper, { emitEvent: false });
    input.value = upper;
  }

  toggleServico(id: number): void {
    const atual = new Set(this.servicosSelecionados());
    if (atual.has(id)) {
      atual.delete(id);
    } else {
      atual.add(id);
    }
    this.servicosSelecionados.set(atual);
  }

  criar(): void {
    if (this.formCliente.invalid || this.formVeiculo.invalid || this.servicosSelecionados().size === 0) {
      return;
    }

    this.loading.set(true);
    this.erroBackend.set(null);

    const raw = this.formCliente.getRawValue();
    const cpfCnpjSemMascara = (raw.cpfCnpj ?? '').replace(/\D/g, '');

    const vRaw = this.formVeiculo.getRawValue();
    const req: CriarOrdemServicoRequest = {
      cpfCnpj: cpfCnpjSemMascara,
      veiculo: {
        placa: vRaw.placa!,
        marca: vRaw.marca || null,
        modelo: vRaw.modelo || null,
        ano: vRaw.ano || null,
      },
      servicosSolicitados: Array.from(this.servicosSelecionados()).map(id => ({ servicoId: id })),
    };

    this.osService.criar(req).subscribe({
      next: (os) => {
        this.loading.set(false);
        this.snackBar.open(`OS ${os.numeroOs} criada com sucesso!`, 'Fechar', { duration: 4000 });
        this.router.navigate(['/ordens-servico']);
      },
      error: (err) => {
        this.loading.set(false);
        this.erroBackend.set(this.extrairErro(err));
      },
    });
  }

  voltar(): void {
    this.router.navigate(['/ordens-servico']);
  }

  private extrairErro(err: any): string {
    const body = err?.error;
    if (!body) return 'Erro inesperado. Tente novamente.';
    if (typeof body === 'string') return body;
    if (body.erro) return body.erro;
    if (body.message) return body.message;
    const msgs = Object.values(body).filter((v): v is string => typeof v === 'string');
    return msgs.length ? msgs.join(' ') : 'Erro ao criar ordem de serviço.';
  }
}
