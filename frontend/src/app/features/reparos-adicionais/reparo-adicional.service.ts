import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CriarReparoAdicionalRequest, CriarReparoAdicionalResponse } from './reparo-adicional.model';

@Injectable({ providedIn: 'root' })
export class ReparoAdicionalService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/ordens-servico`;

  criar(numeroOs: string, req: CriarReparoAdicionalRequest): Observable<CriarReparoAdicionalResponse> {
    return this.http.post<CriarReparoAdicionalResponse>(
      `${this.base}/${numeroOs}/reparos-adicionais`,
      req,
    );
  }
}
