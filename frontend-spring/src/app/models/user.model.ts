export type UserType = 'parent' | 'nursery';

export interface User {
  id: string;
  name: string;
  email: string;
  type: UserType;
  phone?: string;
}
