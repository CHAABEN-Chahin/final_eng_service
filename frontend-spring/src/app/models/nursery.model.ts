import { Review } from './review.model';

export interface Nursery {
  id: string;
  name: string;
  address: string;
  city?: string;
  postalCode?: string;
  distance: number;
  rating: number;
  reviewCount: number;
  price: number;
  availableSpots: number;
  totalSpots: number;
  hours: string;
  photo: string;
  description: string;
  activities: string[];
  facilities?: string[];
  staff: number;
  ageRange: string;
  phone?: string;
  email?: string;
  ownerId?: string;
  reviews?: Review[];
}
