import { FormControl } from '@angular/forms';
import { normalizarPlaca, placaValidator } from './veiculo.model';

describe('veiculo.model', () => {
  describe('normalizarPlaca', () => {
    it('deve converter para maiusculas e remover caracteres especiais', () => {
      expect(normalizarPlaca('abc-1d23')).toBe('ABC1D23');
    });

    it('deve lidar com valor vazio ou nulo', () => {
      expect(normalizarPlaca('')).toBe('');
      expect(normalizarPlaca(null as unknown as string)).toBe('');
    });
  });

  describe('placaValidator', () => {
    const validator = placaValidator();

    it('deve retornar null quando o campo esta vazio', () => {
      expect(validator(new FormControl(''))).toBeNull();
    });

    it('deve validar placa no formato antigo (ABC1234)', () => {
      expect(validator(new FormControl('ABC1234'))).toBeNull();
    });

    it('deve validar placa no formato Mercosul (ABC1D23)', () => {
      expect(validator(new FormControl('ABC1D23'))).toBeNull();
    });

    it('deve aceitar placa com hifen e minusculas', () => {
      expect(validator(new FormControl('abc-1234'))).toBeNull();
    });

    it('deve invalidar formato incorreto', () => {
      expect(validator(new FormControl('AB123'))).toEqual({ placa: 'Use o formato ABC1234 ou ABC1D23' });
    });
  });
});
