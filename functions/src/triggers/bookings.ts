import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions";
import { db } from "../app.js";
import { applyDriverLateCancellationPenalty } from "../services/trustRepository.js";
import { classifyCancellationPenalty } from "../services/reliability.js";
import { BookingDocument } from "../types/trust.js";

export const onBookingStatusUpdated = onDocumentUpdated(
  "bookings/{bookingId}",
  async (event) => {
    const before = event.data?.before.data() as BookingDocument | undefined;
    const after = event.data?.after.data() as BookingDocument | undefined;

    if (!before || !after) {
      return;
    }

    if (before.status === after.status) {
      return;
    }

    if (after.status !== "canceled") {
      return;
    }

    if (after.canceledByRole !== "driver") {
      return;
    }

    if (!after.canceledAt || !after.scheduledStartAt) {
      logger.warn("Booking cancellation missing timestamps", {
        bookingId: event.params.bookingId,
      });
      return;
    }

    const millisecondsUntilRide =
      after.scheduledStartAt.toMillis() - after.canceledAt.toMillis();
    const hoursBeforeRide = millisecondsUntilRide / (1000 * 60 * 60);
    const penalty = classifyCancellationPenalty(hoursBeforeRide);

    await applyDriverLateCancellationPenalty({
      db,
      userId: after.driverId,
      eventId: `booking_canceled:${event.params.bookingId}`,
      bucket: penalty.bucket,
      penaltyPoints: penalty.penaltyPoints,
    });
  },
);
