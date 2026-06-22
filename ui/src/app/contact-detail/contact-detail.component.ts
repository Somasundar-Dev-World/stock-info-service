import { Component } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from "@angular/common";


interface TeamMemberDetail {
  name: string;
  designation: string;
  role: string;
  bio: string;
  email: string;
  linkedIn: string;
  detailedBio: string;
}

@Component({
  selector: 'app-contact-detail',
  standalone: true,
  imports: [ CommonModule, RouterModule], 
  templateUrl: './contact-detail.component.html',
  styleUrl: './contact-detail.component.css'
})
export class ContactDetailComponent {

  memberDetails: TeamMemberDetail | null = null;
  loading: boolean = true;
  error: string | null = null;

  constructor( private route: ActivatedRoute, private http: HttpClient )
  { 

  }

  ngOnInit() {
    const name = this.route.snapshot.paramMap.get('name');
    if (name) {
      this.fetchMemberDetails(name);
    }
  }

  fetchMemberDetails(name: string) {
    this.loading = true;
    this.error = null;


    const apiURL =  `https://stock-info-service.onrender.com/api/v1/team/${name}`;
    this.http.get<TeamMemberDetail>(apiURL).subscribe({
      next: (data) => {
        this.memberDetails = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to fetch member details';
        this.loading = false;
      }
    });
  }

}
