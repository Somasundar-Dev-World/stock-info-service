import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StockService } from './stock.service';
import { StockResponse } from './stock.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  query: string = '';
  stock: StockResponse | null = null;
  loading: boolean = false;
  error: string | null = null;

  constructor(private stockService: StockService) {}

  searchStock() {
    if (!this.query.trim()) {
      this.error = 'Please enter a ticker symbol or company name.';
      return;
    }

    this.loading = true;
    this.error = null;
    this.stock = null;

    this.stockService.lookupStock(this.query).subscribe({
      next: (data) => {
        this.stock = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message;
        this.loading = false;
      }
    });
  }

  isPositive(change: number): boolean {
    return change >= 0;
  }
}
