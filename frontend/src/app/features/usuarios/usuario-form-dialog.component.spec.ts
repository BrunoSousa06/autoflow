import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { UsuarioFormDialogComponent } from './usuario-form-dialog.component';
import { UsuarioAdminService } from './usuario.service';
import { AuthService } from '../../core/services/auth.service';
import { UsuarioResponse } from './usuario.model';

describe('UsuarioFormDialogComponent', () => {
  let mockService: jasmine.SpyObj<UsuarioAdminService>;
  let mockAuth: jasmine.SpyObj<AuthService>;
  let mockDialogRef: jasmine.SpyObj<MatDialogRef<UsuarioFormDialogComponent>>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;

  const usuarioCriado: UsuarioResponse = { id: 1, nome: 'Joao', email: 'joao@teste.com', role: 'ATENDENTE' };

  function criarComponente(role: string | null): UsuarioFormDialogComponent {
    mockAuth.getRole.and.returnValue(role);
    TestBed.configureTestingModule({
      providers: [
        { provide: UsuarioAdminService, useValue: mockService },
        { provide: AuthService, useValue: mockAuth },
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: MatSnackBar, useValue: mockSnackBar },
      ],
    });

    return TestBed.runInInjectionContext(() => new UsuarioFormDialogComponent());
  }

  beforeEach(() => {
    mockService = jasmine.createSpyObj('UsuarioAdminService', ['cadastrar']);
    mockAuth = jasmine.createSpyObj('AuthService', ['getRole']);
    mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
  });

  describe('rolesDisponiveis', () => {
    it('deve incluir todas as roles quando o usuario logado e ADMIN', () => {
      const component = criarComponente('ADMIN');

      expect(component.rolesDisponiveis()).toEqual(['ADMIN', 'ATENDENTE', 'MECANICO', 'CLIENTE']);
    });

    it('deve restringir para ATENDENTE/CLIENTE quando o usuario logado nao e ADMIN', () => {
      const component = criarComponente('ATENDENTE');

      expect(component.rolesDisponiveis()).toEqual(['ATENDENTE', 'CLIENTE']);
    });
  });

  describe('ngOnInit - validadores dinamicos por role', () => {
    it('deve tornar cpfCnpj e telefone obrigatorios quando a role muda para CLIENTE', () => {
      const component = criarComponente('ADMIN');
      component.ngOnInit();

      component.f['role'].setValue('CLIENTE');

      expect(component.f['cpfCnpj'].hasError('required')).toBeTrue();
      expect(component.f['telefone'].hasError('required')).toBeTrue();
    });

    it('deve limpar cpfCnpj e telefone quando a role muda para um valor diferente de CLIENTE', () => {
      const component = criarComponente('ADMIN');
      component.ngOnInit();
      component.f['role'].setValue('CLIENTE');
      component.f['cpfCnpj'].setValue('12345678900');
      component.f['telefone'].setValue('11999998888');

      component.f['role'].setValue('ATENDENTE');

      expect(component.f['cpfCnpj'].value).toBe('');
      expect(component.f['telefone'].value).toBe('');
      expect(component.f['cpfCnpj'].hasError('required')).toBeFalse();
    });
  });

  describe('mascaras', () => {
    it('mascaraCpfCnpj deve formatar o valor do input e do control', () => {
      const component = criarComponente('ADMIN');
      const input = document.createElement('input');
      input.value = '52998224725';
      const event = { target: input } as unknown as Event;

      component.mascaraCpfCnpj(event);

      expect(input.value).toBe('529.982.247-25');
      expect(component.f['cpfCnpj'].value).toBe('529.982.247-25');
    });

    it('mascaraTelefone deve formatar o valor do input e do control', () => {
      const component = criarComponente('ADMIN');
      const input = document.createElement('input');
      input.value = '11999998888';
      const event = { target: input } as unknown as Event;

      component.mascaraTelefone(event);

      expect(input.value).toBe('(11) 99999-8888');
      expect(component.f['telefone'].value).toBe('(11) 99999-8888');
    });
  });

  describe('salvar', () => {
    it('nao deve chamar o servico quando o formulario e invalido', () => {
      const component = criarComponente('ADMIN');

      component.salvar();

      expect(mockService.cadastrar).not.toHaveBeenCalled();
      expect(component.form.touched).toBeTrue();
    });

    it('deve marcar erro de senhas diferentes e nao chamar o servico', () => {
      const component = criarComponente('ADMIN');
      component.form.patchValue({
        nome: 'Joao', email: 'joao@teste.com', role: 'ATENDENTE',
        senha: 'Senha123!', confirmarSenha: 'Outra123!',
      });

      component.salvar();

      expect(component.f['confirmarSenha'].hasError('senhasDiferentes')).toBeTrue();
      expect(mockService.cadastrar).not.toHaveBeenCalled();
    });

    it('deve cadastrar sem cpfCnpj/telefone quando a role nao e CLIENTE', () => {
      mockService.cadastrar.and.returnValue(of(usuarioCriado));
      const component = criarComponente('ADMIN');
      component.form.patchValue({
        nome: 'Joao', email: 'joao@teste.com', role: 'ATENDENTE',
        senha: 'Senha123!', confirmarSenha: 'Senha123!',
      });

      component.salvar();

      expect(mockService.cadastrar).toHaveBeenCalledWith({
        nome: 'Joao', email: 'joao@teste.com', role: 'ATENDENTE', senha: 'Senha123!',
      });
      expect(mockSnackBar.open).toHaveBeenCalledWith('Usuário cadastrado com sucesso!', 'Fechar', { duration: 3000 });
      expect(mockDialogRef.close).toHaveBeenCalledWith(true);
    });

    it('deve cadastrar com cpfCnpj (apenas digitos) e telefone quando a role e CLIENTE', () => {
      mockService.cadastrar.and.returnValue(of(usuarioCriado));
      const component = criarComponente('ADMIN');
      component.ngOnInit();
      component.f['role'].setValue('CLIENTE');
      component.form.patchValue({
        nome: 'Cliente Novo', email: 'cliente@teste.com',
        cpfCnpj: '529.982.247-25', telefone: '(11) 99999-8888',
        senha: 'Senha123!', confirmarSenha: 'Senha123!',
      });

      component.salvar();

      expect(mockService.cadastrar).toHaveBeenCalledWith({
        nome: 'Cliente Novo', email: 'cliente@teste.com', role: 'CLIENTE', senha: 'Senha123!',
        cpfCnpj: '52998224725', telefone: '(11) 99999-8888',
      });
    });
  });

  describe('cancelar', () => {
    it('deve fechar o dialog com false', () => {
      const component = criarComponente('ADMIN');

      component.cancelar();

      expect(mockDialogRef.close).toHaveBeenCalledWith(false);
    });
  });

  describe('tratamento de erro do backend', () => {
    function formularioValido(component: UsuarioFormDialogComponent): void {
      component.form.patchValue({
        nome: 'Joao', email: 'joao@teste.com', role: 'ATENDENTE',
        senha: 'Senha123!', confirmarSenha: 'Senha123!',
      });
    }

    it('deve exibir mensagem padrao quando nao ha corpo no erro', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({})));
      const component = criarComponente('ADMIN');
      formularioValido(component);

      component.salvar();

      expect(component.erroBackend()).toBe('Erro inesperado. Tente novamente.');
      expect(component.loading()).toBeFalse();
    });

    it('deve exibir a mensagem quando o corpo do erro e uma string', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({ error: 'Falha no servidor' })));
      const component = criarComponente('ADMIN');
      formularioValido(component);

      component.salvar();

      expect(component.erroBackend()).toBe('Falha no servidor');
    });

    it('deve exibir a mensagem de erro de negocio quando o corpo possui "erro"', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({ error: { erro: 'E-mail ja cadastrado' } })));
      const component = criarComponente('ADMIN');
      formularioValido(component);

      component.salvar();

      expect(component.erroBackend()).toBe('E-mail ja cadastrado');
    });

    it('deve mapear erros de validacao por campo do backend', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({ error: { email: 'E-mail invalido' } })));
      const component = criarComponente('ADMIN');
      formularioValido(component);

      component.salvar();

      expect(component.form.get('email')?.errors).toEqual({ backend: 'E-mail invalido' });
      expect(component.erroBackend()).toBeNull();
    });

    it('deve exibir mensagem padrao quando o corpo nao mapeia nenhum campo conhecido', () => {
      mockService.cadastrar.and.returnValue(throwError(() => ({ error: { campoDesconhecido: 'x' } })));
      const component = criarComponente('ADMIN');
      formularioValido(component);

      component.salvar();

      expect(component.erroBackend()).toBe('Erro ao processar a requisição.');
    });
  });
});
