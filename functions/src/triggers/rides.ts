import { onDocumentCreated, onDocumentUpdated } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions";
import { db } from "../app.js";
import {
  applyCompletedRideBonus,
  applyDriverLateCancellationPenalty,
} from "../services/trustRepository.js";
import { classifyCancellationPenalty } from "../services/reliability.js";
import {
  calculateHoursBeforeDeparture,
  recordRideCancellationAnalytics,
} from "../services/cancellationAnalytics.js";
import { recordCreateRideLocationUsageAnalytics } from "../services/createRideLocationUsageAnalytics.js";
import { recordPublishFromDraftAnalytics } from "../services/publishFromDraftAnalytics.js";
import { updateUserCancellationMetrics } from "../services/cancellationMetrics.js";
import { RideDocument } from "../types/trust.js";

function resolveDepartureAt(ride: RideDocument) {
  return ride.departureAt ?? ride.scheduledStartAt;
}

export const onRidePublished = onDocumentCreated(
  "rides/{rideId}",
  async (event) => {
    const ride = event.data?.data() as RideDocument | undefined;

    if (!ride) {
      logger.warn("Ride publish analytics event is empty", {
        rideId: event.params.rideId,
      });
      return;
    }

    if (!ride.driverId) {
      return;
    }

    const normalizedStatus = ride.status?.trim().toLowerCase();
    if (normalizedStatus !== "open" && normalizedStatus !== "published") {
      return;
    }

    try {
      await recordCreateRideLocationUsageAnalytics({
        db,
        rideId: event.params.rideId,
        driverId: ride.driverId,
        usedCurrentLocationOrigin: ride.usedCurrentLocationOrigin ?? false,
        usedCurrentLocationDestination:
          ride.usedCurrentLocationDestination ?? false,
      });
    } catch (error) {
      logger.error("Failed to record create ride current location analytics", {
        rideId: event.params.rideId,
        driverId: ride.driverId,
        error,
      });
    }

    try {
      await recordPublishFromDraftAnalytics({
        db,
        rideId: event.params.rideId,
        driverId: ride.driverId,
        publishedFromDraft: ride.publishedFromDraft ?? false,
        sourceDraftId: ride.sourceDraftId ?? null,
      });
    } catch (error) {
      logger.error("Failed to record publish from draft analytics", {
        rideId: event.params.rideId,
        driverId: ride.driverId,
        error,
      });
    }
  },
);

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

    const departureAt = resolveDepartureAt(after);

    if (!after.canceledAt || !departureAt) {
      logger.warn("Ride cancellation missing timestamps", {
        rideId: event.params.rideId,
      });
      return;
    }

    const eventId = `ride_canceled:${event.params.rideId}`;
    const hoursBeforeRide = calculateHoursBeforeDeparture({
      departureAt,
      canceledAt: after.canceledAt,
    });
    const penalty = classifyCancellationPenalty(hoursBeforeRide);

    await applyDriverLateCancellationPenalty({
      db,
      userId: after.driverId,
      eventId,
      bucket: penalty.bucket,
      penaltyPoints: penalty.penaltyPoints,
    });

    try {
      await updateUserCancellationMetrics({
        db,
        userId: after.driverId,
        eventId,
        hoursBeforeDeparture: hoursBeforeRide,
      });
    } catch (error) {
      logger.error("Failed to update user cancellation metrics", {
        rideId: event.params.rideId,
        driverId: after.driverId,
        error,
      });
    }

    try {
      await recordRideCancellationAnalytics({
        db,
        eventId,
        rideId: event.params.rideId,
        userId: after.driverId,
        activeRole: "driver",
        departureAt,
        canceledAt: after.canceledAt,
      });
    } catch (error) {
      logger.error("Failed to record ride cancellation analytics", {
        rideId: event.params.rideId,
        driverId: after.driverId,
        error,
      });
    }
  },
);
