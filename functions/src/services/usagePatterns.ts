import { FieldValue, Firestore, Transaction } from "firebase-admin/firestore";
import {
  AppOpenEventDocument,
  UserUsagePatternDocument,
} from "../types/usagePatterns.js";

interface UpdateUserUsagePatternParams {
  db: Firestore;
  event: AppOpenEventDocument;
}

const HOUR_BUCKET_COUNT = 24;
const HALF_HOUR_BUCKET_COUNT = 48;

export async function updateUserUsagePattern({
  db,
  event,
}: UpdateUserUsagePatternParams): Promise<void> {
  // Each user has exactly one aggregate usage document.
  // We update it in a Firestore transaction so concurrent app opens do not
  // overwrite each other or lose increments.
  const document = db.collection(USER_USAGE_PATTERNS_COLLECTION).doc(event.uid);

  await db.runTransaction(async (transaction) => {
    // Read the current aggregate state first. If the document does not exist
    // yet, the normalizer below will seed every field with safe defaults.
    const snapshot = await transaction.get(document);
    const currentPattern = normalizeUsagePattern(
      snapshot.data() as Partial<UserUsagePatternDocument> | undefined,
      event,
    );

    // Increment the bucket that corresponds to the hour when the app was
    // opened. This is the coarse usage signal.
    const nextHourCounts = incrementHourCounts(
      currentPattern.hourCounts,
      event.hourOfDay,
    );

    // Increment the half-hour bucket. This is the more precise signal used to
    // infer the user's peak usage window.
    const nextHalfHourCounts = incrementHalfHourCounts(
      currentPattern.halfHourCounts,
      event.hourOfDay,
      event.minuteOfHour,
    );

    // Keep a simple total so the backend can reason about how much data has
    // been collected for this user.
    const nextTotalOpenCount = currentPattern.totalOpenCount + 1;

    // Recalculate the peak half-hour bucket after the new event is included.
    // In this implementation, "peak" means the bucket with the highest count.
    const nextPeak = calculatePeakHalfHourBucket(nextHalfHourCounts);

    // Merge the updated aggregate back into Firestore.
    // The document remains backend-owned; the client should not write it.
    transaction.set(
      document,
      {
        uid: event.uid,
        email: event.email,
        timezone: event.timezone,
        hourCounts: nextHourCounts,
        halfHourCounts: nextHalfHourCounts,
        totalOpenCount: nextTotalOpenCount,
        peakHour: Math.floor(nextPeak.bucket / 2),
        peakHalfHourBucket: nextPeak.bucket,
        peakScore: nextPeak.score,
        lastOpenedAt: event.openedAt,
        // Server timestamp keeps the backend write time authoritative.
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true },
    );
  });
}

function normalizeUsagePattern(
  current: Partial<UserUsagePatternDocument> | undefined,
  event: AppOpenEventDocument,
): UserUsagePatternDocument {
  // Build a complete, safe in-memory version of the document before
  // incrementing counts. This avoids having to handle missing fields in the
  // update logic itself.
  return {
    uid: current?.uid ?? event.uid,
    email: current?.email ?? event.email,
    timezone: current?.timezone ?? event.timezone,
    hourCounts: normalizeCountMap(current?.hourCounts, HOUR_BUCKET_COUNT),
    halfHourCounts: normalizeCountMap(
      current?.halfHourCounts,
      HALF_HOUR_BUCKET_COUNT,
    ),
    totalOpenCount: current?.totalOpenCount ?? 0,
    peakHour: current?.peakHour ?? null,
    peakHalfHourBucket: current?.peakHalfHourBucket ?? null,
    peakScore: current?.peakScore ?? 0,
    lastOpenedAt: current?.lastOpenedAt ?? null,
    lastPeakNotificationSentAt: current?.lastPeakNotificationSentAt ?? null,
    lastPeakNotificationWindowKey:
      current?.lastPeakNotificationWindowKey ?? null,
    updatedAt: current?.updatedAt,
  };
}

function normalizeCountMap(
  source: Record<string, number> | undefined,
  bucketCount: number,
): Record<string, number> {
  // Firestore documents may have missing buckets on first write or after old
  // schema versions. Normalize to a dense map so every bucket is present.
  const normalized: Record<string, number> = {};

  for (let bucket = 0; bucket < bucketCount; bucket += 1) {
    normalized[bucket.toString()] = source?.[bucket.toString()] ?? 0;
  }

  return normalized;
}

function incrementHourCounts(
  counts: Record<string, number>,
  hourOfDay: number,
): Record<string, number> {
  // Hour counts give a coarse overview of the user's behavior by clock hour.
  return {
    ...counts,
    [hourOfDay.toString()]: (counts[hourOfDay.toString()] ?? 0) + 1,
  };
}

function incrementHalfHourCounts(
  counts: Record<string, number>,
  hourOfDay: number,
  minuteOfHour: number,
): Record<string, number> {
  // Split the hour into two 30-minute windows and increment the matching one.
  const halfHourBucket = resolveHalfHourBucket(hourOfDay, minuteOfHour);

  return {
    ...counts,
    [halfHourBucket.toString()]: (counts[halfHourBucket.toString()] ?? 0) + 1,
  };
}

function resolveHalfHourBucket(hourOfDay: number, minuteOfHour: number): number {
  // Each hour is split into two windows:
  // 00-29 => even bucket, 30-59 => odd bucket.
  return hourOfDay * 2 + (minuteOfHour >= 30 ? 1 : 0);
}

function calculatePeakHalfHourBucket(counts: Record<string, number>): {
  bucket: number;
  score: number;
} {
  // Find the bucket with the highest number of opens.
  // If there is a tie, the earliest bucket wins because we only replace the
  // current best when a strictly larger score is found.
  let bestBucket = 0;
  let bestScore = -1;

  for (let bucket = 0; bucket < HALF_HOUR_BUCKET_COUNT; bucket += 1) {
    const score = counts[bucket.toString()] ?? 0;

    if (score > bestScore) {
      bestScore = score;
      bestBucket = bucket;
    }
  }

  return {
    bucket: bestBucket,
    score: bestScore,
  };
}

const USER_USAGE_PATTERNS_COLLECTION = "user_usage_patterns";
