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
  templateUrl: './veiculo-form-dialog.component.html',
  styleUrl: './veiculo-form-dialog.component.scss',
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
