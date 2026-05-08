import { FieldValue, Timestamp } from "firebase-admin/firestore";

export type BookingStatus =
  | "requested"
  | "confirmed"
  | "canceled"
  | "completed";

export type RideStatus =
  | "draft"
  | "published"
  | "in_progress"
  | "completed"
  | "canceled";

export type CancellationBucket =
  | "gt_12h"
  | "between_3h_12h"
  | "between_1h_3h"
  | "lt_1h";

export type CancellationAnalyticsType = "early" | "normal" | "late";

export interface BookingDocument {
  rideId: string;
  driverId: string;
  passengerId: string;
  status: BookingStatus;
  scheduledStartAt: Timestamp;
  canceledAt?: Timestamp;
  canceledByRole?: "driver" | "passenger" | "system";
  updatedAt?: Timestamp;
}

export interface RideDocument {
  driverId: string;
  status: RideStatus;
  departureAt?: Timestamp;
  destination?: string;
  usedCurrentLocationOrigin?: boolean;
  usedCurrentLocationDestination?: boolean;
  scheduledStartAt?: Timestamp;
  completedAt?: Timestamp;
  canceledAt?: Timestamp;
  canceledByRole?: "driver" | "passenger" | "system";
  updatedAt?: Timestamp;
}

export interface CreateRideLocationUsageAnalyticsDocument {
  rideId: string;
  driverId: string;
  usedCurrentLocationOrigin: boolean;
  usedCurrentLocationDestination: boolean;
  publishedAt: FieldValue;
}

export interface UserProfileDocument {
  fullName?: string;
  email?: string;
  role?: "driver" | "passenger" | "admin" | string;
}

export interface UserCancellationMetricsDocument {
  userId: string;
  cancellationCount: number;
  totalHoursBeforeCancellation: number;
  averageHoursBeforeCancellation: number;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

export interface RideCancellationAnalyticsDocument {
  userId: string;
  rideId: string;
  activeRole: "driver" | "passenger";
  cancelledAt: FieldValue;
  cancellationHour: number;
  cancellationDayOfWeek: number;
  hoursBeforeDeparture: number;
  cancellationType: CancellationAnalyticsType;
  driverName?: string;
  driverEmail?: string;
}

export interface DestinationEventDocument {
  eventType: "ride_booked" | "search_performed";
  userId: string;
  rideId?: string;
  destinationName: string;
  destinationKey: string;
  createdAt: Timestamp;
}

export interface UserDestinationInsightsDocument {
  userId: string;
  totalBookingsTracked: number;
  destinationCounts: Array<{
    destinationName: string;
    bookingCount: number;
  }>;
  topDestinations: Array<{
    destinationName: string;
    bookingCount: number;
    rank: number;
  }>;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

export interface TrustMetricsDocument {
  userId: string;
  completedRideCount: number;
  completedRideBonusPoints: number;
  lateCancellationCount: number;
  totalCancellationPenaltyPoints: number;
  cancellationBuckets: Record<CancellationBucket, number>;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

export interface TrustScoreBreakdown {
  baseScore: number;
  completedRideBonusApplied: number;
  cancellationPenaltyApplied: number;
}

export interface TrustScoreDocument {
  userId: string;
  reliabilityScore: number;
  breakdown: TrustScoreBreakdown;
  explanation: string;
  version: "driver_reliability_v1";
  updatedAt: Timestamp;
}
