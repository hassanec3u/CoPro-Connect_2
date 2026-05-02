import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Guard réservant la route aux comptes ayant le rôle ADMIN.
 * - Non authentifié -> redirection vers /login
 * - Authentifié mais non admin -> redirection vers /
 */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated) {
    return router.parseUrl('/login');
  }

  if (!auth.isAdmin) {
    return router.parseUrl('/');
  }

  return true;
};
