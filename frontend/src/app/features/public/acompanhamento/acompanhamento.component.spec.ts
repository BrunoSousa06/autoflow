import { Router } from '@angular/router';
import { AcompanhamentoComponent } from './acompanhamento.component';

describe('AcompanhamentoComponent', () => {
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(() => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
  });

  it('irParaLogin deve navegar para /login', () => {
    const component = new AcompanhamentoComponent(mockRouter);

    component.irParaLogin();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
  });
});
