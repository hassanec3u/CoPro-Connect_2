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
  // Étape 1: Login
  username = '';
  password = '';
  showPassword = false;

  // Étape 2: MFA
  mfaStep = false;
  mfaCode = '';
  maskedEmail = '';
  mfaDigits: string[] = ['', '', '', '', '', ''];

  // Commun
  error: string | null = null;
  loading = false;
  countdown = 0;
  private countdownInterval: any = null;

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
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    if (!this.username.trim() || !this.password.trim()) {
      const message = 'Veuillez saisir votre nom d\'utilisateur et votre mot de passe.';
      this.error = message;
      this.toast.show(message, 'error');
      this.cdr.detectChanges();
      return;
    }

    this.error = null;
    this.loading = true;
    this.cdr.detectChanges();

    try {
      const response = await this.auth.login(this.username.trim(), this.password);

      if (response.mfa_required) {
        this.mfaStep = true;
        this.maskedEmail = response.masked_email || '';
        this.mfaDigits = ['', '', '', '', '', ''];
        this.toast.show('Code de vérification envoyé !', 'success');
        this.startResendCountdown();
        this.loading = false;
        this.cdr.detectChanges();
        setTimeout(() => this.focusMfaInput(0), 100);
        return;
      }

      this.toast.show('Connexion réussie !', 'success');
      this.router.navigateByUrl('/').catch(() => {});
    } catch (err: any) {
      const message = getUserFriendlyErrorMessage(err);
      this.error = message;
      this.toast.show(message, 'error');
      this.cdr.detectChanges();
    } finally {
      this.loading = false;
      this.cdr.detectChanges();
    }
  }

  async handleVerifyMfa(event?: Event): Promise<void> {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    const code = this.mfaDigits.join('');
    if (code.length !== 6) {
      this.error = 'Veuillez saisir le code à 6 chiffres.';
      this.cdr.detectChanges();
      return;
    }

    this.error = null;
    this.loading = true;
    this.cdr.detectChanges();

    try {
      await this.auth.verifyMfa(this.username.trim(), code);
      this.toast.show('Connexion réussie !', 'success');
      this.router.navigateByUrl('/').catch(() => {});
    } catch (err: any) {
      const message = getUserFriendlyErrorMessage(err);
      this.error = message;
      this.toast.show(message, 'error');
      this.mfaDigits = ['', '', '', '', '', ''];
      this.cdr.detectChanges();
      setTimeout(() => this.focusMfaInput(0), 100);
    } finally {
      this.loading = false;
      this.cdr.detectChanges();
    }
  }

  async handleResendCode(): Promise<void> {
    if (this.countdown > 0) return;

    this.error = null;
    this.loading = true;
    this.cdr.detectChanges();

    try {
      await this.auth.login(this.username.trim(), this.password);
      this.toast.show('Nouveau code envoyé !', 'success');
      this.mfaDigits = ['', '', '', '', '', ''];
      this.startResendCountdown();
      setTimeout(() => this.focusMfaInput(0), 100);
    } catch (err: any) {
      const message = getUserFriendlyErrorMessage(err);
      this.error = message;
      this.toast.show(message, 'error');
    } finally {
      this.loading = false;
      this.cdr.detectChanges();
    }
  }

  handleBackToLogin(): void {
    this.mfaStep = false;
    this.mfaDigits = ['', '', '', '', '', ''];
    this.error = null;
    this.countdown = 0;
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
      this.countdownInterval = null;
    }
    this.cdr.detectChanges();
  }

  onMfaDigitInput(event: Event, index: number): void {
    const input = event.target as HTMLInputElement;
    const value = input.value;

    // Gérer le collage d'un code complet
    if (value.length > 1) {
      const digits = value.replace(/\D/g, '').split('');
      for (let i = 0; i < 6; i++) {
        this.mfaDigits[i] = digits[i] || '';
      }
      this.cdr.detectChanges();
      const lastFilledIndex = Math.min(digits.length - 1, 5);
      this.focusMfaInput(lastFilledIndex < 5 ? lastFilledIndex + 1 : 5);
      return;
    }

    // Garder seulement le dernier chiffre saisi
    this.mfaDigits[index] = value.replace(/\D/g, '').slice(-1);
    this.cdr.detectChanges();

    // Avancer automatiquement au champ suivant
    if (this.mfaDigits[index] && index < 5) {
      this.focusMfaInput(index + 1);
    }

    // Soumettre automatiquement quand tous les chiffres sont remplis
    if (this.mfaDigits.every(d => d.length === 1)) {
      this.handleVerifyMfa();
    }
  }

  onMfaDigitKeydown(event: KeyboardEvent, index: number): void {
    if (event.key === 'Backspace' && !this.mfaDigits[index] && index > 0) {
      this.mfaDigits[index - 1] = '';
      this.cdr.detectChanges();
      this.focusMfaInput(index - 1);
      event.preventDefault();
    }
  }

  private focusMfaInput(index: number): void {
    const input = document.getElementById('mfa-digit-' + index) as HTMLInputElement;
    if (input) {
      input.focus();
      input.select();
    }
  }

  private startResendCountdown(): void {
    this.countdown = 60;
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }
    this.countdownInterval = setInterval(() => {
      this.countdown--;
      if (this.countdown <= 0) {
        clearInterval(this.countdownInterval);
        this.countdownInterval = null;
      }
      this.cdr.detectChanges();
    }, 1000);
  }
}
