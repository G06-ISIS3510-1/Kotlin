import { Firestore, Timestamp } from "firebase-admin/firestore";
import { UserCancellationMetricsDocument } from "../types/trust.js";

const USER_CANCELLATION_METRICS_COLLECTION = "user_cancellation_metrics";
const APPLIED_EVENTS_SUBCOLLECTION = "appliedEvents";

export async function updateUserCancellationMetrics(params: {
  db: Firestore;
  userId: string;
  eventId: string;
  hoursBeforeDeparture: number;
}): Promise<void> {
  const { db, userId, eventId, hoursBeforeDeparture } = params;

  await db.runTransaction(async (transaction) => {
    const metricsRef = db.collection(USER_CANCELLATION_METRICS_COLLECTION).doc(userId);
    const eventRef = metricsRef.collection(APPLIED_EVENTS_SUBCOLLECTION).doc(eventId);

    const eventDoc = await transaction.get(eventRef);
    if (eventDoc.exists) {
      return;
    }

    const metricsDoc = await transaction.get(metricsRef);
    const now = Timestamp.now();
    const currentMetrics = toMetricsDocument(userId, metricsDoc.data(), now);
    const nextCancellationCount = currentMetrics.cancellationCount + 1;
    const nextTotalHours =
      currentMetrics.totalHoursBeforeCancellation + hoursBeforeDeparture;
    const nextAverageHours = Number(
      (nextTotalHours / nextCancellationCount).toFixed(2),
    );

    const updatedMetrics: UserCancellationMetricsDocument = {
      ...currentMetrics,
      cancellationCount: nextCancellationCount,
      totalHoursBeforeCancellation: Number(nextTotalHours.toFixed(2)),
      averageHoursBeforeCancellation: nextAverageHours,
      updatedAt: now,
    };

    transaction.set(metricsRef, updatedMetrics, { merge: true });
    transaction.set(eventRef, {
      appliedAt: now,
      type: "ride_cancellation_metrics",
      hoursBeforeDeparture,
    });
  });
}

function toMetricsDocument(
  userId: string,
  data: FirebaseFirestore.DocumentData | undefined,
  now: Timestamp,
): UserCancellationMetricsDocument {
  return {
    userId,
    cancellationCount: data?.cancellationCount ?? 0,
    totalHoursBeforeCancellation: data?.totalHoursBeforeCancellation ?? 0,
    averageHoursBeforeCancellation: data?.averageHoursBeforeCancellation ?? 0,
    createdAt: data?.createdAt ?? now,
    updatedAt: now,
  };
}
