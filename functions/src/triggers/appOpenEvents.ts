import { logger } from "firebase-functions";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { db } from "../app.js";
import { updateUserUsagePattern } from "../services/usagePatterns.js";
import { AppOpenEventDocument } from "../types/usagePatterns.js";

export const onAppOpenEventCreated = onDocumentCreated(
  "app_open_events/{eventId}",
  async (event) => {
    const appOpenEvent = event.data?.data() as AppOpenEventDocument | undefined;

    if (!appOpenEvent) {
      logger.warn("App open event is empty", {
        eventId: event.params.eventId,
      });
      return;
    }

    if (!isValidAppOpenEvent(appOpenEvent)) {
      logger.warn("App open event is missing required fields", {
        eventId: event.params.eventId,
        uid: appOpenEvent.uid,
      });
      return;
    }

    await updateUserUsagePattern({
      db,
      event: appOpenEvent,
    });
  },
);

function isValidAppOpenEvent(event: AppOpenEventDocument): boolean {
  return Boolean(
    event.uid &&
      event.email &&
      typeof event.openedAt === "number" &&
      typeof event.hourOfDay === "number" &&
      typeof event.minuteOfHour === "number" &&
      typeof event.dayOfWeek === "number" &&
      event.timezone &&
      event.openSource,
  );
}
