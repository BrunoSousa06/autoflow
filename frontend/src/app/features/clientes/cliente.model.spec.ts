import { FormControl } from '@angular/forms';
import { cpfCnpjValidator, formatarCpfCnpj, formatarTelefone } from './cliente.model';

describe('cliente.model', () => {
  describe('formatarCpfCnpj', () => {
    it('deve formatar um CPF completo', () => {
      expect(formatarCpfCnpj('12345678900')).toBe('123.456.789-00');
    });

    it('deve formatar um CNPJ completo', () => {
      expect(formatarCpfCnpj('12345678000199')).toBe('12.345.678/0001-99');
    });

    it('deve ignorar caracteres nao numericos', () => {
      expect(formatarCpfCnpj('abc123.456.789-00xyz')).toBe('123.456.789-00');
    });

    it('deve lidar com valor vazio ou nulo', () => {
      expect(formatarCpfCnpj('')).toBe('');
      expect(formatarCpfCnpj(null as unknown as string)).toBe('');
    });
  });

  describe('formatarTelefone', () => {
    it('deve formatar telefone fixo (10 digitos)', () => {
      expect(formatarTelefone('1133334444')).toBe('(11) 3333-4444');
    });

    it('deve formatar celular (11 digitos)', () => {
      expect(formatarTelefone('11999998888')).toBe('(11) 99999-8888');
    });

    it('deve retornar vazio quando nao ha digitos', () => {
      expect(formatarTelefone('')).toBe('');
    });

    it('deve limitar a 11 digitos', () => {
      expect(formatarTelefone('119999988887777')).toBe('(11) 99999-8888');
    });
  });

  describe('cpfCnpjValidator', () => {
    const validator = cpfCnpjValidator();

    it('deve retornar null quando o campo esta vazio', () => {
      expect(validator(new FormControl(''))).toBeNull();
    });

    it('deve validar um CPF valido', () => {
      expect(validator(new FormControl('529.982.247-25'))).toBeNull();
    });

    it('deve invalidar um CPF com digito verificador incorreto', () => {
      expect(validator(new FormControl('529.982.247-24'))).toEqual({ cpfCnpj: 'CPF inválido' });
    });

    it('deve invalidar CPF com todos os digitos iguais', () => {
      expect(validator(new FormControl('111.111.111-11'))).toEqual({ cpfCnpj: 'CPF inválido' });
    });

    it('deve validar um CNPJ valido', () => {
      expect(validator(new FormControl('11.222.333/0001-81'))).toBeNull();
    });

    it('deve invalidar um CNPJ com digito verificador incorreto', () => {
      expect(validator(new FormControl('11.222.333/0001-80'))).toEqual({ cpfCnpj: 'CNPJ inválido' });
    });

    it('deve invalidar quantidade de digitos diferente de 11 ou 14', () => {
      expect(validator(new FormControl('123456'))).toEqual({ cpfCnpj: 'CPF deve ter 11 dígitos ou CNPJ 14 dígitos' });
    });
  });
});
