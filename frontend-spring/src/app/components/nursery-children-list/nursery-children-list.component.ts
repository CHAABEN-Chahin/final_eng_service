import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NurseryService } from '../../services/nursery.service';

@Component({
  selector: 'app-nursery-children-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './nursery-children-list.component.html',
  styleUrl: './nursery-children-list.component.scss'
})
export class NurseryChildrenListComponent implements OnInit {
  enrolledChildren: any[] = [];
  isLoading = true;

  constructor(
    private authService: AuthService,
    private nurseryService: NurseryService,
    private router: Router
  ) {}

  ngOnInit() {
    const user = this.authService.currentUser;
    if (user) {
      this.nurseryService.getNurseriesByOwner(user.id).subscribe({
        next: (nurseries) => {
          if (nurseries.length > 0) {
            this.nurseryService.getEnrolledChildren(nurseries[0].id).subscribe({
              next: (response) => {
                // Spring Boot returns {parents: [{parentName, children: [{childName, age, ...}]}]}
                const parents = response.parents || [];
                const flatChildren: any[] = [];
                for (const parent of parents) {
                  for (const child of (parent.children || [])) {
                    flatChildren.push({
                      ...child,
                      parentName: parent.parentName,
                      parentEmail: parent.parentEmail,
                      parentPhone: parent.parentPhone,
                      name: child.childName || child.name
                    });
                  }
                }
                this.enrolledChildren = flatChildren.length > 0 ? flatChildren
                  : (response.children || response.enrolledChildren || response || []);
                this.isLoading = false;
              },
              error: () => this.isLoading = false
            });
          } else {
            this.isLoading = false;
          }
        },
        error: () => this.isLoading = false
      });
    }
  }

  openEspaceEnfant(child: any) {
    const name = child.child_name || child.name || 'Enfant';
    const age = child.child_age || child.age || '';
    sessionStorage.setItem('currentChildName', name);
    sessionStorage.setItem('currentChildAge', age.toString());
    this.router.navigate(['/nursery/espace-enfant', child.childId || child.id], {
      state: { childName: name, childAge: age }
    });
  }
}
