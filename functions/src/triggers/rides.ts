import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions";
import { db } from "../app.js";
import {
  applyCompletedRideBonus,
  applyDriverLateCancellationPenalty,
} from "../services/trustRepository.js";
import { classifyCancellationPenalty } from "../services/reliability.js";
import { RideDocument } from "../types/trust.js";

export const onRideCompleted = onDocumentUpdated(
  "rides/{rideId}",
  async (event) => {
    const before = event.data?.before.data() as RideDocument | undefined;
    const after = event.data?.after.data() as RideDocument | undefined;

    if (!before || !after) {
      return;
    }

    if (before.status === "completed" || after.status !== "completed") {
      return;
    }

    await applyCompletedRideBonus({
      db,
      userId: after.driverId,
      eventId: `ride_completed:${event.params.rideId}`,
    });
  },
);

export const onRideCanceled = onDocumentUpdated(
  "rides/{rideId}",
  async (event) => {
    const before = event.data?.before.data() as RideDocument | undefined;
    const after = event.data?.after.data() as RideDocument | undefined;

    if (!before || !after) {
      return;
    }

    if (before.status === after.status) {
      return;
    }

    if (after.status !== "canceled" || after.canceledByRole !== "driver") {
      return;
    }

    if (!after.canceledAt || !after.scheduledStartAt) {
      logger.warn("Ride cancellation missing timestamps", {
        rideId: event.params.rideId,
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
      eventId: `ride_canceled:${event.params.rideId}`,
      bucket: penalty.bucket,
      penaltyPoints: penalty.penaltyPoints,
    });
  },
);
