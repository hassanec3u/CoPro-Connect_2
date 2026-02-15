import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { Resident } from '../../models';

@Component({
  selector: 'app-export-pdf-button',
  standalone: true,
  imports: [CommonModule],
  template: `<button class="btn-save" (click)="handleExport()">📄 Exporter PDF</button>`
})
export class ExportPdfButtonComponent {
  @Input() users: Resident[] = [];

  handleExport(): void {
    const doc = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
    const title = 'Liste des résidents';
    const head = [[
      'Lot', 'Bât', 'Appt', 'Étage', 'Cave',
      'Statut', 'Propriétaire',
      'Mobile Propriétaire', 'Email Propriétaire',
      'Occupants'
    ]];

    const body = this.users.map((u) => {
      const occupantsFormatted = (u.occupants || [])
        .map((c) => [c.nom, c.mobile, c.email].filter(Boolean).join(' • '))
        .join('\n\n') || '-';
      return [
        u.lot_id || '-',
        u.batiment || '-',
        u.porte || '-',
        u.etage || '-',
        u.cave_id || '-',
        u.statut_lot || '-',
        u.proprietaire_nom || '-',
        u.proprietaire_mobile || '-',
        u.proprietaire_email || '-',
        occupantsFormatted
      ];
    });

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
      columnStyles: { 9: { cellWidth: 300, overflow: 'linebreak', valign: 'top' } },
      margin: { left: 20, right: 20 },
      theme: 'striped'
    });

    doc.save('residents-list.pdf');
  }
}
