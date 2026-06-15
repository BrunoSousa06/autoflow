export type StatusOrdemServico =
  | 'RECEBIDA'
  | 'EM_DIAGNOSTICO'
  | 'AGUARDANDO_APROVACAO'
  | 'EM_EXECUCAO'
  | 'FINALIZADA'
  | 'ENTREGUE';

export type StatusServicoOs = 'AGUARDANDO' | 'EM_EXECUCAO' | 'FINALIZADO' | 'CANCELADO';

export const STATUS_OS_LABEL: Record<StatusOrdemServico, string> = {
  RECEBIDA: 'Recebida',
  EM_DIAGNOSTICO: 'Em Diagnóstico',
  AGUARDANDO_APROVACAO: 'Aguardando Aprovação',
  EM_EXECUCAO: 'Em Execução',
  FINALIZADA: 'Finalizada',
  ENTREGUE: 'Entregue',
};

export interface VeiculoOrdemServicoRequest {
  placa: string;
  marca?: string | null;
  modelo?: string | null;
  ano?: number | null;
}

export interface ServicoSolicitadoRequest {
  servicoId: number;
}

export interface CriarOrdemServicoRequest {
  cpfCnpj: string;
  veiculo: VeiculoOrdemServicoRequest;
  servicosSolicitados: ServicoSolicitadoRequest[];
}

export interface ServicoOsResponse {
  id: number;
  servicoId: number;
  nome: string;
  valor: number;
  status: StatusServicoOs;
  iniciadoEm: string | null;
  finalizadoEm: string | null;
}

export interface OrdemServicoResponse {
  id: number;
  numeroOs: string;
  clienteNome: string;
  clienteCpfCnpj: string;
  status: StatusOrdemServico;
  dataAbertura: string;
  execucaoIniciadaEm: string | null;
  finalizadaEm: string | null;
  entregueEm: string | null;
  servicos: ServicoOsResponse[];
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

export interface OrdemServicoFiltro {
  cliente?: string;
  numeroOs?: string;
  status?: StatusOrdemServico;
  page?: number;
  size?: number;
}
