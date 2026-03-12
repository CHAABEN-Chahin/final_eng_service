export interface Enrollment {
  id: string;
  childName: string;
  childAge: number;
  parentName: string;
  parentPhone: string;
  nurseryId: string;
  nurseryName?: string;
  startDate: string;
  endDate?: string;
  status: string;
  medicalNotes?: string;
  dietaryRestrictions?: string;
  notes?: string;
  createdAt?: string;
}
