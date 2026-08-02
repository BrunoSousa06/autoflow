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
});
