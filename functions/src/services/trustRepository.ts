import { Firestore, Timestamp } from "firebase-admin/firestore";
import {
  CancellationBucket,
  TrustMetricsDocument,
} from "../types/trust.js";
import { buildTrustScoreDocument } from "./reliability.js";

const TRUST_METRICS_COLLECTION = "trustMetrics";
const TRUST_SCORES_COLLECTION = "trustScores";
const APPLIED_EVENTS_SUBCOLLECTION = "appliedEvents";

export async function applyCompletedRideBonus(params: {
  db: Firestore;
  userId: string;
  eventId: string;
}): Promise<void> {
  const { db, userId, eventId } = params;

  await db.runTransaction(async (transaction) => {
    const eventRef = db
      .collection(TRUST_METRICS_COLLECTION)
      .doc(userId)
      .collection(APPLIED_EVENTS_SUBCOLLECTION)
      .doc(eventId);

    const eventDoc = await transaction.get(eventRef);
    if (eventDoc.exists) {
      return;
    }

    const metricsRef = db.collection(TRUST_METRICS_COLLECTION).doc(userId);
    const scoreRef = db.collection(TRUST_SCORES_COLLECTION).doc(userId);
    const metricsDoc = await transaction.get(metricsRef);
    const now = Timestamp.now();
    const currentMetrics = toMetricsDocument(userId, metricsDoc.data(), now);

    const updatedMetrics: TrustMetricsDocument = {
      ...currentMetrics,
      completedRideCount: currentMetrics.completedRideCount + 1,
      completedRideBonusPoints: currentMetrics.completedRideBonusPoints + 1,
      updatedAt: now,
    };

    transaction.set(metricsRef, updatedMetrics, { merge: true });
    transaction.set(
      scoreRef,
      buildTrustScoreDocument(userId, updatedMetrics, now),
      { merge: true },
    );
    transaction.set(eventRef, { appliedAt: now, type: "ride_completed" });
  });
}

export async function applyDriverLateCancellationPenalty(params: {
  db: Firestore;
  userId: string;
  eventId: string;
  bucket: CancellationBucket;
  penaltyPoints: number;
}): Promise<void> {
  const { db, userId, eventId, bucket, penaltyPoints } = params;

  await db.runTransaction(async (transaction) => {
    const eventRef = db
      .collection(TRUST_METRICS_COLLECTION)
      .doc(userId)
      .collection(APPLIED_EVENTS_SUBCOLLECTION)
      .doc(eventId);

    const eventDoc = await transaction.get(eventRef);
    if (eventDoc.exists) {
      return;
    }

    const metricsRef = db.collection(TRUST_METRICS_COLLECTION).doc(userId);
    const scoreRef = db.collection(TRUST_SCORES_COLLECTION).doc(userId);
    const metricsDoc = await transaction.get(metricsRef);
    const now = Timestamp.now();
    const currentMetrics = toMetricsDocument(userId, metricsDoc.data(), now);

    const updatedMetrics: TrustMetricsDocument = {
      ...currentMetrics,
      lateCancellationCount: currentMetrics.lateCancellationCount + 1,
      totalCancellationPenaltyPoints:
        currentMetrics.totalCancellationPenaltyPoints + penaltyPoints,
      cancellationBuckets: {
        ...currentMetrics.cancellationBuckets,
        [bucket]: currentMetrics.cancellationBuckets[bucket] + 1,
      },
      updatedAt: now,
    };

    transaction.set(metricsRef, updatedMetrics, { merge: true });
    transaction.set(
      scoreRef,
      buildTrustScoreDocument(userId, updatedMetrics, now),
      { merge: true },
    );
    transaction.set(eventRef, {
      appliedAt: now,
      type: "driver_late_cancellation",
      bucket,
      penaltyPoints,
    });
  });
}

function toMetricsDocument(
  userId: string,
  data: FirebaseFirestore.DocumentData | undefined,
  now: Timestamp,
): TrustMetricsDocument {
  return {
    userId,
    completedRideCount: data?.completedRideCount ?? 0,
    completedRideBonusPoints: data?.completedRideBonusPoints ?? 0,
    lateCancellationCount: data?.lateCancellationCount ?? 0,
    totalCancellationPenaltyPoints: data?.totalCancellationPenaltyPoints ?? 0,
    cancellationBuckets: {
      gt_12h: data?.cancellationBuckets?.gt_12h ?? 0,
      between_3h_12h: data?.cancellationBuckets?.between_3h_12h ?? 0,
      between_1h_3h: data?.cancellationBuckets?.between_1h_3h ?? 0,
      lt_1h: data?.cancellationBuckets?.lt_1h ?? 0,
    },
    createdAt: data?.createdAt ?? now,
    updatedAt: now,
  };
}
