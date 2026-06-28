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
  templateUrl: './cliente-form-dialog.component.html',
  styleUrl: './cliente-form-dialog.component.scss',
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
