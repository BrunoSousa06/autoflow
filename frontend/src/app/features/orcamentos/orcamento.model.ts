export type StatusOrcamento = 'DISPONIVEL' | 'APROVADO' | 'REPROVADO' | 'SUBSTITUIDO';

export type TipoOrcamento = 'PRINCIPAL' | 'ADICIONAL';

export interface OrcamentoResponse {
  id: number;
  ordemServicoId: number;
  numeroOs: string;
  tipo: TipoOrcamento;
  versao: number;
  status: StatusOrcamento;
  totalServicos: number;
  totalItens: number;
  totalGeral: number;
  servicos: OrcamentoServico[];
  itens: OrcamentoItemNecessario[];
  criadoEm: string;
  disponibilizadoEm: string | null;
}

export interface OrcamentoServico {
  servicoId: number;
  nome: string;
  valor: number;
}

export interface OrcamentoItemNecessario {
  pecaInsumoId: number;
  servicoOsId: number;
  nome: string;
  tipo: 'PECA' | 'INSUMO' | string;
  valorUnitario: number;
  quantidade: number;
  valorTotal: number;
}

export interface OrcamentoFiltro {
  statusOrcamento?: StatusOrcamento | '';
  numeroOs?: string;
  placa?: string;
  clienteEmail?: string;
  clienteDocumento?: string;
  tipo?: TipoOrcamento | '';
}

export interface RecusarOrcamentoRequest {
  motivo?: string | null;
}

export const STATUS_ORCAMENTO_LABEL: Record<StatusOrcamento, string> = {
  DISPONIVEL: 'Pendente',
  APROVADO: 'Aprovado',
  REPROVADO: 'Reprovado',
  SUBSTITUIDO: 'Substituído',
};

export const TIPO_ORCAMENTO_LABEL: Record<TipoOrcamento, string> = {
  PRINCIPAL: 'Principal',
  ADICIONAL: 'Adicional',
};
