import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ResidentService } from '../../residents/resident.service';

@Component({
  selector: 'app-export-happix-pdf-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button class="btn-save" (click)="handleExport()" [disabled]="exporting">
      {{ exporting ? '⏳ Export en cours...' : '📄 Exporter PDF' }}
    </button>
  `
})
export class ExportHappixPdfButtonComponent {
  exporting = false;

  constructor(private residentService: ResidentService) {}

  async handleExport(): Promise<void> {
    this.exporting = true;
    try {
      await this.residentService.exportHappixPdf();
    } catch (err) {
      console.error('Erreur export PDF Happix', err);
    } finally {
      this.exporting = false;
    }
  }
}
