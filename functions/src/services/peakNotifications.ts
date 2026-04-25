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
  // entry point used by the scheduled Cloud Function.
  // It scans all users with a predicted peak window and evaluates each one
  // against the current UTC time.
  const snapshot = await db
    .collection(USER_USAGE_PATTERNS_COLLECTION)
    .where("peakHalfHourBucket", ">=", 0)
    .get();

  for (const document of snapshot.docs) {
    // Reuse the single-user sender so the scheduler and any other caller
    // follow the exact same delivery rules.
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
  // A valid user id and timezone are required before we can match the user to
  // a local half-hour window and find their device tokens.
  if (!usagePattern.uid || !usagePattern.timezone) {
    return;
  }

  // Convert the current time from UTC into the user's local timezone so the
  // comparison uses the user's actual clock, not the server clock.
  const localWindow = resolveLocalWindow(nowUtc, usagePattern.timezone);
  if (!localWindow) {
    logger.warn("Skipping user with invalid timezone", {
      uid: usagePattern.uid,
      timezone: usagePattern.timezone,
    });
    return;
  }

  // Only notify when the user's predicted peak bucket matches the current
  // local half-hour bucket.
  if (usagePattern.peakHalfHourBucket !== localWindow.bucket) {
    return;
  }

  // Prevent duplicate notifications in the same calendar day and half-hour
  // window. This protects against scheduler retries and overlapping runs.
  if (usagePattern.lastPeakNotificationWindowKey === localWindow.windowKey) {
    return;
  }

  // Device tokens live on the users document, not inside the usage profile.
  // That keeps delivery tied to the latest registered devices.
  const userSnapshot = await db
    .collection(USERS_COLLECTION)
    .doc(usagePattern.uid)
    .get();
  const user = userSnapshot.data() as UserDocument | undefined;
  const tokens = sanitizeTokens(user?.fcmTokens);

  // Without tokens there is nowhere to send the push, so stop here.
  if (tokens.length === 0) {
    return;
  }

  // Send the same notification to every valid token for this user.
  const response = await messaging.sendEachForMulticast({
    tokens,
    notification: buildNotificationContent(),
    data: buildNotificationData(usagePattern),
    android: {
      priority: "high",
    },
  });

  // If every token failed, do not mark the send as successful in Firestore.
  if (response.successCount === 0) {
    logger.warn("Peak notification failed for all tokens", {
      uid: usagePattern.uid,
      failureCount: response.failureCount,
    });
    return;
  }

  // Record the last successful send so the same window is not notified again.
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
  // Use Intl.DateTimeFormat with an IANA timezone so the bucket calculation
  // matches the user's local time rather than the server's timezone.
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

    // The window key is a stable identifier for one day + one half-hour slot.
    return {
      bucket,
      windowKey: `${year}-${month}-${day}_${bucket}`,
    };
  } catch {
    // Invalid timezone strings should fail safely and simply skip delivery.
    return null;
  }
}

function getPart(
  parts: Intl.DateTimeFormatPart[],
  type: Intl.DateTimeFormatPartTypes,
): string {
  // Extract a specific time part from the formatted output.
  return parts.find((part) => part.type === type)?.value ?? "";
}

function sanitizeTokens(tokens: string[] | undefined): string[] {
  // Remove blanks and duplicates so FCM only receives valid destination tokens.
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
  // Mirror the visible content in the data payload and include the metadata
  // the Android app uses to recognize the notification type.
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
