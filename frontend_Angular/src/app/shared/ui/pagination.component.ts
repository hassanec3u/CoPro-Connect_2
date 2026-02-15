import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.css'
})
export class PaginationComponent {
  @Input() totalPages = 0;
  @Input() currentPage = 1;
  @Output() pageChange = new EventEmitter<number>();

  get visiblePages(): Array<number | string> {
    if (this.totalPages <= 7) {
      return Array.from({ length: this.totalPages }, (_, i) => i + 1);
    }

    if (this.currentPage <= 3) {
      return [1, 2, 3, 4, 5, '...', this.totalPages];
    }

    if (this.currentPage >= this.totalPages - 2) {
      return [1, '...', this.totalPages - 4, this.totalPages - 3, this.totalPages - 2, this.totalPages - 1, this.totalPages];
    }

    return [1, '...', this.currentPage - 1, this.currentPage, this.currentPage + 1, '...', this.totalPages];
  }

  selectPage(page: number | string): void {
    if (typeof page === 'number') {
      this.pageChange.emit(page);
    }
  }
}
