import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AcompanhamentoComponent } from './acompanhamento.component';
import { AcompanhamentoService } from './acompanhamento.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

describe('AcompanhamentoComponent', () => {
  let router: jasmine.SpyObj<Router>;
  let service: jasmine.SpyObj<AcompanhamentoService>;
  let dialog: jasmine.SpyObj<MatDialog>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  beforeEach(() => {
    router = jasmine.createSpyObj('Router', ['navigate']);
    service = jasmine.createSpyObj('AcompanhamentoService', ['consultar']);
    dialog = jasmine.createSpyObj('MatDialog', ['open']);
    snackBar = jasmine.createSpyObj('MatSnackBar', ['open']);
  });

  function criarComponente(token: string | null): AcompanhamentoComponent {
    const route = { snapshot: { queryParamMap: convertToParamMap(token ? { token } : {}) } } as ActivatedRoute;
    return new AcompanhamentoComponent(router, route, service, dialog, snackBar);
  }

  it('deve consultar e exibir a OS usando o token da URL', () => {
    service.consultar.and.returnValue(of({
      numeroOs: 'OS-123', status: 'EM_EXECUCAO', dataAbertura: '2026-08-01T10:00:00',
      execucaoIniciadaEm: '2026-08-01T11:00:00', finalizadaEm: null, entregueEm: null
    }));
    const component = criarComponente('token-publico');

    component.ngOnInit();

    expect(service.consultar).toHaveBeenCalledWith('token-publico');
    expect(component.acompanhamento?.numeroOs).toBe('OS-123');
    expect(component.progresso()).toBe(67);
    expect(component.etapaConcluida(2)).toBeTrue();
    expect(component.etapaConcluida(3)).toBeFalse();
    expect(component.atualizadoEm).not.toBeNull();
    expect(component.carregando).toBeFalse();
  });

  it('deve consultar novamente ao atualizar', () => {
    service.consultar.and.returnValue(of({
      numeroOs: 'OS-123', status: 'RECEBIDA', dataAbertura: '2026-08-01T10:00:00',
      execucaoIniciadaEm: null, finalizadaEm: null, entregueEm: null
    }));
    const component = criarComponente('token-publico');
    component.ngOnInit();

    component.atualizar();

    expect(service.consultar).toHaveBeenCalledTimes(2);
  });

  it('deve informar quando o link for inválido', () => {
    service.consultar.and.returnValue(throwError(() => new Error('não encontrado')));
    const component = criarComponente('invalido');

    component.ngOnInit();

    expect(component.erro).toContain('inválido');
    expect(component.podeTentarNovamente).toBeTrue();
  });

  it('não deve permitir nova tentativa quando o link estiver incompleto', () => {
    const component = criarComponente(null);

    component.ngOnInit();

    expect(service.consultar).not.toHaveBeenCalled();
    expect(component.podeTentarNovamente).toBeFalse();
  });

  it('irParaLogin deve navegar para /login', () => {
    const component = criarComponente(null);
    component.irParaLogin();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
