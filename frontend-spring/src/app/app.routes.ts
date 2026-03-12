import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth/auth.component';
import { WelcomeComponent } from './components/welcome/welcome.component';
import { ParentDashboardComponent } from './components/parent-dashboard/parent-dashboard.component';
import { NurseryDashboardComponent } from './components/nursery-dashboard/nursery-dashboard.component';
import { NurserySearchComponent } from './components/nursery-search/nursery-search.component';
import { NurseryDetailsComponent } from './components/nursery-details/nursery-details.component';
import { EnrollmentComponent } from './components/enrollment/enrollment.component';
import { ChatListComponent } from './components/chat-list/chat-list.component';
import { ChatComponent } from './components/chat/chat.component';
import { NotificationsComponent } from './components/notifications/notifications.component';
import { ParentChildrenComponent } from './components/parent-children/parent-children.component';
import { ParentEnrollmentsComponent } from './components/parent-enrollments/parent-enrollments.component';
import { ParentPaymentsComponent } from './components/parent-payments/parent-payments.component';
import { ParentReviewsComponent } from './components/parent-reviews/parent-reviews.component';
import { NurserySetupComponent } from './components/nursery-setup/nursery-setup.component';
import { ManageEnrolledComponent } from './components/manage-enrolled/manage-enrolled.component';
import { NurseryChildrenListComponent } from './components/nursery-children-list/nursery-children-list.component';
import { NurseryProgramComponent } from './components/nursery-program/nursery-program.component';
import { NurseryPerformanceComponent } from './components/nursery-performance/nursery-performance.component';
import { NurseryStatisticsComponent } from './components/nursery-statistics/nursery-statistics.component';
import { NurseryFinancialTrackingComponent } from './components/nursery-financial-tracking/nursery-financial-tracking.component';
import { NurserySettingsComponent } from './components/nursery-settings/nursery-settings.component';
import { EspaceEnfantComponent } from './components/espace-enfant/espace-enfant.component';
import { EducatorLogFormComponent } from './components/educator-log-form/educator-log-form.component';
import { ParentDailyStoryComponent } from './components/parent-daily-story/parent-daily-story.component';
import { ChildChatComponent } from './components/child-chat/child-chat.component';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/welcome', pathMatch: 'full' },
  { path: 'welcome', component: WelcomeComponent },
  { path: 'auth', component: AuthComponent },
  { path: 'parent/dashboard', component: ParentDashboardComponent, canActivate: [AuthGuard] },
  { path: 'parent/children', component: ParentChildrenComponent, canActivate: [AuthGuard] },
  { path: 'parent/espace-enfant/:childId', component: EspaceEnfantComponent, canActivate: [AuthGuard] },
  { path: 'parent/espace-enfant/:childId/story', component: ParentDailyStoryComponent, canActivate: [AuthGuard] },
  { path: 'parent/espace-enfant/:childId/chat', component: ChildChatComponent, canActivate: [AuthGuard] },
  { path: 'parent/enrollments', component: ParentEnrollmentsComponent, canActivate: [AuthGuard] },
  { path: 'parent/payments', component: ParentPaymentsComponent, canActivate: [AuthGuard] },
  { path: 'parent/reviews', component: ParentReviewsComponent, canActivate: [AuthGuard] },
  { path: 'nursery/dashboard', component: NurseryDashboardComponent, canActivate: [AuthGuard] },
  { path: 'nursery/setup', component: NurserySetupComponent, canActivate: [AuthGuard] },
  { path: 'nursery/manage-enrolled', component: ManageEnrolledComponent, canActivate: [AuthGuard] },
  { path: 'nursery/children-list', component: NurseryChildrenListComponent, canActivate: [AuthGuard] },
  { path: 'nursery/espace-enfant/:childId', component: EspaceEnfantComponent, canActivate: [AuthGuard] },
  { path: 'nursery/espace-enfant/:childId/log', component: EducatorLogFormComponent, canActivate: [AuthGuard] },
  { path: 'nursery/espace-enfant/:childId/chat', component: ChildChatComponent, canActivate: [AuthGuard] },
  { path: 'nursery/program', component: NurseryProgramComponent, canActivate: [AuthGuard] },
  { path: 'nursery/performance', component: NurseryPerformanceComponent, canActivate: [AuthGuard] },
  { path: 'nursery/statistics', component: NurseryStatisticsComponent, canActivate: [AuthGuard] },
  { path: 'nursery/financial', component: NurseryFinancialTrackingComponent, canActivate: [AuthGuard] },
  { path: 'nursery/settings', component: NurserySettingsComponent, canActivate: [AuthGuard] },
  { path: 'search', component: NurserySearchComponent, canActivate: [AuthGuard] },
  { path: 'nursery/details/:id', component: NurseryDetailsComponent, canActivate: [AuthGuard] },
  { path: 'enrollment/:nurseryId', component: EnrollmentComponent, canActivate: [AuthGuard] },
  { path: 'chat', component: ChatListComponent, canActivate: [AuthGuard] },
  { path: 'chat/:conversationId', component: ChatComponent, canActivate: [AuthGuard] },
  { path: 'notifications', component: NotificationsComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '/welcome' }
];
