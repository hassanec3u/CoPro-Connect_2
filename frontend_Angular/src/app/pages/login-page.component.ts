import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { ToastService } from '../core/toast.service';
import { getUserFriendlyErrorMessage } from '../utils/error.utils';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.css'
})
export class LoginPageComponent {
  username = '';
  password = '';
  error: string | null = null;
  showPassword = false;

  constructor(
    private auth: AuthService, 
    private router: Router, 
    private toast: ToastService,
    private cdr: ChangeDetectorRef
  ) {}

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  async handleLogin(event?: Event): Promise<void> {
    // Empêcher le comportement par défaut du formulaire
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    
    // Vérifier que les champs ne sont pas vides
    if (!this.username.trim() || !this.password.trim()) {
      const message = 'Veuillez saisir votre nom d\'utilisateur et votre mot de passe.';
      this.error = message;
      this.toast.show(message, 'error');
      this.cdr.detectChanges(); // Forcer la détection des changements
      return;
    }
    
    this.error = null;
    this.cdr.detectChanges(); // Réinitialiser l'affichage des erreurs
    
    try {
      await this.auth.login(this.username.trim(), this.password);
      // Afficher le message de succès
      this.toast.show('Connexion réussie !', 'success');
      // Naviguer vers le dashboard (sans await pour ne pas bloquer)
      this.router.navigateByUrl('/').catch(() => {
        // Ignorer les erreurs de navigation
      });
    } catch (err: any) {
      // S'assurer que l'erreur est toujours capturée et affichée
      const message = getUserFriendlyErrorMessage(err);
      this.error = message;
      this.toast.show(message, 'error');
      // Forcer la détection des changements immédiatement après avoir défini l'erreur
      this.cdr.detectChanges();
      // Log pour debug (peut être supprimé en production)
      console.error('Erreur de connexion:', err);
    }
  }
}
