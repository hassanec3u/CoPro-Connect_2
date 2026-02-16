import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { Resident } from '../models';
import { ResidentsStoreService } from '../residents/residents-store.service';
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
  residents$: Observable<Resident[]>;
  private routerSub?: Subscription;

  constructor(
    public store: ResidentsStoreService,
    private router: Router,
    private sidebar: SidebarService
  ) {
    this.residents$ = this.store.residents$;
  }

  ngOnInit(): void {
    this.reloadData();
    this.routerSub = this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => {
        if (e.urlAfterRedirects.includes('happix')) {
          this.reloadData();
        }
      });
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }

  private reloadData(): void {
    this.store.loadAllResidents();
  }

  openSidebar(): void {
    this.sidebar.open();
  }

  goBack(): void {
    this.router.navigateByUrl('/');
  }
}
