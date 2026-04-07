import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions";
import { db } from "../app.js";
import { updateUserDestinationInsights } from "../services/destinationInsights.js";
import { DestinationEventDocument } from "../types/trust.js";

export const onDestinationEventCreated = onDocumentCreated(
  "analytics_destination_events/{eventId}",
  async (event) => {
    const destinationEvent =
      event.data?.data() as DestinationEventDocument | undefined;

    if (!destinationEvent) {
      logger.warn("Destination analytics event is empty", {
        eventId: event.params.eventId,
      });
      return;
    }

    if (destinationEvent.eventType !== "ride_booked") {
      return;
    }

    if (!destinationEvent.userId || !destinationEvent.destinationName) {
      logger.warn("Destination analytics event is missing required fields", {
        eventId: event.params.eventId,
      });
      return;
    }

    await updateUserDestinationInsights({
      db,
      eventId: event.params.eventId,
      event: destinationEvent,
    });
  },
);
