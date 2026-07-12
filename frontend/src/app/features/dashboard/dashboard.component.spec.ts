import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { AuthService } from '../../core/services/auth.service';
import { DashboardService, TempoMedioOsResponse, TempoMedioServicoResponse } from '../../core/services/dashboard.service';

describe('DashboardComponent', () => {
  let mockAuth: jasmine.SpyObj<AuthService>;
  let mockDashboardService: jasmine.SpyObj<DashboardService>;

  function criarComponente(role: string | null): DashboardComponent {
    mockAuth.getUsuarioLogado.and.returnValue(role ? { email: 'user@teste.com', role } : null);
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuth },
        { provide: DashboardService, useValue: mockDashboardService },
      ],
    });
    return TestBed.runInInjectionContext(() => new DashboardComponent());
  }

  beforeEach(() => {
    mockAuth = jasmine.createSpyObj('AuthService', ['getUsuarioLogado']);
    mockDashboardService = jasmine.createSpyObj('DashboardService', ['getTempoMedioOs', 'getTempoMedioPorServico']);
  });

  it('deve montar atalhos e rotulo para ADMIN e carregar metricas', () => {
    const tempoOs: TempoMedioOsResponse = { quantidadeOrdensFinalizadas: 10, tempoMedioSegundos: 3600, tempoMedioMinutos: 60, tempoMedioHoras: 1 };
    const servicos: TempoMedioServicoResponse[] = [
      { servicoId: 1, nomeServico: 'A', quantidadeExecucoes: 2, tempoMedioSegundos: 100, tempoMedioMinutos: 1, tempoMedioHoras: 0.1 },
      { servicoId: 2, nomeServico: 'B', quantidadeExecucoes: 5, tempoMedioSegundos: 100, tempoMedioMinutos: 1, tempoMedioHoras: 0.1 },
    ];
    mockDashboardService.getTempoMedioOs.and.returnValue(of(tempoOs));
    mockDashboardService.getTempoMedioPorServico.and.returnValue(of(servicos));

    const component = criarComponente('ADMIN');
    component.ngOnInit();

    expect(component.isAdmin).toBeTrue();
    expect(component.roleLabel).toBe('Administrador');
    expect(component.shortcuts.length).toBeGreaterThan(0);
    expect(component.tempoMedioOs()).toEqual(tempoOs);
    expect(component.topServicos()[0].servicoId).toBe(2);
    expect(component.metricsLoading()).toBeFalse();
  });

  it('nao deve carregar metricas para roles diferentes de ADMIN', () => {
    const component = criarComponente('ATENDENTE');

    component.ngOnInit();

    expect(mockDashboardService.getTempoMedioOs).not.toHaveBeenCalled();
    expect(component.isAdmin).toBeFalse();
    expect(component.roleContext).toContain('atalhos');
  });

  it('deve setar metricsError quando falha ao carregar metricas', () => {
    mockDashboardService.getTempoMedioOs.and.returnValue(throwError(() => ({})));
    mockDashboardService.getTempoMedioPorServico.and.returnValue(of([]));

    const component = criarComponente('ADMIN');
    component.ngOnInit();

    expect(component.metricsError()).toBeTrue();
    expect(component.metricsLoading()).toBeFalse();
  });

  it('deve retornar shortcuts vazios e roleLabel igual a role quando desconhecida', () => {
    const component = criarComponente('DESCONHECIDA');

    expect(component.shortcuts).toEqual([]);
    expect(component.roleLabel).toBe('DESCONHECIDA');
  });

  it('deve lidar com usuario nao logado', () => {
    const component = criarComponente(null);

    expect(component.role).toBe('');
    expect(component.shortcuts).toEqual([]);
  });

  it('formatHoras deve formatar em minutos quando menor que 1 hora', () => {
    const component = criarComponente('ADMIN');

    expect(component.formatHoras(0.5)).toBe('30 min');
  });

  it('formatHoras deve formatar em horas quando maior ou igual a 1', () => {
    const component = criarComponente('ADMIN');

    expect(component.formatHoras(2.567)).toBe('2.6 h');
  });
});
