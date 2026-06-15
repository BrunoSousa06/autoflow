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
  template: `
    <div class="page">
      <div class="page-header">
        <button mat-icon-button (click)="voltar()" matTooltip="Voltar para listagem">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <div>
          <h1>Criar Ordem de Serviço</h1>
          <p class="subtitle">Preencha os dados para abrir uma OS</p>
        </div>
      </div>

      <mat-card class="wizard-card">
        <mat-stepper linear #stepper>

          <!-- ── Passo 1: Cliente ─────────────────────────────────────── -->
          <mat-step [stepControl]="formCliente" label="Cliente">
            <div class="step-content">
              <p class="step-desc">Informe o CPF ou CNPJ do cliente que já deve estar cadastrado no sistema.</p>
              <form [formGroup]="formCliente" novalidate>
                <mat-form-field appearance="outline" class="field-full">
                  <mat-label>CPF / CNPJ do Cliente</mat-label>
                  <mat-icon matPrefix>badge</mat-icon>
                  <input
                    matInput
                    formControlName="cpfCnpj"
                    (input)="onCpfCnpjInput($event)"
                    placeholder="000.000.000-00 ou 00.000.000/0000-00"
                    autocomplete="off"
                  />
                  @if (fc['cpfCnpj'].touched && fc['cpfCnpj'].hasError('required')) {
                    <mat-error>CPF/CNPJ é obrigatório</mat-error>
                  } @else if (fc['cpfCnpj'].touched && fc['cpfCnpj'].hasError('cpfCnpj')) {
                    <mat-error>{{ fc['cpfCnpj'].getError('cpfCnpj') }}</mat-error>
                  }
                </mat-form-field>
              </form>
            </div>
            <div class="step-actions">
              <span></span>
              <button
                mat-raised-button
                color="primary"
                matStepperNext
                [disabled]="formCliente.invalid"
              >
                Próximo
                <mat-icon>chevron_right</mat-icon>
              </button>
            </div>
          </mat-step>

          <!-- ── Passo 2: Veículo ─────────────────────────────────────── -->
          <mat-step [stepControl]="formVeiculo" label="Veículo">
            <div class="step-content">
              <p class="step-desc">
                Informe a placa do veículo. Se já estiver cadastrado, os dados serão aproveitados.
                Caso contrário, será criado automaticamente.
              </p>
              <form [formGroup]="formVeiculo" novalidate>
                <mat-form-field appearance="outline" class="field-full">
                  <mat-label>Placa</mat-label>
                  <mat-icon matPrefix>directions_car</mat-icon>
                  <input
                    matInput
                    formControlName="placa"
                    (input)="onPlacaInput($event)"
                    placeholder="ABC1234 ou ABC1D23"
                    maxlength="7"
                    autocomplete="off"
                  />
                  @if (fv['placa'].touched && fv['placa'].hasError('required')) {
                    <mat-error>Placa é obrigatória</mat-error>
                  } @else if (fv['placa'].touched && fv['placa'].hasError('pattern')) {
                    <mat-error>Placa inválida — use o formato ABC1234 ou ABC1D23</mat-error>
                  }
                </mat-form-field>

                <div class="form-grid-3">
                  <mat-form-field appearance="outline">
                    <mat-label>Marca (opcional)</mat-label>
                    <input matInput formControlName="marca" placeholder="Toyota" />
                  </mat-form-field>

                  <mat-form-field appearance="outline">
                    <mat-label>Modelo (opcional)</mat-label>
                    <input matInput formControlName="modelo" placeholder="Corolla" />
                  </mat-form-field>

                  <mat-form-field appearance="outline">
                    <mat-label>Ano (opcional)</mat-label>
                    <input matInput formControlName="ano" type="number" [min]="1900" [max]="anoMax" placeholder="2020" />
                    @if (fv['ano'].hasError('min') || fv['ano'].hasError('max')) {
                      <mat-error>Ano inválido</mat-error>
                    }
                  </mat-form-field>
                </div>
              </form>
            </div>
            <div class="step-actions">
              <button mat-button matStepperPrevious>
                <mat-icon>chevron_left</mat-icon>
                Anterior
              </button>
              <button
                mat-raised-button
                color="primary"
                matStepperNext
                [disabled]="formVeiculo.invalid"
              >
                Próximo
                <mat-icon>chevron_right</mat-icon>
              </button>
            </div>
          </mat-step>

          <!-- ── Passo 3: Serviços ────────────────────────────────────── -->
          <mat-step label="Serviços">
            <div class="step-content">
              <p class="step-desc">Selecione um ou mais serviços solicitados pelo cliente.</p>

              @if (loadingServicos()) {
                <div class="loading-servicos">
                  <mat-spinner diameter="32" />
                  <span>Carregando serviços...</span>
                </div>
              } @else if (erroServicos()) {
                <div class="error-banner">
                  <mat-icon>error_outline</mat-icon>
                  <span>{{ erroServicos() }}</span>
                </div>
              } @else {
                <div class="servicos-lista">
                  @for (s of servicos(); track s.id) {
                    <div
                      class="servico-item"
                      [class.selecionado]="servicosSelecionados().has(s.id)"
                      (click)="toggleServico(s.id)"
                    >
                      <mat-checkbox
                        [checked]="servicosSelecionados().has(s.id)"
                        (change)="toggleServico(s.id)"
                        (click)="$event.stopPropagation()"
                      >
                        <span class="servico-nome">{{ s.nome }}</span>
                      </mat-checkbox>
                      @if (s.valor != null) {
                        <span class="servico-valor">{{ s.valor | currency:'BRL':'symbol':'1.2-2' }}</span>
                      }
                    </div>
                  }
                </div>

                @if (servicosSelecionados().size === 0) {
                  <p class="aviso-selecao">Selecione ao menos um serviço para continuar.</p>
                }
              }

              @if (erroBackend()) {
                <div class="error-banner">
                  <mat-icon>error_outline</mat-icon>
                  <span>{{ erroBackend() }}</span>
                </div>
              }
            </div>

            <div class="step-actions">
              <button mat-button matStepperPrevious>
                <mat-icon>chevron_left</mat-icon>
                Anterior
              </button>
              <button
                mat-raised-button
                color="primary"
                (click)="criar()"
                [disabled]="loading() || loadingServicos() || servicosSelecionados().size === 0"
              >
                @if (loading()) {
                  <mat-spinner diameter="18" />
                } @else {
                  <mat-icon>check_circle</mat-icon>
                }
                Criar OS
              </button>
            </div>
          </mat-step>

        </mat-stepper>
      </mat-card>
    </div>
  `,
  styles: [`
    .page {
      padding: 24px;
      max-width: 800px;
      margin: 0 auto;
    }

    .page-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 24px;

      h1 {
        margin: 0 0 2px;
        font-size: 1.5rem;
        font-weight: 600;
        color: #1a1a1a;
      }

      .subtitle {
        margin: 0;
        color: #666;
        font-size: 0.875rem;
      }
    }

    .wizard-card {
      padding: 0;
      overflow: hidden;

      ::ng-deep .mat-stepper-horizontal {
        background: transparent;
      }

      ::ng-deep .mat-horizontal-content-container {
        padding: 0;
      }
    }

    .step-content {
      padding: 24px 24px 8px;

      .step-desc {
        margin: 0 0 20px;
        color: #555;
        font-size: 0.9rem;
        line-height: 1.5;
      }
    }

    .step-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 24px 24px;
      gap: 12px;

      button {
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }

    .field-full { width: 100%; }

    .form-grid-3 {
      display: grid;
      grid-template-columns: 1fr 1fr 120px;
      gap: 12px;
    }

    .loading-servicos {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 24px 0;
      color: #666;
    }

    .servicos-lista {
      display: flex;
      flex-direction: column;
      gap: 4px;
      margin-bottom: 8px;
      max-height: 320px;
      overflow-y: auto;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
    }

    .servico-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 16px;
      cursor: pointer;
      transition: background 0.15s;
      border-bottom: 1px solid #f0f0f0;

      &:last-child { border-bottom: none; }

      &:hover { background: #f5f5f5; }

      &.selecionado { background: #e3f2fd; }

      .servico-nome { font-size: 0.9rem; }

      .servico-valor {
        font-size: 0.875rem;
        font-weight: 500;
        color: #2e7d32;
        white-space: nowrap;
        margin-left: 12px;
      }
    }

    .aviso-selecao {
      margin: 8px 0 0;
      font-size: 0.85rem;
      color: #e65100;
    }

    .error-banner {
      display: flex;
      align-items: center;
      gap: 8px;
      background: #fdecea;
      color: #c62828;
      border-radius: 6px;
      padding: 10px 14px;
      margin-top: 12px;
      font-size: 0.9rem;

      mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
        flex-shrink: 0;
      }
    }

    mat-spinner { display: inline-block; }

    @media (max-width: 600px) {
      .form-grid-3 { grid-template-columns: 1fr; }
    }
  `],
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
