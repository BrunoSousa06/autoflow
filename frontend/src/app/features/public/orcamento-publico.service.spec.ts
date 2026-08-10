import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {environment} from '../../../environments/environment';
import {OrcamentoPublicoService} from './orcamento-publico.service';

describe('OrcamentoPublicoService', () => {
  let service: OrcamentoPublicoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({providers: [provideHttpClient(), provideHttpClientTesting()]});
    service = TestBed.inject(OrcamentoPublicoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('deve consultar o orçamento com o token', () => {
    service.consultar(10, 'abc').subscribe();

    const req = http.expectOne(`${environment.apiUrl}/public/orcamentos/10?token=abc`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('deve enviar aprovação e nome opcional', () => {
    service.aprovar(10, 'abc', ' Maria ').subscribe();

    const req = http.expectOne(`${environment.apiUrl}/public/orcamentos/10/aprovar?token=abc`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({nome: 'Maria'});
    req.flush({});
  });

  it('deve enviar recusa com motivo e nome', () => {
    service.recusar(10, 'abc', ' Muito caro ', ' Maria ').subscribe();

    const req = http.expectOne(`${environment.apiUrl}/public/orcamentos/10/recusar?token=abc`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({motivo: 'Muito caro', nome: 'Maria'});
    req.flush({});
  });
});
