import { Timestamp } from "firebase-admin/firestore";

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
  scheduledStartAt?: Timestamp;
  completedAt?: Timestamp;
  canceledAt?: Timestamp;
  canceledByRole?: "driver" | "passenger" | "system";
  updatedAt?: Timestamp;
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
