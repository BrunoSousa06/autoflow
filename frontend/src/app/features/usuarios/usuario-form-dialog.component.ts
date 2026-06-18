import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { UsuarioAdminService } from './usuario.service';
import {
  RoleEnum,
  ROLE_LABELS,
  UsuarioRequest,
} from './usuario.model';
import { formatarCpfCnpj, formatarTelefone, cpfCnpjValidator } from '../clientes/cliente.model';

const SENHA_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

function senhaIgualValidator(group: AbstractControl): ValidationErrors | null {
  const senha = group.get('senha')?.value;
  const confirmar = group.get('confirmarSenha')?.value;
  if (senha && confirmar && senha !== confirmar) {
    group.get('confirmarSenha')?.setErrors({ senhasDiferentes: true });
    return { senhasDiferentes: true };
  }
  if (confirmar && group.get('confirmarSenha')?.hasError('senhasDiferentes')) {
    group.get('confirmarSenha')?.setErrors(null);
  }
  return null;
}

@Component({
  selector: 'app-usuario-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './usuario-form-dialog.component.html',
  styleUrl: './usuario-form-dialog.component.scss',
})
export class UsuarioFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(UsuarioAdminService);
  private readonly dialogRef = inject(MatDialogRef<UsuarioFormDialogComponent>);
  private readonly auth = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);
  readonly data = inject(MAT_DIALOG_DATA);

  readonly loading = signal(false);
  readonly erroBackend = signal<string | null>(null);
  readonly roleLabels = ROLE_LABELS;
  mostrarSenha = false;

  readonly rolesDisponiveis = computed<RoleEnum[]>(() =>
    this.auth.getRole() === 'ADMIN'
      ? ['ADMIN', 'ATENDENTE', 'MECANICO', 'CLIENTE']
      : ['ATENDENTE', 'CLIENTE']
  );

  readonly form = this.fb.group({
    nome:          ['', [Validators.required]],
    email:         ['', [Validators.required, Validators.email]],
    role:          [this.rolesDisponiveis()[0] as string, [Validators.required]],
    cpfCnpj:       [''],
    telefone:      [''],
    senha:         ['', [Validators.required, Validators.pattern(SENHA_PATTERN)]],
    confirmarSenha:['', [Validators.required]],
  }, { validators: senhaIgualValidator });

  get f() { return this.form.controls; }

  readonly roleAtual = computed(() => this.f['role'].value as RoleEnum);

  ngOnInit(): void {
    this.f['role'].valueChanges.subscribe((role) => {
      const isCliente = role === 'CLIENTE';
      if (isCliente) {
        this.f['cpfCnpj'].setValidators([Validators.required, cpfCnpjValidator()]);
        this.f['telefone'].setValidators([Validators.required]);
      } else {
        this.f['cpfCnpj'].clearValidators();
        this.f['telefone'].clearValidators();
        this.f['cpfCnpj'].setValue('');
        this.f['telefone'].setValue('');
      }
      this.f['cpfCnpj'].updateValueAndValidity();
      this.f['telefone'].updateValueAndValidity();
    });
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

    const raw = this.form.getRawValue();
    const req: UsuarioRequest = {
      nome:  raw.nome!,
      email: raw.email!,
      role:  raw.role as RoleEnum,
      senha: raw.senha!,
      ...(raw.role === 'CLIENTE' && {
        cpfCnpj:  raw.cpfCnpj!.replace(/\D/g, ''),
        telefone: raw.telefone!,
      }),
    };

    this.service.cadastrar(req).subscribe({
      next: () => {
        this.snackBar.open('Usuário cadastrado com sucesso!', 'Fechar', { duration: 3000 });
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

    const campoParaControl: Record<string, string> = {
      nome: 'nome', email: 'email', cpfCnpj: 'cpfCnpj', telefone: 'telefone', senha: 'senha',
    };
    let temErroCampo = false;
    for (const [campo, msg] of Object.entries(body as Record<string, string>)) {
      const controlName = campoParaControl[campo];
      if (controlName) {
        this.form.get(controlName)?.setErrors({ backend: msg });
        temErroCampo = true;
      }
    }
    if (!temErroCampo) {
      this.erroBackend.set('Erro ao processar a requisição.');
    }
  }
}
