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
import { ServicoService } from './servico.service';
import { ServicoResponse, ServicoRequest } from './servico.model';

export interface ServicoFormDialogData {
  servico: ServicoResponse | null;
}

@Component({
  selector: 'app-servico-form-dialog',
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
      <mat-icon>{{ edicao ? 'edit' : 'build' }}</mat-icon>
      {{ edicao ? 'Editar Serviço' : 'Novo Serviço' }}
    </h2>

    <mat-dialog-content>
      <form [formGroup]="form" novalidate>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Nome</mat-label>
          <mat-icon matPrefix>label</mat-icon>
          <input matInput formControlName="nome" autocomplete="off" placeholder="Ex: Troca de óleo" />
          @if (f['nome'].touched && f['nome'].hasError('required')) {
            <mat-error>Nome é obrigatório</mat-error>
          } @else if (f['nome'].hasError('backend')) {
            <mat-error>{{ f['nome'].getError('backend') }}</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Descrição</mat-label>
          <mat-icon matPrefix>description</mat-icon>
          <textarea
            matInput
            formControlName="descricao"
            rows="3"
            placeholder="Descreva o serviço prestado"
          ></textarea>
          @if (f['descricao'].touched && f['descricao'].hasError('required')) {
            <mat-error>Descrição é obrigatória</mat-error>
          } @else if (f['descricao'].hasError('backend')) {
            <mat-error>{{ f['descricao'].getError('backend') }}</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Valor (R$)</mat-label>
          <mat-icon matPrefix>attach_money</mat-icon>
          <input
            matInput
            formControlName="valor"
            type="number"
            min="0"
            step="0.01"
            placeholder="Ex: 150.00"
          />
          <mat-hint>Opcional</mat-hint>
          @if (f['valor'].hasError('min')) {
            <mat-error>Valor não pode ser negativo</mat-error>
          } @else if (f['valor'].hasError('backend')) {
            <mat-error>{{ f['valor'].getError('backend') }}</mat-error>
          }
        </mat-form-field>

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
      <button mat-raised-button color="primary" (click)="salvar()" [disabled]="loading()">
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
      min-width: 480px;
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

    mat-spinner { display: inline-block; }
  `],
})
export class ServicoFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly servicoService = inject(ServicoService);
  private readonly dialogRef = inject(MatDialogRef<ServicoFormDialogComponent>);
  private readonly data = inject<ServicoFormDialogData>(MAT_DIALOG_DATA);
  private readonly snackBar = inject(MatSnackBar);

  readonly edicao = this.data.servico !== null;
  readonly loading = signal(false);
  readonly erroBackend = signal<string | null>(null);

  readonly form = this.fb.group({
    nome:     ['', [Validators.required]],
    descricao:['', [Validators.required]],
    valor:    [null as number | null, [Validators.min(0)]],
  });

  get f() { return this.form.controls; }

  constructor() {
    if (this.data.servico) {
      const s = this.data.servico;
      this.form.patchValue({ nome: s.nome, descricao: s.descricao, valor: s.valor });
    }
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.erroBackend.set(null);

    const raw = this.form.getRawValue();
    const req: ServicoRequest = {
      nome:      raw.nome!,
      descricao: raw.descricao!,
      valor:     raw.valor ?? null,
    };

    const obs = this.edicao
      ? this.servicoService.atualizar(this.data.servico!.id, req)
      : this.servicoService.cadastrar(req);

    obs.subscribe({
      next: () => {
        this.snackBar.open(
          this.edicao ? 'Serviço atualizado com sucesso!' : 'Serviço cadastrado com sucesso!',
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

    const campoMap: Record<string, string> = {
      nome: 'nome',
      descricao: 'descricao',
      valor: 'valor',
    };
    let temErroCampo = false;
    for (const [campo, msg] of Object.entries(body as Record<string, string>)) {
      const ctrl = campoMap[campo];
      if (ctrl) {
        this.form.get(ctrl)?.setErrors({ backend: msg });
        temErroCampo = true;
      }
    }
    if (!temErroCampo) {
      this.erroBackend.set('Erro ao processar a requisição.');
    }
  }
}
