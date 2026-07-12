import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { PecaInsumoFormDialogComponent, PecaInsumoFormDialogData } from './peca-insumo-form-dialog.component';
import { PecaInsumoService } from './peca-insumo.service';
import { PecaInsumoResponse } from './peca-insumo.model';

describe('PecaInsumoFormDialogComponent', () => {
  let mockService: jasmine.SpyObj<PecaInsumoService>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<PecaInsumoFormDialogComponent>>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;

  const itemExistente: PecaInsumoResponse = { id: 1, nome: 'Filtro', valor: 20, quantidade: 10, tipo: 'PECA' };

  function criarComponente(data: PecaInsumoFormDialogData): PecaInsumoFormDialogComponent {
    TestBed.configureTestingModule({
      providers: [
        { provide: PecaInsumoService, useValue: mockService },
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data },
        { provide: MatSnackBar, useValue: mockSnackBar },
      ],
    });

    return TestBed.runInInjectionContext(() => new PecaInsumoFormDialogComponent());
  }

  function preencherFormularioValido(component: PecaInsumoFormDialogComponent): void {
    component.form.setValue({ nome: 'Oleo 5W30', valor: 45, quantidade: 20, tipo: 'INSUMO' });
  }

  beforeEach(() => {
    mockService = jasmine.createSpyObj('PecaInsumoService', ['cadastrar', 'atualizar']);
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
  });

  describe('modo cadastro', () => {
    it('deve iniciar com formulario vazio e tipo padrao PECA', () => {
      const component = criarComponente({ item: null });

      expect(component.edicao).toBeFalse();
      expect(component.form.value).toEqual({ nome: '', valor: null, quantidade: 0, tipo: 'PECA' });
    });

    it('salvar nao deve chamar o servico quando o formulario e invalido', () => {
      const component = criarComponente({ item: null });
      component.form.patchValue({ quantidade: -1 });

      component.salvar();

      expect(mockService.cadastrar).not.toHaveBeenCalled();
      expect(component.form.touched).toBeTrue();
    });

    it('salvar deve chamar cadastrar com os dados do formulario, convertendo quantidade para numero', () => {
      mockService.cadastrar.and.returnValue(of(itemExistente));
      const component = criarComponente({ item: null });
      preencherFormularioValido(component);

      component.salvar();

      expect(mockService.cadastrar).toHaveBeenCalledWith({
        nome: 'Oleo 5W30', valor: 45, quantidade: 20, tipo: 'INSUMO',
      });
      expect(mockSnackBar.open).toHaveBeenCalledWith('Peca/Insumo cadastrado com sucesso.', 'Fechar', { duration: 3000 });
      expect(mockDialogRef.close).toHaveBeenCalledWith(true);
    });
  });

  describe('modo edicao', () => {
    it('deve preencher o formulario com os dados do item', () => {
      const component = criarComponente({ item: itemExistente });

      expect(component.edicao).toBeTrue();
      expect(component.form.value).toEqual({ nome: 'Filtro', valor: 20, quantidade: 10, tipo: 'PECA' });
    });

    it('salvar deve chamar atualizar com o id do item', () => {
      mockService.atualizar.and.returnValue(of(itemExistente));
      const component = criarComponente({ item: itemExistente });
      component.form.patchValue({ quantidade: 15 });

      component.salvar();

      expect(mockService.atualizar).toHaveBeenCalledWith(1, {
        nome: 'Filtro', valor: 20, quantidade: 15, tipo: 'PECA',
      });
      expect(mockSnackBar.open).toHaveBeenCalledWith('Peca/Insumo atualizado com sucesso.', 'Fechar', { duration: 3000 });
      expect(mockDialogRef.close).toHaveBeenCalledWith(true);
    });
  });

  describe('cancelar', () => {
    it('deve fechar o dialog com false', () => {
      const component = criarComponente({ item: null });

      component.cancelar();

      expect(mockDialogRef.close).toHaveBeenCalledWith(false);
    });
  });

  describe('tratamento de erro do backend', () => {
    it('deve exibir mensagem padrao quando nao ha corpo no erro', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({})));
      const component = criarComponente({ item: null });
      preencherFormularioValido(component);

      component.salvar();

      expect(component.erroBackend()).toBe('Erro inesperado. Tente novamente.');
      expect(component.loading()).toBeFalse();
    });

    it('deve exibir a mensagem quando o corpo do erro e uma string', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({ error: 'Falha no servidor' })));
      const component = criarComponente({ item: null });
      preencherFormularioValido(component);

      component.salvar();

      expect(component.erroBackend()).toBe('Falha no servidor');
    });

    it('deve exibir a mensagem de erro de negocio quando o corpo possui "erro"', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({ error: { erro: 'Item ja cadastrado' } })));
      const component = criarComponente({ item: null });
      preencherFormularioValido(component);

      component.salvar();

      expect(component.erroBackend()).toBe('Item ja cadastrado');
    });

    it('deve mapear erros de validacao por campo do backend', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({ error: { quantidade: 'Quantidade invalida' } })));
      const component = criarComponente({ item: null });
      preencherFormularioValido(component);

      component.salvar();

      expect(component.form.get('quantidade')?.errors).toEqual({ backend: 'Quantidade invalida' });
      expect(component.erroBackend()).toBeNull();
    });

    it('deve exibir mensagem padrao quando o corpo nao mapeia nenhum campo conhecido', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({ error: { campoDesconhecido: 'x' } })));
      const component = criarComponente({ item: null });
      preencherFormularioValido(component);

      component.salvar();

      expect(component.erroBackend()).toBe('Erro ao processar a requisição.');
    });

    it('deve limpar o erro de backend do campo apos o usuario corrigir o valor e reenviar', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({ error: { quantidade: 'Quantidade invalida' } })));
      const component = criarComponente({ item: null });
      preencherFormularioValido(component);
      component.salvar();
      expect(component.form.get('quantidade')?.hasError('backend')).toBeTrue();
      expect(component.form.invalid).toBeTrue();

      component.form.get('quantidade')?.setValue(30);
      expect(component.form.get('quantidade')?.hasError('backend')).toBeFalse();

      mockService.cadastrar.and.returnValue(of(itemExistente));
      component.salvar();

      expect(mockService.cadastrar).toHaveBeenCalledWith({
        nome: 'Oleo 5W30', valor: 45, quantidade: 30, tipo: 'INSUMO',
      });
      expect(component.form.get('quantidade')?.hasError('backend')).toBeFalse();
    });
  });
});
