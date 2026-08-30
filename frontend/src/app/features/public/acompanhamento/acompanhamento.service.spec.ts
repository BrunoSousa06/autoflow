import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../../environments/environment';
import { AcompanhamentoService } from './acompanhamento.service';

describe('AcompanhamentoService', () => {
  let service: AcompanhamentoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AcompanhamentoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('deve consultar o endpoint público com o token', () => {
    service.consultar('abc').subscribe();
    const req = http.expectOne(`${environment.apiUrl}/public/ordens-servico/acompanhamento?token=abc`);
    expect(req.request.method).toBe('GET');
    req.flush({ numeroOs: 'OS-1', status: 'RECEBIDA', dataAbertura: '2026-08-01T10:00:00' });
  });

  it('deve baixar o PDF do orçamento pelo token de acompanhamento', () => {
    service.baixarOrcamento(10, 'abc').subscribe();

    const req = http.expectOne(`${environment.apiUrl}/public/orcamentos/10/pdf/acompanhamento?token=abc`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['pdf'], { type: 'application/pdf' }));
  });

  it('deve aprovar o orçamento pelo token de acompanhamento', () => {
    service.aprovarOrcamento(10, 'abc').subscribe();

    const req = http.expectOne(`${environment.apiUrl}/public/orcamentos/10/aprovar/acompanhamento?token=abc`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush({});
  });
});
