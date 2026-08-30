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
  CATEGORIA_PECA_INSUMO_LABEL,
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
  templateUrl: './peca-insumo-form-dialog.component.html',
  styleUrl: './peca-insumo-form-dialog.component.scss',
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

  tipoLabel(tipo: CategoriaPecaInsumo): string {
    return CATEGORIA_PECA_INSUMO_LABEL[tipo] ?? tipo;
  }

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
      this.erroBackend.set('Erro ao processar a requisição.');
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
