import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const snackBar = inject(MatSnackBar);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Erros de login são tratados no componente
      if (req.url.includes('/auth/login')) {
        return throwError(() => error);
      }

      switch (error.status) {
        case 401:
          auth.logout();
          router.navigate(['/login']);
          break;

        case 403:
          snackBar.open(
            'Sem permissão para acessar este recurso.',
            'Fechar',
            { duration: 4000 }
          );
          break;

        default:
          if (error.status >= 500 || error.status === 0) {
            snackBar.open(
              'Erro interno do servidor. Tente novamente.',
              'Fechar',
              { duration: 4000 }
            );
          }
      }

      return throwError(() => error);
    })
  );
};
