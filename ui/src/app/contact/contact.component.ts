import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface TeamMember {
  name: string;
  designation: string;
  role: string;
  bio: string;
}

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './contact.component.html',
  styleUrls: ['./contact.component.css']
})
export class ContactComponent implements OnInit {
  teamMembers: TeamMember[] = [];
  loading: boolean = true;
  error: string | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetchTeam();
  }

  fetchTeam() {
    this.loading = true;
    this.error = null;
    
    this.http.get<TeamMember[]>('https://stock-info-service.onrender.com/api/v1/team')
      .subscribe({
        next: (data) => {
          this.teamMembers = data;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to load team data. Please try again later.';
          this.loading = false;
          console.error(err);
        }
      });
  }
}
