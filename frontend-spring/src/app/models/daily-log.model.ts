export interface DailyLog {
  id: string;
  childId: string;
  nurseryId?: string;
  educatorId?: string;
  educatorName?: string;
  date: string;
  mood: number;
  appetite: number;
  energy: number;
  sociability: number;
  concentration: number;
  autonomy: number;
  sleep: number;
  motorSkills: number;
  tags: string[];
  remarks?: string;
  isInternal: boolean;
  aiSummary?: string;
  createdAt?: string;
}
