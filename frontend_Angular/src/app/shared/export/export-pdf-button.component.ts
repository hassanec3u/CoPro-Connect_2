import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ResidentService } from '../../residents/resident.service';

@Component({
  selector: 'app-export-pdf-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button class="btn-save" (click)="handleExport()" [disabled]="exporting">
      {{ exporting ? '⏳ Export en cours...' : '📄 Exporter PDF' }}
    </button>
  `
})
export class ExportPdfButtonComponent {
  exporting = false;

  constructor(private residentService: ResidentService) {}

  async handleExport(): Promise<void> {
    this.exporting = true;
    try {
      await this.residentService.exportResidentsPdf();
    } catch (err) {
      console.error('Erreur export PDF résidents', err);
    } finally {
      this.exporting = false;
    }
  }
}
