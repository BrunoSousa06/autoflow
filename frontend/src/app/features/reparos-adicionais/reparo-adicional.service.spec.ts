import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ReparoAdicionalService } from './reparo-adicional.service';
import { CriarReparoAdicionalRequest, CriarReparoAdicionalResponse } from './reparo-adicional.model';
import { environment } from '../../../environments/environment';

const BASE = `${environment.apiUrl}/ordens-servico`;

describe('ReparoAdicionalService', () => {
  let service: ReparoAdicionalService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(ReparoAdicionalService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('deve criar o servico', () => {
    expect(service).toBeTruthy();
  });

  it('criar deve chamar POST em /ordens-servico/{numeroOs}/reparos-adicionais com o body correto', () => {
    const body: CriarReparoAdicionalRequest = {
      servicos: [{ servicoId: 1, itensNecessarios: [{ pecaInsumoId: 10, quantidade: 2 }] }],
    };
    const resposta: CriarReparoAdicionalResponse = { reparoAdicionalId: 1, orcamentoId: 5, publicUrl: '/public/OS-001' };

    let resultado: CriarReparoAdicionalResponse | undefined;
    service.criar('OS-001', body).subscribe((r) => (resultado = r));

    const req = httpTesting.expectOne(`${BASE}/OS-001/reparos-adicionais`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush(resposta);

    expect(resultado).toEqual(resposta);
  });
});
