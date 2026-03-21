import { initializeApp } from "firebase-admin/app";
import { getFirestore, Timestamp } from "firebase-admin/firestore";
import assert from "node:assert/strict";

initializeApp();

const db = getFirestore();

async function main(): Promise<void> {
  const driverId = "driver_test_001";
  const rideId = "ride_test_001";
  const bookingId = "booking_test_001";

  await cleanup(driverId, rideId, bookingId);

  await db.collection("rides").doc(rideId).set({
    driverId,
    status: "published",
    scheduledStartAt: Timestamp.fromDate(new Date("2026-03-21T18:00:00.000Z")),
    updatedAt: Timestamp.now(),
  });

  await db.collection("rides").doc(rideId).update({
    status: "completed",
    completedAt: Timestamp.now(),
    updatedAt: Timestamp.now(),
  });

  await waitForCondition(async () => {
    const scoreSnap = await db.collection("trustScores").doc(driverId).get();
    return scoreSnap.exists;
  });

  await db.collection("bookings").doc(bookingId).set({
    rideId,
    driverId,
    passengerId: "passenger_test_001",
    status: "confirmed",
    scheduledStartAt: Timestamp.fromDate(new Date("2026-03-21T18:00:00.000Z")),
    updatedAt: Timestamp.now(),
  });

  await db.collection("bookings").doc(bookingId).update({
    status: "canceled",
    canceledByRole: "driver",
    canceledAt: Timestamp.fromDate(new Date("2026-03-21T16:30:00.000Z")),
    updatedAt: Timestamp.now(),
  });

  await waitForCondition(async () => {
    const metricsSnap = await db.collection("trustMetrics").doc(driverId).get();
    const data = metricsSnap.data();
    return (data?.lateCancellationCount ?? 0) >= 1;
  });

  const metricsSnap = await db.collection("trustMetrics").doc(driverId).get();
  const scoreSnap = await db.collection("trustScores").doc(driverId).get();

  const metrics = metricsSnap.data();
  const score = scoreSnap.data();

  assert.ok(metrics, "trustMetrics document should exist");
  assert.ok(score, "trustScores document should exist");

  assert.equal(metrics.completedRideCount, 1);
  assert.equal(metrics.lateCancellationCount, 1);
  assert.equal(metrics.totalCancellationPenaltyPoints, 10);
  assert.equal(metrics.cancellationBuckets.between_1h_3h, 1);

  assert.equal(score.reliabilityScore, 91);
  assert.equal(score.breakdown.baseScore, 100);
  assert.equal(score.breakdown.completedRideBonusApplied, 1);
  assert.equal(score.breakdown.cancellationPenaltyApplied, 10);

  console.log("Integration test passed.");
  console.log(JSON.stringify({ metrics, score }, null, 2));
}

async function cleanup(
  driverId: string,
  rideId: string,
  bookingId: string,
): Promise<void> {
  await Promise.all([
    db.collection("rides").doc(rideId).delete().catch(() => undefined),
    db.collection("bookings").doc(bookingId).delete().catch(() => undefined),
    db.collection("trustScores").doc(driverId).delete().catch(() => undefined),
    deleteTrustMetrics(driverId),
  ]);
}

async function deleteTrustMetrics(userId: string): Promise<void> {
  const metricsRef = db.collection("trustMetrics").doc(userId);
  const appliedEvents = await metricsRef.collection("appliedEvents").get();
  await Promise.all(appliedEvents.docs.map((doc) => doc.ref.delete()));
  await metricsRef.delete().catch(() => undefined);
}

async function waitForCondition(
  condition: () => Promise<boolean>,
  timeoutMs = 8000,
  intervalMs = 250,
): Promise<void> {
  const startedAt = Date.now();
  while (Date.now() - startedAt < timeoutMs) {
    if (await condition()) {
      return;
    }
    await new Promise((resolve) => setTimeout(resolve, intervalMs));
  }

  throw new Error("Timed out waiting for emulator-side updates");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
