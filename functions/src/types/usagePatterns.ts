export interface AppOpenEventDocument {
  uid: string;
  email: string;
  openedAt: number;
  hourOfDay: number;
  minuteOfHour: number;
  dayOfWeek: number;
  timezone: string;
  openSource: string;
}

export interface UserUsagePatternDocument {
  uid: string;
  email: string;
  timezone: string;
  hourCounts: Record<string, number>;
  halfHourCounts: Record<string, number>;
  totalOpenCount: number;
  peakHour: number | null;
  peakHalfHourBucket: number | null;
  peakScore: number;
  lastOpenedAt: number | null;
  updatedAt?: unknown;
}
