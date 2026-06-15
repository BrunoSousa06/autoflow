export type StatusOrdemServico =
  | 'RECEBIDA'
  | 'EM_DIAGNOSTICO'
  | 'AGUARDANDO_APROVACAO'
  | 'EM_EXECUCAO'
  | 'FINALIZADA'
  | 'ENTREGUE';

export type StatusServicoOs = 'AGUARDANDO' | 'EM_EXECUCAO' | 'FINALIZADO' | 'CANCELADO';
export type StatusOrcamento = 'DISPONIVEL' | 'APROVADO' | 'REPROVADO' | 'SUBSTITUIDO';
export type TipoOrcamento = 'PRINCIPAL' | 'ADICIONAL';
export type StatusItemNecessario = 'DISPONIVEL' | 'PENDENTE' | 'UTILIZADO' | 'CANCELADO';
export type CategoriaPecaInsumo = string;

export const STATUS_OS_LABEL: Record<StatusOrdemServico, string> = {
  RECEBIDA: 'Recebida',
  EM_DIAGNOSTICO: 'Em Diagnóstico',
  AGUARDANDO_APROVACAO: 'Aguardando Aprovação',
  EM_EXECUCAO: 'Em Execução',
  FINALIZADA: 'Finalizada',
  ENTREGUE: 'Entregue',
};

export const STATUS_SERVICO_OS_LABEL: Record<StatusServicoOs, string> = {
  AGUARDANDO: 'Aguardando',
  EM_EXECUCAO: 'Em Execução',
  FINALIZADO: 'Finalizado',
  CANCELADO: 'Cancelado',
};

export const STATUS_ORCAMENTO_LABEL: Record<StatusOrcamento, string> = {
  DISPONIVEL: 'Disponível',
  APROVADO: 'Aprovado',
  REPROVADO: 'Reprovado',
  SUBSTITUIDO: 'Substituído',
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

export interface IncluirMecanicoRequest {
  mecanicoId?: number | null;
  mecanicoEmail?: string | null;
}

export interface ItemNecessarioOs {
  pecaInsumoId: number;
  nome: string;
  tipo: CategoriaPecaInsumo;
  valorUnitario: number;
  quantidade: number;
  valorTotal: number;
  status: StatusItemNecessario;
  motivoPendencia: string | null;
  quantidadeDisponivel: number | null;
  mensagemStatus: string | null;
}

export interface ServicoOsResponse {
  id: number;
  servicoId: number;
  nome: string;
  valor: number;
  status: StatusServicoOs;
  iniciadoEm: string | null;
  finalizadoEm: string | null;
  itensNecessarios: ItemNecessarioOs[];
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

export interface ClienteDetalheOs {
  id: number;
  nome: string;
  cpfCnpj: string;
  email: string;
  telefone: string;
}

export interface VeiculoDetalheOs {
  id: number;
  placa: string;
  marca: string | null;
  modelo: string | null;
  ano: number;
}

export interface OrcamentoResumoOs {
  id: number;
  tipo: TipoOrcamento;
  versao: number;
  status: StatusOrcamento;
  totalServicos: number;
  totalItens: number;
  totalGeral: number;
  criadoEm: string;
  disponibilizadoEm: string | null;
  aprovadoEm: string | null;
  reprovadoEm: string | null;
  mensagem: string | null;
}

export interface OrdemServicoDetalheResponse {
  id: number;
  numeroOs: string;
  status: StatusOrdemServico;
  dataAbertura: string;
  ultimaAtualizacao: string;
  execucaoIniciadaEm: string | null;
  finalizadaEm: string | null;
  entregueEm: string | null;
  cliente: ClienteDetalheOs;
  veiculo: VeiculoDetalheOs;
  servicos: ServicoOsResponse[];
  orcamentoAtual: OrcamentoResumoOs | null;
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
