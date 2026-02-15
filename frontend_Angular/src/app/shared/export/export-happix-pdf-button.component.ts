import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { getHappixTypeLabel } from '../../utils/happix.utils';

@Component({
  selector: 'app-export-happix-pdf-button',
  standalone: true,
  imports: [CommonModule],
  template: `<button class="btn-save" (click)="handleExport()">📄 Exporter PDF</button>`
})
export class ExportHappixPdfButtonComponent {
  @Input() entries: any[] = [];

  handleExport(): void {
    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    const title = 'Liste des comptes Happix';
    const head = [[
      'Nom',
      'Email',
      'Numéro',
      'Nom borne',
      'Type',
      'Relation',
      'Bâtiment',
      'Appt',
      'Résidents concernés'
    ]];

    const body = this.entries.map((e) => [
      e.nom || '-',
      e.email || '-',
      e.mobile || '-',
      e.nom_borne || '-',
      getHappixTypeLabel(e.type) || '-',
      e.relation || '-',
      e.batiment || '-',
      e.appart || '-',
      (e.residents || []).join(', ') || '-'
    ]);

    doc.setFontSize(16);
    doc.text(title, 40, 40);
    doc.setFontSize(10);
    doc.text(`Généré le : ${new Date().toLocaleString()}`, 40, 60);

    autoTable(doc, {
      head,
      body,
      startY: 80,
      styles: { fontSize: 9, cellPadding: 5, overflow: 'linebreak' },
      headStyles: { fillColor: [22, 160, 133], textColor: 255, fontSize: 9 },
      columnStyles: { 8: { cellWidth: 200, overflow: 'linebreak', valign: 'top' } },
      margin: { left: 20, right: 20 },
      theme: 'striped'
    });

    doc.save('happix-list.pdf');
  }
}
