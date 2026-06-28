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
  templateUrl: './servico-form-dialog.component.html',
  styleUrl: './servico-form-dialog.component.scss',
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
