import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { User } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = environment.apiUrl;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const stored = localStorage.getItem('currentUser');
    if (stored) {
      this.currentUserSubject.next(JSON.parse(stored));
    }
  }

  get currentUser(): User | null {
    return this.currentUserSubject.value;
  }

  login(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/auth/login`, { email, password }).pipe(
      tap(response => {
        if (response.user) {
          const user: User = {
            id: response.user.id,
            name: response.user.name,
            email: response.user.email,
            type: response.user.type || response.user.user_type,
            phone: response.user.phone
          };
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
        }
      })
    );
  }

  register(name: string, email: string, password: string, userType: string, phone: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/auth/register`, {
      name, email, password, user_type: userType, phone
    }).pipe(
      tap(response => {
        if (response.user) {
          const user: User = {
            id: response.user.id,
            name: response.user.name,
            email: response.user.email,
            type: response.user.type || response.user.user_type,
            phone: response.user.phone
          };
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  getUserById(userId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/users/${userId}`);
  }

  updateUser(userId: string, data: { name: string; phone: string }): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/users/${userId}`, data).pipe(
      tap(response => {
        if (response.user) {
          const currentUser = this.currentUserSubject.value;
          if (currentUser) {
            const updatedUser: User = {
              id: currentUser.id,
              name: response.user.name || currentUser.name,
              email: currentUser.email,
              type: currentUser.type,
              phone: response.user.phone || currentUser.phone
            };
            localStorage.setItem('currentUser', JSON.stringify(updatedUser));
            this.currentUserSubject.next(updatedUser);
          }
        }
      })
    );
  }

  isLoggedIn(): boolean {
    return this.currentUserSubject.value !== null;
  }
}
