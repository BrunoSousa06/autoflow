import { ItensNecessariosRequest } from '../ordens-servico/ordem-servico.model';

export interface ServicoReparoAdicionalRequest {
  servicoId: number;
  itensNecessarios: ItensNecessariosRequest[];
}

export interface CriarReparoAdicionalRequest {
  servicos: ServicoReparoAdicionalRequest[];
}

export interface CriarReparoAdicionalResponse {
  reparoAdicionalId: number;
  orcamentoId: number;
  publicUrl: string;
}
