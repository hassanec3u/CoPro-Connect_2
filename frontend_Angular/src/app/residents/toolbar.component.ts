import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BATIMENTS, STATUTS } from '../utils';

@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './toolbar.component.html',
  styleUrl: './toolbar.component.css'
})
export class ToolbarComponent {
  @Input() searchTerm = '';
  @Input() filterBat = 'Tous';
  @Input() filterStatus = 'Tous';
  @Output() searchChange = new EventEmitter<string>();
  @Output() filterBatChange = new EventEmitter<string>();
  @Output() filterStatusChange = new EventEmitter<string>();

  batiments = BATIMENTS;
  statuts = STATUTS;
}
