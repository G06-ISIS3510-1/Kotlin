import { Timestamp } from "firebase-admin/firestore";
import {
  CancellationBucket,
  TrustMetricsDocument,
  TrustScoreDocument,
} from "../types/trust.js";

export const DRIVER_RELIABILITY_VERSION = "driver_reliability_v1" as const;
export const DRIVER_RELIABILITY_BASE_SCORE = 100;
export const DRIVER_COMPLETED_RIDE_BONUS_CAP = 10;

export interface CancellationPenaltyResult {
  bucket: CancellationBucket;
  penaltyPoints: number;
  hoursBeforeRide: number;
}

export interface ScoreCalculationInput {
  completedRideCount: number;
  totalCancellationPenaltyPoints: number;
}

export interface ScoreCalculationResult {
  score: number;
  completedRideBonusApplied: number;
  cancellationPenaltyApplied: number;
  explanation: string;
}

export function classifyCancellationPenalty(
  hoursBeforeRide: number,
): CancellationPenaltyResult {
  if (hoursBeforeRide > 12) {
    return {
      bucket: "gt_12h",
      penaltyPoints: 2,
      hoursBeforeRide,
    };
  }

  if (hoursBeforeRide > 3) {
    return {
      bucket: "between_3h_12h",
      penaltyPoints: 5,
      hoursBeforeRide,
    };
  }

  if (hoursBeforeRide > 1) {
    return {
      bucket: "between_1h_3h",
      penaltyPoints: 10,
      hoursBeforeRide,
    };
  }

  return {
    bucket: "lt_1h",
    penaltyPoints: 15,
    hoursBeforeRide,
  };
}

export function calculateDriverReliabilityScore(
  input: ScoreCalculationInput,
): ScoreCalculationResult {
  const completedRideBonusApplied = Math.min(
    input.completedRideCount,
    DRIVER_COMPLETED_RIDE_BONUS_CAP,
  );

  const cancellationPenaltyApplied = Math.max(
    input.totalCancellationPenaltyPoints,
    0,
  );

  const rawScore =
    DRIVER_RELIABILITY_BASE_SCORE +
    completedRideBonusApplied -
    cancellationPenaltyApplied;

  const score = clamp(rawScore, 0, 100);

  return {
    score,
    completedRideBonusApplied,
    cancellationPenaltyApplied,
    explanation:
      "Score = clamp(100 + completedRideBonus - cancellationPenalty, 0, 100)",
  };
}

export function buildTrustScoreDocument(
  userId: string,
  metrics: Pick<
    TrustMetricsDocument,
    "completedRideCount" | "totalCancellationPenaltyPoints"
  >,
  updatedAtIso: Timestamp,
): TrustScoreDocument {
  const result = calculateDriverReliabilityScore({
    completedRideCount: metrics.completedRideCount,
    totalCancellationPenaltyPoints: metrics.totalCancellationPenaltyPoints,
  });

  return {
    userId,
    reliabilityScore: result.score,
    breakdown: {
      baseScore: DRIVER_RELIABILITY_BASE_SCORE,
      completedRideBonusApplied: result.completedRideBonusApplied,
      cancellationPenaltyApplied: result.cancellationPenaltyApplied,
    },
    explanation: result.explanation,
    version: DRIVER_RELIABILITY_VERSION,
    updatedAt: updatedAtIso,
  };
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}
