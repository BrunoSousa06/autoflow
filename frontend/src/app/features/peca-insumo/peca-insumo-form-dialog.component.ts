import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSelectModule } from '@angular/material/select';
import { PecaInsumoService } from './peca-insumo.service';
import {
  CATEGORIAS_PECA_INSUMO,
  CategoriaPecaInsumo,
  PecaInsumoRequest,
  PecaInsumoResponse,
} from './peca-insumo.model';

export interface PecaInsumoFormDialogData {
  item: PecaInsumoResponse | null;
}

@Component({
  selector: 'app-peca-insumo-form-dialog',
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
    MatSelectModule,
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon>{{ edicao ? 'edit' : 'inventory_2' }}</mat-icon>
      {{ edicao ? 'Editar Peca/Insumo' : 'Nova Peca/Insumo' }}
    </h2>

    <mat-dialog-content>
      <form [formGroup]="form" novalidate>
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Nome</mat-label>
          <mat-icon matPrefix>label</mat-icon>
          <input matInput formControlName="nome" autocomplete="off" placeholder="Ex: Filtro de oleo" />
          @if (f.nome.touched && f.nome.hasError('required')) {
            <mat-error>Nome e obrigatorio</mat-error>
          } @else if (f.nome.hasError('backend')) {
            <mat-error>{{ f.nome.getError('backend') }}</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Tipo</mat-label>
          <mat-icon matPrefix>category</mat-icon>
          <mat-select formControlName="tipo">
            @for (tipo of categorias; track tipo) {
              <mat-option [value]="tipo">{{ tipo }}</mat-option>
            }
          </mat-select>
          @if (f.tipo.touched && f.tipo.hasError('required')) {
            <mat-error>Tipo e obrigatorio</mat-error>
          } @else if (f.tipo.hasError('backend')) {
            <mat-error>{{ f.tipo.getError('backend') }}</mat-error>
          }
        </mat-form-field>

        <div class="form-grid">
          <mat-form-field appearance="outline">
            <mat-label>Valor (R$)</mat-label>
            <mat-icon matPrefix>attach_money</mat-icon>
            <input matInput formControlName="valor" type="number" min="0" step="0.01" />
            @if (f.valor.hasError('min')) {
              <mat-error>Valor nao pode ser negativo</mat-error>
            } @else if (f.valor.hasError('backend')) {
              <mat-error>{{ f.valor.getError('backend') }}</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Quantidade</mat-label>
            <mat-icon matPrefix>numbers</mat-icon>
            <input matInput formControlName="quantidade" type="number" min="0" step="1" />
            @if (f.quantidade.hasError('required')) {
              <mat-error>Quantidade e obrigatoria</mat-error>
            } @else if (f.quantidade.hasError('min')) {
              <mat-error>Quantidade nao pode ser negativa</mat-error>
            } @else if (f.quantidade.hasError('pattern')) {
              <mat-error>Informe um numero inteiro</mat-error>
            } @else if (f.quantidade.hasError('backend')) {
              <mat-error>{{ f.quantidade.getError('backend') }}</mat-error>
            }
          </mat-form-field>
        </div>

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
        {{ edicao ? 'Salvar alteracoes' : 'Cadastrar' }}
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
      min-width: 520px;
      padding-top: 8px !important;
    }

    .full-width {
      width: 100%;
      margin-bottom: 4px;
    }

    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
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

    @media (max-width: 640px) {
      mat-dialog-content { min-width: 0; }
      .form-grid { grid-template-columns: 1fr; }
    }
  `],
})
export class PecaInsumoFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(PecaInsumoService);
  private readonly dialogRef = inject(MatDialogRef<PecaInsumoFormDialogComponent>);
  private readonly data = inject<PecaInsumoFormDialogData>(MAT_DIALOG_DATA);
  private readonly snackBar = inject(MatSnackBar);

  readonly categorias = CATEGORIAS_PECA_INSUMO;
  readonly edicao = this.data.item !== null;
  readonly loading = signal(false);
  readonly erroBackend = signal<string | null>(null);

  readonly form = this.fb.group({
    nome: ['', [Validators.required]],
    valor: [null as number | null, [Validators.min(0)]],
    quantidade: [0, [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)]],
    tipo: ['PECA' as CategoriaPecaInsumo, [Validators.required]],
  });

  get f() { return this.form.controls; }

  constructor() {
    if (this.data.item) {
      const item = this.data.item;
      this.form.patchValue({
        nome: item.nome,
        valor: item.valor,
        quantidade: item.quantidade,
        tipo: item.tipo,
      });
    }
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.erroBackend.set(null);
    this.limparErrosBackend();

    const raw = this.form.getRawValue();
    const req: PecaInsumoRequest = {
      nome: raw.nome!,
      valor: raw.valor ?? null,
      quantidade: Number(raw.quantidade ?? 0),
      tipo: raw.tipo!,
    };

    const obs = this.edicao
      ? this.service.atualizar(this.data.item!.id, req)
      : this.service.cadastrar(req);

    obs.subscribe({
      next: () => {
        this.snackBar.open(
          this.edicao ? 'Peca/Insumo atualizado com sucesso.' : 'Peca/Insumo cadastrado com sucesso.',
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
    if (!body) {
      this.erroBackend.set('Erro inesperado. Tente novamente.');
      return;
    }
    if (typeof body === 'string') {
      this.erroBackend.set(body);
      return;
    }

    if (body['erro']) {
      this.erroBackend.set(body['erro']);
      return;
    }

    const campoMap: Record<string, keyof typeof this.form.controls> = {
      nome: 'nome',
      valor: 'valor',
      quantidade: 'quantidade',
      tipo: 'tipo',
    };

    let temErroCampo = false;
    for (const [campo, msg] of Object.entries(body as Record<string, string>)) {
      const ctrl = campoMap[campo];
      if (ctrl) {
        this.form.controls[ctrl].setErrors({ backend: msg });
        temErroCampo = true;
      }
    }
    if (!temErroCampo) {
      this.erroBackend.set('Erro ao processar a requisicao.');
    }
  }

  private limparErrosBackend(): void {
    Object.values(this.form.controls).forEach((control) => {
      if (!control.errors?.['backend']) return;
      const { backend, ...rest } = control.errors;
      control.setErrors(Object.keys(rest).length ? rest : null);
    });
  }
}
