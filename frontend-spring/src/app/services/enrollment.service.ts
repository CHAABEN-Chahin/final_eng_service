import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Enrollment } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createEnrollment(data: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/enrollments`, data);
  }

  getEnrollmentsByParent(parentId: string): Observable<Enrollment[]> {
    return this.http.get<any>(`${this.apiUrl}/enrollments/parent/${parentId}`).pipe(
      map(response => {
        const enrollments = response.enrollments || response || [];
        return enrollments.map((e: any) => this.mapEnrollment(e));
      })
    );
  }

  getEnrollmentsByNursery(nurseryId: string): Observable<Enrollment[]> {
    return this.http.get<any>(`${this.apiUrl}/enrollments/nursery/${nurseryId}`).pipe(
      map(response => {
        const enrollments = response.enrollments || response || [];
        return enrollments.map((e: any) => this.mapEnrollment(e));
      })
    );
  }

  acceptEnrollment(enrollmentId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/enrollments/${enrollmentId}/accept`, {});
  }

  rejectEnrollment(enrollmentId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/enrollments/${enrollmentId}/reject`, {});
  }

  private mapEnrollment(e: any): Enrollment {
    const child = e.child || {};
    const parent = e.parent || {};
    const nursery = e.nursery || {};

    return {
      id: e.id?.toString() || e.enrollmentId?.toString() || '',
      childName: child.childName || child.name || e.childName || e.child_name || '',
      childAge: parseInt(child.age || e.childAge || e.child_age) || 0,
      parentName: parent.name || e.parentName || e.parent_name || '',
      parentPhone: parent.phone || e.parentPhone || e.parent_phone || '',
      nurseryId: nursery.id || e.nurseryId || e.nursery_id || '',
      nurseryName: nursery.name || e.nurseryName || e.nursery_name || '',
      startDate: e.startDate || e.start_date || '',
      endDate: e.endDate || e.end_date || '',
      status: e.status || 'pending',
      medicalNotes: e.medicalNotes || e.medical_notes || '',
      dietaryRestrictions: e.dietaryRestrictions || e.dietary_restrictions || '',
      notes: e.notes || '',
      createdAt: e.createdAt || e.created_at || ''
    };
  }
}
