import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { DetalheOsFacade } from './detalhe-os.facade';
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

describe('DetalheOsFacade', () => {
  let facade: DetalheOsFacade;
  let ordemServicoService: jasmine.SpyObj<OrdemServicoService>;
  let orcamentoService: jasmine.SpyObj<OrcamentoService>;
  let reparoAdicionalService: jasmine.SpyObj<ReparoAdicionalService>;

  const osResponse = {} as OrdemServicoResponse;
  const detalheResponse = {} as OrdemServicoDetalheResponse;
  const diagnosticoResponse = {} as FinalizarDiagnosticoResponse;
  const orcamentoResponse = {} as OrcamentoResponse;
  const reparoResponse = {} as CriarReparoAdicionalResponse;

  beforeEach(() => {
    ordemServicoService = jasmine.createSpyObj<OrdemServicoService>('OrdemServicoService', [
      'buscarPorNumeroOs',
      'iniciarDiagnostico',
      'registrarLaudo',
      'incluirServicos',
      'registrarItensServico',
      'finalizarDiagnostico',
      'atribuirMecanico',
      'entregar',
      'iniciarServico',
      'finalizarServico',
    ]);
    orcamentoService = jasmine.createSpyObj<OrcamentoService>('OrcamentoService', [
      'aprovar',
      'recusar',
      'baixarPdf',
    ]);
    reparoAdicionalService = jasmine.createSpyObj<ReparoAdicionalService>('ReparoAdicionalService', ['criar']);

    TestBed.configureTestingModule({
      providers: [
        DetalheOsFacade,
        { provide: OrdemServicoService, useValue: ordemServicoService },
        { provide: OrcamentoService, useValue: orcamentoService },
        { provide: ReparoAdicionalService, useValue: reparoAdicionalService },
      ],
    });

    facade = TestBed.inject(DetalheOsFacade);
  });

  it('deve delegar a busca da ordem de serviço', () => {
    ordemServicoService.buscarPorNumeroOs.and.returnValue(of(detalheResponse));

    expect(facade.buscar('OS-001')).toBeDefined();
    expect(ordemServicoService.buscarPorNumeroOs).toHaveBeenCalledWith('OS-001');
  });

  it('deve delegar os comandos de diagnóstico e serviços', () => {
    const requestLaudo = { laudo: 'Diagnóstico concluído' } as RegistrarLaudoRequest;
    const servicos = [{ servicoId: 10, quantidade: 1 } as ServicoSolicitadoRequest];
    const itens = [{ pecaInsumoId: 20, quantidade: 2 }] as ItensNecessariosRequest[];
    const requestMecanico = { mecanicoId: 30 } as IncluirMecanicoRequest;

    ordemServicoService.iniciarDiagnostico.and.returnValue(of(osResponse));
    ordemServicoService.registrarLaudo.and.returnValue(of(osResponse));
    ordemServicoService.incluirServicos.and.returnValue(of(osResponse));
    ordemServicoService.registrarItensServico.and.returnValue(of(osResponse));
    ordemServicoService.finalizarDiagnostico.and.returnValue(of(diagnosticoResponse));
    ordemServicoService.atribuirMecanico.and.returnValue(of(osResponse));
    ordemServicoService.entregar.and.returnValue(of(osResponse));
    ordemServicoService.iniciarServico.and.returnValue(of(osResponse));
    ordemServicoService.finalizarServico.and.returnValue(of(osResponse));

    facade.iniciarDiagnostico('OS-001');
    facade.registrarLaudo('OS-001', requestLaudo);
    facade.incluirServicos('OS-001', servicos);
    facade.registrarItens('OS-001', 11, itens);
    facade.finalizarDiagnostico('OS-001');
    facade.atribuirMecanico('OS-001', requestMecanico);
    facade.entregar('OS-001');
    facade.iniciarServico('OS-001', 11);
    facade.finalizarServico('OS-001', 11);

    expect(ordemServicoService.iniciarDiagnostico).toHaveBeenCalledWith('OS-001');
    expect(ordemServicoService.registrarLaudo).toHaveBeenCalledWith('OS-001', requestLaudo);
    expect(ordemServicoService.incluirServicos).toHaveBeenCalledWith('OS-001', servicos);
    expect(ordemServicoService.registrarItensServico).toHaveBeenCalledWith('OS-001', 11, itens);
    expect(ordemServicoService.finalizarDiagnostico).toHaveBeenCalledWith('OS-001');
    expect(ordemServicoService.atribuirMecanico).toHaveBeenCalledWith('OS-001', requestMecanico);
    expect(ordemServicoService.entregar).toHaveBeenCalledWith('OS-001');
    expect(ordemServicoService.iniciarServico).toHaveBeenCalledWith('OS-001', 11);
    expect(ordemServicoService.finalizarServico).toHaveBeenCalledWith('OS-001', 11);
  });

  it('deve delegar as operações de orçamento', () => {
    orcamentoService.aprovar.and.returnValue(of(orcamentoResponse));
    orcamentoService.recusar.and.returnValue(of(orcamentoResponse));
    orcamentoService.baixarPdf.and.returnValue(of(new Blob(['pdf'])));

    facade.aprovarOrcamento(40);
    facade.recusarOrcamento(40, 'Valor não aprovado');
    facade.baixarOrcamentoPdf(40);

    expect(orcamentoService.aprovar).toHaveBeenCalledWith(40);
    expect(orcamentoService.recusar).toHaveBeenCalledWith(40, 'Valor não aprovado');
    expect(orcamentoService.baixarPdf).toHaveBeenCalledWith(40);
  });

  it('deve delegar a criação de reparo adicional', () => {
    const request = {} as CriarReparoAdicionalRequest;
    reparoAdicionalService.criar.and.returnValue(of(reparoResponse));

    facade.criarReparoAdicional('OS-001', request);

    expect(reparoAdicionalService.criar).toHaveBeenCalledWith('OS-001', request);
  });
});
