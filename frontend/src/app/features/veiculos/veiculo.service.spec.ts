import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { VeiculoService } from './veiculo.service';
import { VeiculoRequest, VeiculoResponse, VeiculoUpdateRequest } from './veiculo.model';
import { environment } from '../../../environments/environment';

const BASE = `${environment.apiUrl}/veiculos`;

describe('VeiculoService', () => {
  let service: VeiculoService;
  let httpTesting: HttpTestingController;

  const cliente = { id: 1, nome: 'Cliente Teste', cpfCnpj: '123.456.789-00', telefone: '11999999999', email: 'cliente@teste.com' };
  const veiculo: VeiculoResponse = { id: 1, marca: 'Fiat', ano: 2020, placa: 'ABC1D23', modelo: 'Uno', cliente };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(VeiculoService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('deve criar o servico', () => {
    expect(service).toBeTruthy();
  });

  it('listar sem filtros deve incluir apenas page e size', () => {
    service.listar().subscribe();

    const req = httpTesting.expectOne((r) => r.url === BASE);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('20');
    expect(req.request.params.has('placa')).toBeFalse();
    req.flush({ content: [veiculo], page: { totalElements: 1, totalPages: 1, number: 0, size: 20 } });
  });

  it('listar com filtros deve incluir os query params fornecidos', () => {
    service.listar({ placa: 'ABC1D23', marca: 'Fiat', modelo: 'Uno', ano: 2020 }, 1, 10).subscribe();

    const req = httpTesting.expectOne((r) => r.url === BASE);
    expect(req.request.params.get('placa')).toBe('ABC1D23');
    expect(req.request.params.get('marca')).toBe('Fiat');
    expect(req.request.params.get('modelo')).toBe('Uno');
    expect(req.request.params.get('ano')).toBe('2020');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('10');
    req.flush({ content: [], page: { totalElements: 0, totalPages: 0, number: 1, size: 10 } });
  });

  it('buscarPorId deve chamar GET em /veiculos/{id}', () => {
    service.buscarPorId(1).subscribe();

    const req = httpTesting.expectOne(`${BASE}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(veiculo);
  });

  it('cadastrar deve chamar POST em /veiculos com o body correto', () => {
    const body: VeiculoRequest = { cpfCnpj: '123.456.789-00', marca: 'Fiat', ano: 2020, placa: 'ABC1D23', modelo: 'Uno' };

    service.cadastrar(body).subscribe();

    const req = httpTesting.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush(veiculo);
  });

  it('atualizar deve chamar PATCH em /veiculos/{id}/atualizacao', () => {
    const body: VeiculoUpdateRequest = { marca: 'Fiat', ano: 2021, placa: 'ABC1D23', modelo: 'Uno' };

    service.atualizar(1, body).subscribe();

    const req = httpTesting.expectOne(`${BASE}/1/atualizacao`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(body);
    req.flush(veiculo);
  });

  it('deletar deve chamar DELETE em /veiculos/{id} esperando resposta texto', () => {
    let resultado: string | undefined;
    service.deletar(1).subscribe((r) => (resultado = r));

    const req = httpTesting.expectOne(`${BASE}/1`);
    expect(req.request.method).toBe('DELETE');
    expect(req.request.responseType).toBe('text');
    req.flush('Veiculo removido');

    expect(resultado).toBe('Veiculo removido');
  });
});
