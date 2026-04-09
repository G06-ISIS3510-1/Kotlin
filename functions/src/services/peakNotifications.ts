import { FieldValue, Firestore } from "firebase-admin/firestore";
import { Messaging } from "firebase-admin/messaging";
import { logger } from "firebase-functions";
import {
  UserDocument,
  UserUsagePatternDocument,
} from "../types/usagePatterns.js";
import { PeakNotificationPayload } from "../types/peakNotifications.js";

interface SendPeakUsageNotificationsParams {
  db: Firestore;
  messaging: Messaging;
  nowUtc?: Date;
}

interface SendPeakUsageNotificationParams {
  db: Firestore;
  messaging: Messaging;
  usagePattern: UserUsagePatternDocument;
  nowUtc?: Date;
}

export async function sendPeakUsageNotifications({
  db,
  messaging,
  nowUtc = new Date(),
}: SendPeakUsageNotificationsParams): Promise<void> {
  const snapshot = await db
    .collection(USER_USAGE_PATTERNS_COLLECTION)
    .where("peakHalfHourBucket", ">=", 0)
    .get();

  for (const document of snapshot.docs) {
    await sendPeakUsageNotification({
      db,
      messaging,
      usagePattern: document.data() as UserUsagePatternDocument,
      nowUtc,
    });
  }
}

export async function sendPeakUsageNotification({
  db,
  messaging,
  usagePattern,
  nowUtc = new Date(),
}: SendPeakUsageNotificationParams): Promise<void> {
  if (!usagePattern.uid || !usagePattern.timezone) {
    return;
  }

  const localWindow = resolveLocalWindow(nowUtc, usagePattern.timezone);
  if (!localWindow) {
    logger.warn("Skipping user with invalid timezone", {
      uid: usagePattern.uid,
      timezone: usagePattern.timezone,
    });
    return;
  }

  if (usagePattern.peakHalfHourBucket !== localWindow.bucket) {
    return;
  }

  // Throttle duplicate sends for the same local day + half-hour window.
  if (usagePattern.lastPeakNotificationWindowKey === localWindow.windowKey) {
    return;
  }

  const userSnapshot = await db
    .collection(USERS_COLLECTION)
    .doc(usagePattern.uid)
    .get();
  const user = userSnapshot.data() as UserDocument | undefined;
  const tokens = sanitizeTokens(user?.fcmTokens);

  if (tokens.length === 0) {
    return;
  }

  const response = await messaging.sendEachForMulticast({
    tokens,
    notification: buildNotificationContent(),
    data: buildNotificationData(usagePattern),
    android: {
      priority: "high",
    },
  });

  if (response.successCount === 0) {
    logger.warn("Peak notification failed for all tokens", {
      uid: usagePattern.uid,
      failureCount: response.failureCount,
    });
    return;
  }

  await db.collection(USER_USAGE_PATTERNS_COLLECTION)
    .doc(usagePattern.uid)
    .set(
      {
        lastPeakNotificationSentAt: nowUtc.getTime(),
        lastPeakNotificationWindowKey: localWindow.windowKey,
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true },
    );
}

function resolveLocalWindow(
  date: Date,
  timezone: string,
): { bucket: number; windowKey: string } | null {
  try {
    const formatter = new Intl.DateTimeFormat("en-CA", {
      timeZone: timezone,
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      hourCycle: "h23",
    });

    const parts = formatter.formatToParts(date);
    const year = getPart(parts, "year");
    const month = getPart(parts, "month");
    const day = getPart(parts, "day");
    const hour = Number(getPart(parts, "hour"));
    const minute = Number(getPart(parts, "minute"));
    const bucket = hour * 2 + (minute >= 30 ? 1 : 0);

    return {
      bucket,
      windowKey: `${year}-${month}-${day}_${bucket}`,
    };
  } catch {
    return null;
  }
}

function getPart(
  parts: Intl.DateTimeFormatPart[],
  type: Intl.DateTimeFormatPartTypes,
): string {
  return parts.find((part) => part.type === type)?.value ?? "";
}

function sanitizeTokens(tokens: string[] | undefined): string[] {
  return [...new Set((tokens ?? []).filter((token) => token.trim().length > 0))];
}

function buildNotificationContent(): { title: string; body: string } {
  // Keep the visible copy simple and deterministic for the demo.
  return {
    title: "It's a great time to ride",
    body: "You usually open Wheels around now. Check the latest rides.",
  };
}

function buildNotificationData(
  usagePattern: UserUsagePatternDocument,
): PeakNotificationPayload {
  const content = buildNotificationContent();

  return {
    ...content,
    type: "habit_based_notification",
    uid: usagePattern.uid,
    peakHalfHourBucket: String(usagePattern.peakHalfHourBucket ?? 0),
    timezone: usagePattern.timezone,
  };
}

const USER_USAGE_PATTERNS_COLLECTION = "user_usage_patterns";
const USERS_COLLECTION = "users";
