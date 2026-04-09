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
  const document = db.collection(USER_USAGE_PATTERNS_COLLECTION).doc(event.uid);

  await db.runTransaction(async (transaction) => {
    const snapshot = await transaction.get(document);
    const currentPattern = normalizeUsagePattern(
      snapshot.data() as Partial<UserUsagePatternDocument> | undefined,
      event,
    );

    const nextHourCounts = incrementHourCounts(
      currentPattern.hourCounts,
      event.hourOfDay,
    );
    const nextHalfHourCounts = incrementHalfHourCounts(
      currentPattern.halfHourCounts,
      event.hourOfDay,
      event.minuteOfHour,
    );
    const nextTotalOpenCount = currentPattern.totalOpenCount + 1;
    const nextPeak = calculatePeakHalfHourBucket(nextHalfHourCounts);

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
