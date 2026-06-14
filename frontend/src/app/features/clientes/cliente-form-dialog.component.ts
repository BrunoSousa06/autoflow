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
import { ClienteService } from './cliente.service';
import {
  ClienteRequest,
  ClienteResponse,
  cpfCnpjValidator,
  formatarCpfCnpj,
  formatarTelefone,
} from './cliente.model';

export interface ClienteFormDialogData {
  cliente: ClienteResponse | null;
}

@Component({
  selector: 'app-cliente-form-dialog',
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
      <mat-icon>{{ edicao ? 'edit' : 'person_add' }}</mat-icon>
      {{ edicao ? 'Editar Cliente' : 'Novo Cliente' }}
    </h2>

    <mat-dialog-content>
      <form [formGroup]="form" novalidate>

        <!-- Nome -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Nome completo</mat-label>
          <mat-icon matPrefix>person</mat-icon>
          <input matInput formControlName="nome" autocomplete="off" />
          @if (f['nome'].hasError('required')) {
            <mat-error>Nome é obrigatório</mat-error>
          } @else if (f['nome'].hasError('backend')) {
            <mat-error>{{ f['nome'].getError('backend') }}</mat-error>
          }
        </mat-form-field>

        <!-- CPF/CNPJ -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>CPF / CNPJ</mat-label>
          <mat-icon matPrefix>badge</mat-icon>
          <input
            matInput
            formControlName="cpfCnpj"
            placeholder="000.000.000-00 ou 00.000.000/0000-00"
            autocomplete="off"
            (input)="mascaraCpfCnpj($event)"
          />
          @if (edicao) {
            <mat-hint>CPF/CNPJ não pode ser alterado após o cadastro</mat-hint>
          }
          @if (f['cpfCnpj'].hasError('required')) {
            <mat-error>CPF/CNPJ é obrigatório</mat-error>
          } @else if (f['cpfCnpj'].hasError('cpfCnpj')) {
            <mat-error>{{ f['cpfCnpj'].getError('cpfCnpj') }}</mat-error>
          } @else if (f['cpfCnpj'].hasError('backend')) {
            <mat-error>{{ f['cpfCnpj'].getError('backend') }}</mat-error>
          }
        </mat-form-field>

        <!-- Telefone -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Telefone</mat-label>
          <mat-icon matPrefix>phone</mat-icon>
          <input
            matInput
            formControlName="telefone"
            placeholder="(00) 90000-0000"
            autocomplete="off"
            (input)="mascaraTelefone($event)"
          />
          @if (f['telefone'].hasError('required')) {
            <mat-error>Telefone é obrigatório</mat-error>
          } @else if (f['telefone'].hasError('backend')) {
            <mat-error>{{ f['telefone'].getError('backend') }}</mat-error>
          }
        </mat-form-field>

        <!-- E-mail -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>E-mail</mat-label>
          <mat-icon matPrefix>email</mat-icon>
          <input matInput formControlName="email" type="email" autocomplete="off" />
          @if (f['email'].hasError('required')) {
            <mat-error>E-mail é obrigatório</mat-error>
          } @else if (f['email'].hasError('email')) {
            <mat-error>Formato de e-mail inválido</mat-error>
          } @else if (f['email'].hasError('backend')) {
            <mat-error>{{ f['email'].getError('backend') }}</mat-error>
          }
        </mat-form-field>

        <!-- Erro geral do backend (409, 404, etc.) -->
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
export class ClienteFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly clienteService = inject(ClienteService);
  private readonly dialogRef = inject(MatDialogRef<ClienteFormDialogComponent>);
  private readonly data = inject<ClienteFormDialogData>(MAT_DIALOG_DATA);
  private readonly snackBar = inject(MatSnackBar);

  readonly edicao = this.data.cliente !== null;
  readonly loading = signal(false);
  readonly erroBackend = signal<string | null>(null);

  readonly form = this.fb.group({
    nome:     ['', [Validators.required]],
    cpfCnpj:  ['', [Validators.required, cpfCnpjValidator()]],
    telefone: ['', [Validators.required]],
    email:    ['', [Validators.required, Validators.email]],
  });

  get f() { return this.form.controls; }

  constructor() {
    if (this.data.cliente) {
      const c = this.data.cliente;
      this.form.patchValue({
        nome:     c.nome,
        cpfCnpj:  formatarCpfCnpj(c.cpfCnpj),
        telefone: formatarTelefone(c.telefone),
        email:    c.email,
      });
      // Desabilita CPF/CNPJ na edição — valor ainda é enviado via getRawValue()
      this.f['cpfCnpj'].disable();
    }
  }

  mascaraCpfCnpj(event: Event): void {
    const input = event.target as HTMLInputElement;
    const formatted = formatarCpfCnpj(input.value);
    input.value = formatted;
    this.f['cpfCnpj'].setValue(formatted, { emitEvent: false });
  }

  mascaraTelefone(event: Event): void {
    const input = event.target as HTMLInputElement;
    const formatted = formatarTelefone(input.value);
    input.value = formatted;
    this.f['telefone'].setValue(formatted, { emitEvent: false });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.erroBackend.set(null);

    // getRawValue() inclui campos disabled (cpfCnpj na edição)
    const raw = this.form.getRawValue();
    const req: ClienteRequest = {
      nome:     raw.nome!,
      cpfCnpj:  raw.cpfCnpj!.replace(/\D/g, ''), // apenas dígitos para satisfazer @Size(11-14)
      telefone: raw.telefone!,
      email:    raw.email!,
    };

    const obs = this.edicao
      ? this.clienteService.atualizar(this.data.cliente!.id, req)
      : this.clienteService.cadastrar(req);

    obs.subscribe({
      next: () => {
        this.snackBar.open(
          this.edicao ? 'Cliente atualizado com sucesso!' : 'Cliente cadastrado com sucesso!',
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

    // Erro de negócio: { "erro": "mensagem" }
    if (body['erro']) { this.erroBackend.set(body['erro']); return; }

    // Erros de validação do backend: { "campo": "mensagem", ... }
    const campoParaControl: Record<string, string> = {
      nome: 'nome', cpfCnpj: 'cpfCnpj', telefone: 'telefone', email: 'email',
    };
    let temErroCampo = false;
    for (const [campo, msg] of Object.entries(body as Record<string, string>)) {
      const controlName = campoParaControl[campo];
      if (controlName) {
        this.form.get(controlName)?.enable();
        this.form.get(controlName)?.setErrors({ backend: msg });
        temErroCampo = true;
      }
    }
    if (!temErroCampo) {
      this.erroBackend.set('Erro ao processar a requisição.');
    }
  }
}
