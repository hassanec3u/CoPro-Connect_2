import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { Subscription } from 'rxjs';
import { ToastContainerComponent } from './shared/ui/toast-container.component';
import { AuthService } from './core/auth.service';
import { ToastService } from './core/toast.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastContainerComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {
  private sub?: Subscription;

  constructor(private auth: AuthService, private toast: ToastService, private router: Router) {}

  ngOnInit(): void {
    this.sub = this.auth.sessionExpired$.subscribe(() => {
      this.toast.show('Votre session a expiré. Veuillez vous reconnecter.', 'warning');
      this.router.navigateByUrl('/login');
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}
