import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { ResidentsStoreService } from '../residents/residents-store.service';
import { Resident } from '../models';
import { SidebarService } from '../core/sidebar.service';
import { HappixListComponent } from '../residents/happix/happix-list.component';

@Component({
  selector: 'app-happix-page',
  standalone: true,
  imports: [CommonModule, HappixListComponent],
  templateUrl: './happix-page.component.html',
  styleUrl: './happix-page.component.css'
})
export class HappixPageComponent implements OnInit, OnDestroy {
  users: Resident[] = [];
  private sub?: Subscription;

  constructor(
    private store: ResidentsStoreService,
    private router: Router,
    private sidebar: SidebarService
  ) {}

  ngOnInit(): void {
    // Charger TOUS les résidents pour la page Happix
    this.store.loadAllResidents();
    
    this.sub = this.store.residents$.subscribe((items: Resident[]) => (this.users = items));
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  openSidebar(): void {
    this.sidebar.open();
  }

  goBack(): void {
    this.router.navigateByUrl('/');
  }
}
