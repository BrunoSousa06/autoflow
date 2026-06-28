import {
  OrcamentoResumoOs,
  ServicoOsResponse,
  StatusOrcamento,
  StatusOrdemServico,
} from '../ordens-servico/ordem-servico.model';

export interface ClienteLogadoResponse {
  id: number;
  nome: string;
  cpfCnpj: string;
  telefone: string;
  email: string;
  veiculos: VeiculoClienteResponse[];
}

export interface VeiculoClienteResponse {
  id: number;
  marca: string | null;
  ano: number | null;
  placa: string;
  modelo: string | null;
}

export interface HistoricoStatusOsCliente {
  status: StatusOrdemServico;
  mensagemCliente: string;
  registradoEm: string;
}

export interface AcompanhamentoOrdemServicoResponse {
  numeroOs: string;
  placa: string;
  statusAtual: StatusOrdemServico;
  dataAbertura: string;
  ultimaAtualizacao: string;
  servicosSolicitados: ServicoOsResponse[];
  orcamentoAtual: OrcamentoResumoOs | null;
  situacaoAprovacao: StatusOrcamento | null;
  mensagemParaCliente: string;
  historicoStatus: HistoricoStatusOsCliente[];
}
