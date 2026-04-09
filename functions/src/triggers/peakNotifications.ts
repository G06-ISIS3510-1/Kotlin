import { logger } from "firebase-functions";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { db, messaging } from "../app.js";
import { sendPeakUsageNotifications } from "../services/peakNotifications.js";

export const sendPeakUsageNotificationsOnSchedule = onSchedule(
  {
    schedule: "every 30 minutes",
    timeZone: "UTC",
  },
  async () => {
    logger.info("Running scheduled peak-usage notification job");

    await sendPeakUsageNotifications({
      db,
      messaging,
    });
  },
);
