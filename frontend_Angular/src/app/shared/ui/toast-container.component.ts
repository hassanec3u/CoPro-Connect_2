import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { ToastService, ToastItem } from '../../core/toast.service';
import { ToastComponent } from './toast.component';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule, ToastComponent],
  templateUrl: './toast-container.component.html',
  styleUrl: './toast-container.component.css'
})
export class ToastContainerComponent implements OnInit, OnDestroy {
  toasts: ToastItem[] = [];
  private sub?: Subscription;
  private timers = new Map<number, any>();

  constructor(public toastService: ToastService) {}

  ngOnInit(): void {
    this.sub = this.toastService.toasts$.subscribe((items) => {
      const previousIds = new Set(this.toasts.map((toast) => toast.id));
      const currentIds = new Set(items.map((toast) => toast.id));
      
      this.toasts = items;

      // Nettoyer les timers pour les toasts qui n'existent plus
      this.timers.forEach((timer, id) => {
        if (!currentIds.has(id)) {
          clearTimeout(timer);
          this.timers.delete(id);
        }
      });

      // Créer des timers pour les nouveaux toasts
      items.forEach((toast) => {
        if (toast.duration > 0 && !this.timers.has(toast.id) && !previousIds.has(toast.id)) {
          const timer = setTimeout(() => {
            this.toastService.close(toast.id);
            this.timers.delete(toast.id);
          }, toast.duration);
          this.timers.set(toast.id, timer);
        }
      });
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.timers.forEach((timer) => clearTimeout(timer));
    this.timers.clear();
  }
}
