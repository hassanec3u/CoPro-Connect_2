import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-dialog.component.html',
  styleUrl: './confirm-dialog.component.css'
})
export class ConfirmDialogComponent {
  @Input() message = '';
  @Input() confirmText = 'Confirmer';
  @Input() cancelText = 'Annuler';
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
  
  private isProcessing = false;
  
  handleConfirm(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    
    // Empêcher les doubles clics
    if (this.isProcessing) {
      return;
    }
    
    this.isProcessing = true;
    this.confirm.emit();
  }
  
  handleCancel(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    
    // Empêcher les doubles clics
    if (this.isProcessing) {
      return;
    }
    
    this.isProcessing = true;
    this.cancel.emit();
  }
}
