import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { ServicoFormDialogComponent, ServicoFormDialogData } from './servico-form-dialog.component';
import { ServicoService } from './servico.service';
import { ServicoResponse } from './servico.model';

describe('ServicoFormDialogComponent', () => {
  let mockService: jasmine.SpyObj<ServicoService>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<ServicoFormDialogComponent>>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;

  const servicoExistente: ServicoResponse = { id: 1, nome: 'Troca de oleo', descricao: 'Troca de oleo do motor', valor: 100, ativo: true };

  function criarComponente(data: ServicoFormDialogData): ServicoFormDialogComponent {
    TestBed.configureTestingModule({
      providers: [
        { provide: ServicoService, useValue: mockService },
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data },
        { provide: MatSnackBar, useValue: mockSnackBar },
      ],
    });

    return TestBed.runInInjectionContext(() => new ServicoFormDialogComponent());
  }

  function preencherFormularioValido(component: ServicoFormDialogComponent): void {
    component.form.setValue({ nome: 'Alinhamento', descricao: 'Alinhamento e balanceamento', valor: 150 });
  }

  beforeEach(() => {
    mockService = jasmine.createSpyObj('ServicoService', ['cadastrar', 'atualizar']);
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
  });

  describe('modo cadastro', () => {
    it('deve iniciar com formulario vazio', () => {
      const component = criarComponente({ servico: null });

      expect(component.edicao).toBeFalse();
      expect(component.form.value).toEqual({ nome: '', descricao: '', valor: null });
    });

    it('salvar nao deve chamar o servico quando o formulario e invalido', () => {
      const component = criarComponente({ servico: null });

      component.salvar();

      expect(mockService.cadastrar).not.toHaveBeenCalled();
      expect(component.form.touched).toBeTrue();
    });

    it('salvar deve chamar cadastrar com os dados do formulario', () => {
      mockService.cadastrar.and.returnValue(of(servicoExistente));
      const component = criarComponente({ servico: null });
      preencherFormularioValido(component);

      component.salvar();

      expect(mockService.cadastrar).toHaveBeenCalledWith({
        nome: 'Alinhamento', descricao: 'Alinhamento e balanceamento', valor: 150,
      });
      expect(mockSnackBar.open).toHaveBeenCalledWith('Serviço cadastrado com sucesso!', 'Fechar', { duration: 3000 });
      expect(mockDialogRef.close).toHaveBeenCalledWith(true);
    });
  });

  describe('modo edicao', () => {
    it('deve preencher o formulario com os dados do servico', () => {
      const component = criarComponente({ servico: servicoExistente });

      expect(component.edicao).toBeTrue();
      expect(component.form.value).toEqual({ nome: 'Troca de oleo', descricao: 'Troca de oleo do motor', valor: 100 });
    });

    it('salvar deve chamar atualizar com o id do servico', () => {
      mockService.atualizar.and.returnValue(of(servicoExistente));
      const component = criarComponente({ servico: servicoExistente });
      component.form.patchValue({ valor: 120 });

      component.salvar();

      expect(mockService.atualizar).toHaveBeenCalledWith(1, {
        nome: 'Troca de oleo', descricao: 'Troca de oleo do motor', valor: 120,
      });
      expect(mockSnackBar.open).toHaveBeenCalledWith('Serviço atualizado com sucesso!', 'Fechar', { duration: 3000 });
      expect(mockDialogRef.close).toHaveBeenCalledWith(true);
    });
  });

  describe('cancelar', () => {
    it('deve fechar o dialog com false', () => {
      const component = criarComponente({ servico: null });

      component.cancelar();

      expect(mockDialogRef.close).toHaveBeenCalledWith(false);
    });
  });

  describe('tratamento de erro do backend', () => {
    it('deve exibir mensagem padrao quando nao ha corpo no erro', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({})));
      const component = criarComponente({ servico: null });
      preencherFormularioValido(component);

      component.salvar();

      expect(component.erroBackend()).toBe('Erro inesperado. Tente novamente.');
      expect(component.loading()).toBeFalse();
    });

    it('deve exibir a mensagem quando o corpo do erro e uma string', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({ error: 'Falha no servidor' })));
      const component = criarComponente({ servico: null });
      preencherFormularioValido(component);

      component.salvar();

      expect(component.erroBackend()).toBe('Falha no servidor');
    });

    it('deve exibir a mensagem de erro de negocio quando o corpo possui "erro"', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({ error: { erro: 'Servico ja cadastrado' } })));
      const component = criarComponente({ servico: null });
      preencherFormularioValido(component);

      component.salvar();

      expect(component.erroBackend()).toBe('Servico ja cadastrado');
    });

    it('deve mapear erros de validacao por campo do backend', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({ error: { valor: 'Valor invalido' } })));
      const component = criarComponente({ servico: null });
      preencherFormularioValido(component);

      component.salvar();

      expect(component.form.get('valor')?.errors).toEqual({ backend: 'Valor invalido' });
      expect(component.erroBackend()).toBeNull();
    });

    it('deve exibir mensagem padrao quando o corpo nao mapeia nenhum campo conhecido', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({ error: { campoDesconhecido: 'x' } })));
      const component = criarComponente({ servico: null });
      preencherFormularioValido(component);

      component.salvar();

      expect(component.erroBackend()).toBe('Erro ao processar a requisição.');
    });
  });
});
