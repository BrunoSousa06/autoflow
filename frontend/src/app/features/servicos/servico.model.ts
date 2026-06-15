export interface ServicoRequest {
  nome: string;
  descricao: string;
  valor?: number | null;
}

export interface ServicoResponse {
  id: number;
  nome: string;
  descricao: string;
  valor: number | null;
}

export interface TempoMedioServicoResponse {
  servicoId: number;
  nomeServico: string;
  quantidadeExecucoes: number;
  tempoMedioSegundos: number | null;
  tempoMedioMinutos: number | null;
  tempoMedioHoras: number | null;
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
