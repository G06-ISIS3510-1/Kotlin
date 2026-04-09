import { logger } from "firebase-functions";
import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import { db, messaging } from "../app.js";
import { sendPeakUsageNotification } from "../services/peakNotifications.js";
import { UserUsagePatternDocument } from "../types/usagePatterns.js";

export const onUserUsagePatternUpdated = onDocumentUpdated(
  "user_usage_patterns/{uid}",
  async (event) => {
    const before = event.data?.before.data() as UserUsagePatternDocument | undefined;
    const after = event.data?.after.data() as UserUsagePatternDocument | undefined;

    if (!after) {
      return;
    }

    // Demo shortcut: if the predicted bucket changes and now matches the
    // user's current local window, send the push immediately instead of waiting
    // for the 30-minute scheduler.
    if (before?.peakHalfHourBucket === after.peakHalfHourBucket) {
      return;
    }

    if (after.peakHalfHourBucket == null) {
      return;
    }

    try {
      await sendPeakUsageNotification({
        db,
        messaging,
        usagePattern: after,
      });
    } catch (error) {
      logger.error("Failed to send immediate peak notification", {
        uid: after.uid,
        error,
      });
    }
  },
);
