import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * Intercepteur HTTP pour :
 * 1. Ajouter automatiquement le token JWT aux requêtes authentifiées
 * 2. Gérer les erreurs HTTP (401, 403, etc.)
 * 3. Rediriger vers /login en cas de session expirée
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Ne pas ajouter le token pour les routes d'authentification
  const isAuthRoute = req.url.includes('/api/auth/login');
  
  // Récupérer le token actuel
  const token = authService.token;

  // Cloner la requête et ajouter le header Authorization si un token existe
  // et que ce n'est pas une route d'authentification
  if (token && !isAuthRoute) {
    const headers: Record<string, string> = {
      Authorization: `Bearer ${token}`
    };
    
    // Ajouter Content-Type seulement s'il n'est pas déjà défini
    if (!req.headers.has('Content-Type')) {
      headers['Content-Type'] = 'application/json';
    }
    
    req = req.clone({
      setHeaders: headers
    });
  }

  // Intercepter la réponse pour gérer les erreurs
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Gérer les erreurs d'authentification
      if (error.status === 401) {
        // Token invalide ou expiré
        authService.logout(false);
        // Rediriger vers la page de login si on n'y est pas déjà
        if (!router.url.includes('/login')) {
          router.navigate(['/login']);
        }
        return throwError(() => new Error('Votre session a expiré. Veuillez vous reconnecter.'));
      }

      // Gérer les erreurs d'autorisation
      if (error.status === 403) {
        return throwError(() => new Error('Accès refusé. Vous n\'avez pas les permissions nécessaires.'));
      }

      // Gérer les erreurs serveur
      if (error.status >= 500) {
        return throwError(() => new Error('Erreur serveur. Veuillez réessayer plus tard.'));
      }

      // Pour les autres erreurs, propager l'erreur telle quelle
      // Elle sera gérée par getUserFriendlyErrorMessage dans les services
      return throwError(() => error);
    })
  );
};
