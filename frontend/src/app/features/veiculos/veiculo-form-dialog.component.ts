import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { VeiculoService } from './veiculo.service';
import {
  VeiculoResponse,
  VeiculoRequest,
  VeiculoUpdateRequest,
  placaValidator,
  normalizarPlaca,
} from './veiculo.model';
import { AuthService } from '../../core/services/auth.service';
import { ClienteService } from '../clientes/cliente.service';

export interface VeiculoFormDialogData {
  veiculo: VeiculoResponse | null;
}

@Component({
  selector: 'app-veiculo-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>{{ edicao ? 'edit' : 'directions_car' }}</mat-icon>
      {{ edicao ? 'Editar Veículo' : 'Novo Veículo' }}
    </h2>

    <mat-dialog-content>
      <form [formGroup]="form" novalidate>

        <!-- CPF/CNPJ do cliente — apenas no cadastro -->
        @if (!edicao) {
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>CPF / CNPJ do cliente</mat-label>
            <mat-icon matPrefix>badge</mat-icon>
            @if (carregandoPerfil()) {
              <mat-spinner matSuffix diameter="16" />
            }
            <input
              matInput
              formControlName="cpfCnpj"
              placeholder="Apenas dígitos — ex: 12345678901"
              autocomplete="off"
            />
            <mat-hint>
              @if (isCliente) {
                Preenchido automaticamente com seu CPF/CNPJ cadastrado
              } @else {
                Informe o CPF (11 dígitos) ou CNPJ (14 dígitos) sem pontuação
              }
            </mat-hint>
            @if (f['cpfCnpj'].touched && f['cpfCnpj'].hasError('required')) {
              <mat-error>CPF/CNPJ é obrigatório</mat-error>
            } @else if (f['cpfCnpj'].touched && f['cpfCnpj'].hasError('minlength')) {
              <mat-error>CPF deve ter 11 dígitos ou CNPJ 14 dígitos</mat-error>
            } @else if (f['cpfCnpj'].touched && f['cpfCnpj'].hasError('maxlength')) {
              <mat-error>CPF deve ter 11 dígitos ou CNPJ 14 dígitos</mat-error>
            } @else if (f['cpfCnpj'].hasError('backend')) {
              <mat-error>{{ f['cpfCnpj'].getError('backend') }}</mat-error>
            }
          </mat-form-field>
        }

        <!-- Marca -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Marca</mat-label>
          <mat-icon matPrefix>business</mat-icon>
          <input matInput formControlName="marca" autocomplete="off" placeholder="Ex: Toyota" />
          @if (f['marca'].touched && f['marca'].hasError('required')) {
            <mat-error>Marca é obrigatória</mat-error>
          } @else if (f['marca'].hasError('backend')) {
            <mat-error>{{ f['marca'].getError('backend') }}</mat-error>
          }
        </mat-form-field>

        <!-- Modelo -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Modelo</mat-label>
          <mat-icon matPrefix>directions_car</mat-icon>
          <input matInput formControlName="modelo" autocomplete="off" placeholder="Ex: Corolla" />
          @if (f['modelo'].touched && f['modelo'].hasError('required')) {
            <mat-error>Modelo é obrigatório</mat-error>
          } @else if (f['modelo'].hasError('backend')) {
            <mat-error>{{ f['modelo'].getError('backend') }}</mat-error>
          }
        </mat-form-field>

        <!-- Ano -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Ano</mat-label>
          <mat-icon matPrefix>calendar_today</mat-icon>
          <input
            matInput
            formControlName="ano"
            type="number"
            placeholder="Ex: 2022"
            min="1900"
            max="2100"
          />
          @if (f['ano'].touched && f['ano'].hasError('required')) {
            <mat-error>Ano é obrigatório</mat-error>
          } @else if (f['ano'].touched && f['ano'].hasError('min')) {
            <mat-error>Ano inválido</mat-error>
          } @else if (f['ano'].hasError('backend')) {
            <mat-error>{{ f['ano'].getError('backend') }}</mat-error>
          }
        </mat-form-field>

        <!-- Placa -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Placa</mat-label>
          <mat-icon matPrefix>pin</mat-icon>
          <input
            matInput
            formControlName="placa"
            placeholder="ABC1234 ou ABC1D23"
            autocomplete="off"
            (input)="normalizarPlacaInput($event)"
          />
          <mat-hint>Formato: ABC1234 (antigo) ou ABC1D23 (Mercosul)</mat-hint>
          @if (f['placa'].touched && f['placa'].hasError('required')) {
            <mat-error>Placa é obrigatória</mat-error>
          } @else if (f['placa'].touched && f['placa'].hasError('placa')) {
            <mat-error>{{ f['placa'].getError('placa') }}</mat-error>
          } @else if (f['placa'].hasError('backend')) {
            <mat-error>{{ f['placa'].getError('backend') }}</mat-error>
          }
        </mat-form-field>

        <!-- Erro geral do backend -->
        @if (erroBackend()) {
          <div class="error-banner">
            <mat-icon>error_outline</mat-icon>
            <span>{{ erroBackend() }}</span>
          </div>
        }

      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="cancelar()" [disabled]="loading()">
        Cancelar
      </button>
      <button
        mat-raised-button
        color="primary"
        (click)="salvar()"
        [disabled]="loading()"
      >
        @if (loading()) {
          <mat-spinner diameter="18" />
        } @else {
          <mat-icon>save</mat-icon>
        }
        {{ edicao ? 'Salvar alterações' : 'Cadastrar' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    h2[mat-dialog-title] {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 1.1rem;
    }

    mat-dialog-content {
      min-width: 460px;
      padding-top: 8px !important;
    }

    .full-width {
      width: 100%;
      margin-bottom: 4px;
    }

    .error-banner {
      display: flex;
      align-items: center;
      gap: 8px;
      background: #fdecea;
      color: #c62828;
      border-radius: 6px;
      padding: 10px 14px;
      margin-top: 8px;
      font-size: 0.9rem;

      mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
        flex-shrink: 0;
      }
    }

    mat-dialog-actions {
      gap: 8px;
      padding-top: 8px;

      button {
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }

    mat-spinner {
      display: inline-block;
    }
  `],
})
export class VeiculoFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly veiculoService = inject(VeiculoService);
  private readonly clienteService = inject(ClienteService);
  private readonly authService = inject(AuthService);
  private readonly dialogRef = inject(MatDialogRef<VeiculoFormDialogComponent>);
  private readonly data = inject<VeiculoFormDialogData>(MAT_DIALOG_DATA);
  private readonly snackBar = inject(MatSnackBar);

  readonly edicao = this.data.veiculo !== null;
  readonly isCliente = this.authService.getRole() === 'CLIENTE';
  readonly loading = signal(false);
  readonly carregandoPerfil = signal(false);
  readonly erroBackend = signal<string | null>(null);

  readonly form = this.fb.group({
    cpfCnpj: ['', [Validators.minLength(11), Validators.maxLength(14)]],
    marca:   ['', [Validators.required]],
    modelo:  ['', [Validators.required]],
    ano:     [null as number | null, [Validators.required, Validators.min(1900)]],
    placa:   ['', [Validators.required, placaValidator()]],
  });

  get f() { return this.form.controls; }

  constructor() {
    if (!this.edicao) {
      this.f['cpfCnpj'].addValidators(Validators.required);
      this.f['cpfCnpj'].updateValueAndValidity();

      if (this.isCliente) {
        this.carregandoPerfil.set(true);
        this.clienteService.meuPerfil().subscribe({
          next: (cliente) => {
            this.f['cpfCnpj'].setValue(cliente.cpfCnpj);
            this.f['cpfCnpj'].disable();
            this.carregandoPerfil.set(false);
          },
          error: () => {
            this.carregandoPerfil.set(false);
            this.erroBackend.set('Não foi possível carregar seu perfil. Informe o CPF/CNPJ manualmente.');
          },
        });
      }
    }

    if (this.data.veiculo) {
      const v = this.data.veiculo;
      this.form.patchValue({
        marca:  v.marca,
        modelo: v.modelo,
        ano:    v.ano,
        placa:  v.placa,
      });
      this.f['cpfCnpj'].disable();
    }
  }

  normalizarPlacaInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const normalizado = normalizarPlaca(input.value);
    input.value = normalizado;
    this.f['placa'].setValue(normalizado, { emitEvent: false });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.erroBackend.set(null);

    const raw = this.form.getRawValue();

    const obs = this.edicao
      ? this.veiculoService.atualizar(this.data.veiculo!.id, {
          marca:  raw.marca!,
          modelo: raw.modelo!,
          ano:    raw.ano!,
          placa:  normalizarPlaca(raw.placa!),
        } as VeiculoUpdateRequest)
      : this.veiculoService.cadastrar({
          cpfCnpj: raw.cpfCnpj!.replace(/\D/g, ''),
          marca:   raw.marca!,
          modelo:  raw.modelo!,
          ano:     raw.ano!,
          placa:   normalizarPlaca(raw.placa!),
        } as VeiculoRequest);

    obs.subscribe({
      next: () => {
        this.snackBar.open(
          this.edicao ? 'Veículo atualizado com sucesso!' : 'Veículo cadastrado com sucesso!',
          'Fechar',
          { duration: 3000 }
        );
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.loading.set(false);
        this.tratarErro(err);
      },
    });
  }

  cancelar(): void {
    this.dialogRef.close(false);
  }

  private tratarErro(err: any): void {
    const body = err?.error;
    if (!body) { this.erroBackend.set('Erro inesperado. Tente novamente.'); return; }
    if (typeof body === 'string') { this.erroBackend.set(body); return; }

    if (body['erro']) { this.erroBackend.set(body['erro']); return; }

    // Erros de validação por campo do backend
    const campoMap: Record<string, string> = {
      cpfCnpj: 'cpfCnpj',
      marca: 'marca',
      modelo: 'modelo',
      ano: 'ano',
      placa: 'placa',
    };
    let temErroCampo = false;
    for (const [campo, msg] of Object.entries(body as Record<string, string>)) {
      const ctrl = campoMap[campo];
      if (ctrl) {
        this.form.get(ctrl)?.enable();
        this.form.get(ctrl)?.setErrors({ backend: msg });
        temErroCampo = true;
      }
    }
    if (!temErroCampo) {
      this.erroBackend.set('Erro ao processar a requisição.');
    }
  }
}
