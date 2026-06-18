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
  template: `
    <h2 mat-dialog-title>
      <mat-icon>person_add</mat-icon>
      Novo Usuário
    </h2>

    <mat-dialog-content>
      <form [formGroup]="form" novalidate>

        <!-- Nome -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Nome completo</mat-label>
          <mat-icon matPrefix>person</mat-icon>
          <input matInput formControlName="nome" autocomplete="off" />
          @if (f['nome'].hasError('required') && f['nome'].touched) {
            <mat-error>Nome é obrigatório</mat-error>
          } @else if (f['nome'].hasError('backend')) {
            <mat-error>{{ f['nome'].getError('backend') }}</mat-error>
          }
        </mat-form-field>

        <!-- E-mail -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>E-mail</mat-label>
          <mat-icon matPrefix>email</mat-icon>
          <input matInput formControlName="email" type="email" autocomplete="off" />
          @if (f['email'].hasError('required') && f['email'].touched) {
            <mat-error>E-mail é obrigatório</mat-error>
          } @else if (f['email'].hasError('email') && f['email'].touched) {
            <mat-error>Formato de e-mail inválido</mat-error>
          } @else if (f['email'].hasError('backend')) {
            <mat-error>{{ f['email'].getError('backend') }}</mat-error>
          }
        </mat-form-field>

        <!-- Função -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Função</mat-label>
          <mat-icon matPrefix>badge</mat-icon>
          <mat-select formControlName="role">
            @for (role of rolesDisponiveis(); track role) {
              <mat-option [value]="role">{{ roleLabels[role] }}</mat-option>
            }
          </mat-select>
          @if (f['role'].hasError('required') && f['role'].touched) {
            <mat-error>Função é obrigatória</mat-error>
          }
        </mat-form-field>

        <!-- CPF/CNPJ — apenas para CLIENTE -->
        @if (roleAtual() === 'CLIENTE') {
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>CPF / CNPJ</mat-label>
            <mat-icon matPrefix>credit_card</mat-icon>
            <input
              matInput
              formControlName="cpfCnpj"
              placeholder="000.000.000-00 ou 00.000.000/0000-00"
              autocomplete="off"
              (input)="mascaraCpfCnpj($event)"
            />
            @if (f['cpfCnpj'].hasError('required') && f['cpfCnpj'].touched) {
              <mat-error>CPF/CNPJ é obrigatório</mat-error>
            } @else if (f['cpfCnpj'].hasError('cpfCnpj')) {
              <mat-error>{{ f['cpfCnpj'].getError('cpfCnpj') }}</mat-error>
            } @else if (f['cpfCnpj'].hasError('backend')) {
              <mat-error>{{ f['cpfCnpj'].getError('backend') }}</mat-error>
            }
          </mat-form-field>

          <!-- Telefone — apenas para CLIENTE -->
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
            @if (f['telefone'].hasError('required') && f['telefone'].touched) {
              <mat-error>Telefone é obrigatório</mat-error>
            } @else if (f['telefone'].hasError('backend')) {
              <mat-error>{{ f['telefone'].getError('backend') }}</mat-error>
            }
          </mat-form-field>
        }

        <!-- Senha -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Senha</mat-label>
          <mat-icon matPrefix>lock</mat-icon>
          <input matInput formControlName="senha" [type]="mostrarSenha ? 'text' : 'password'" autocomplete="new-password" />
          <button mat-icon-button matSuffix type="button" (click)="mostrarSenha = !mostrarSenha">
            <mat-icon>{{ mostrarSenha ? 'visibility_off' : 'visibility' }}</mat-icon>
          </button>
          @if (f['senha'].hasError('required') && f['senha'].touched) {
            <mat-error>Senha é obrigatória</mat-error>
          } @else if (f['senha'].hasError('pattern') && f['senha'].touched) {
            <mat-error>Mín. 8 caracteres com maiúscula, minúscula, número e caractere especial</mat-error>
          }
        </mat-form-field>

        <!-- Confirmar senha -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Confirmar senha</mat-label>
          <mat-icon matPrefix>lock_reset</mat-icon>
          <input matInput formControlName="confirmarSenha" [type]="mostrarSenha ? 'text' : 'password'" autocomplete="new-password" />
          @if (f['confirmarSenha'].hasError('required') && f['confirmarSenha'].touched) {
            <mat-error>Confirmação de senha é obrigatória</mat-error>
          } @else if (f['confirmarSenha'].hasError('senhasDiferentes')) {
            <mat-error>As senhas não coincidem</mat-error>
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
      <button mat-button (click)="cancelar()" [disabled]="loading()">Cancelar</button>
      <button
        mat-raised-button
        color="primary"
        (click)="salvar()"
        [disabled]="loading()"
      >
        @if (loading()) {
          <mat-spinner diameter="18" />
        } @else {
          <mat-icon>person_add</mat-icon>
        }
        Cadastrar
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
