import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { OrdemServicoService } from '../ordem-servico.service';
import {
  FinalizarDiagnosticoResponse,
  IncluirMecanicoRequest,
  ItensNecessariosRequest,
  OrdemServicoDetalheResponse,
  OrdemServicoResponse,
  RegistrarLaudoRequest,
  ServicoSolicitadoRequest,
} from '../ordem-servico.model';
import { OrcamentoService } from '../../orcamentos/orcamento.service';
import { OrcamentoResponse } from '../../orcamentos/orcamento.model';
import { ReparoAdicionalService } from '../../reparos-adicionais/reparo-adicional.service';
import {
  CriarReparoAdicionalRequest,
  CriarReparoAdicionalResponse,
} from '../../reparos-adicionais/reparo-adicional.model';

@Injectable()
export class DetalheOsFacade {
  private readonly ordemServicoService = inject(OrdemServicoService);
  private readonly orcamentoService = inject(OrcamentoService);
  private readonly reparoAdicionalService = inject(ReparoAdicionalService);

  buscar(numeroOs: string): Observable<OrdemServicoDetalheResponse> {
    return this.ordemServicoService.buscarPorNumeroOs(numeroOs);
  }

  iniciarDiagnostico(numeroOs: string): Observable<OrdemServicoResponse> {
    return this.ordemServicoService.iniciarDiagnostico(numeroOs);
  }

  registrarLaudo(numeroOs: string, request: RegistrarLaudoRequest): Observable<OrdemServicoResponse> {
    return this.ordemServicoService.registrarLaudo(numeroOs, request);
  }

  incluirServicos(numeroOs: string, servicos: ServicoSolicitadoRequest[]): Observable<OrdemServicoResponse> {
    return this.ordemServicoService.incluirServicos(numeroOs, servicos);
  }

  registrarItens(numeroOs: string, servicoId: number, itens: ItensNecessariosRequest[]): Observable<OrdemServicoResponse> {
    return this.ordemServicoService.registrarItensServico(numeroOs, servicoId, itens);
  }

  finalizarDiagnostico(numeroOs: string): Observable<FinalizarDiagnosticoResponse> {
    return this.ordemServicoService.finalizarDiagnostico(numeroOs);
  }

  atribuirMecanico(numeroOs: string, request: IncluirMecanicoRequest): Observable<OrdemServicoResponse> {
    return this.ordemServicoService.atribuirMecanico(numeroOs, request);
  }

  entregar(numeroOs: string): Observable<OrdemServicoResponse> {
    return this.ordemServicoService.entregar(numeroOs);
  }

  iniciarServico(numeroOs: string, servicoId: number): Observable<OrdemServicoResponse> {
    return this.ordemServicoService.iniciarServico(numeroOs, servicoId);
  }

  finalizarServico(numeroOs: string, servicoId: number): Observable<OrdemServicoResponse> {
    return this.ordemServicoService.finalizarServico(numeroOs, servicoId);
  }

  aprovarOrcamento(orcamentoId: number): Observable<OrcamentoResponse> {
    return this.orcamentoService.aprovar(orcamentoId);
  }

  recusarOrcamento(orcamentoId: number, motivo: string | null): Observable<OrcamentoResponse> {
    return this.orcamentoService.recusar(orcamentoId, motivo);
  }

  baixarOrcamentoPdf(orcamentoId: number): Observable<Blob> {
    return this.orcamentoService.baixarPdf(orcamentoId);
  }

  criarReparoAdicional(numeroOs: string, request: CriarReparoAdicionalRequest): Observable<CriarReparoAdicionalResponse> {
    return this.reparoAdicionalService.criar(numeroOs, request);
  }
}
