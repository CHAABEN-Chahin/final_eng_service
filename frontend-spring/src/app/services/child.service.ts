import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Child } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ChildService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getChildrenByParent(parentId: string): Observable<Child[]> {
    return this.http.get<any>(`${this.apiUrl}/parents/${parentId}/children`).pipe(
      map(response => {
        const children = response.children || response || [];
        return children.map((c: any) => ({
          id: c.id?.toString() || '',
          name: c.name || '',
          age: parseInt(c.age) || 0,
          photo: c.photoUrl || c.photo || '',
          nurseryId: c.nurseryId || c.nursery_id || ''
        }));
      })
    );
  }
}
