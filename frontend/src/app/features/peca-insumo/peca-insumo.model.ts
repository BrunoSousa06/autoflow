export type CategoriaPecaInsumo = 'PECA' | 'INSUMO';

export const CATEGORIAS_PECA_INSUMO: CategoriaPecaInsumo[] = ['PECA', 'INSUMO'];

export const CATEGORIA_PECA_INSUMO_LABEL: Record<CategoriaPecaInsumo, string> = {
  PECA: 'Peça',
  INSUMO: 'Insumo',
};

export interface PecaInsumoRequest {
  nome: string;
  valor: number | null;
  quantidade: number;
  tipo: CategoriaPecaInsumo;
}

export interface PecaInsumoResponse {
  id: number;
  nome: string;
  valor: number | null;
  quantidade: number;
  tipo: CategoriaPecaInsumo;
}

export interface Page<T> {
  content: T[];
  page: {
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  };
}
