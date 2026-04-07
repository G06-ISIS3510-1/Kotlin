import { Firestore, Timestamp } from "firebase-admin/firestore";
import {
  DestinationEventDocument,
  UserDestinationInsightsDocument,
} from "../types/trust.js";

const USER_DESTINATION_INSIGHTS_COLLECTION = "user_destination_insights";
const APPLIED_EVENTS_SUBCOLLECTION = "appliedEvents";
const MAX_TOP_DESTINATIONS = 3;

export async function updateUserDestinationInsights(params: {
  db: Firestore;
  eventId: string;
  event: DestinationEventDocument;
}): Promise<void> {
  const { db, eventId, event } = params;

  await db.runTransaction(async (transaction) => {
    const insightsRef = db
      .collection(USER_DESTINATION_INSIGHTS_COLLECTION)
      .doc(event.userId);
    const appliedEventRef = insightsRef
      .collection(APPLIED_EVENTS_SUBCOLLECTION)
      .doc(eventId);

    const appliedEventSnapshot = await transaction.get(appliedEventRef);
    if (appliedEventSnapshot.exists) {
      return;
    }

    const insightsSnapshot = await transaction.get(insightsRef);
    const now = Timestamp.now();
    const currentInsights = toInsightsDocument(
      event.userId,
      insightsSnapshot.data(),
      now,
    );

    const nextDestinationCounts = upsertDestinationCount(
      currentInsights.destinationCounts,
      event.destinationName,
    );

    const updatedInsights: UserDestinationInsightsDocument = {
      userId: event.userId,
      totalBookingsTracked: currentInsights.totalBookingsTracked + 1,
      destinationCounts: nextDestinationCounts,
      topDestinations: nextDestinationCounts
        .sort((left, right) => {
          if (right.bookingCount != left.bookingCount) {
            return right.bookingCount - left.bookingCount;
          }

          return left.destinationName.localeCompare(right.destinationName);
        })
        .slice(0, MAX_TOP_DESTINATIONS)
        .map((destination, index) => ({
          destinationName: destination.destinationName,
          bookingCount: destination.bookingCount,
          rank: index + 1,
        })),
      createdAt: currentInsights.createdAt,
      updatedAt: now,
    };

    transaction.set(insightsRef, updatedInsights, { merge: true });
    transaction.set(appliedEventRef, {
      appliedAt: now,
      type: "ride_booked_destination_insight",
      destinationName: event.destinationName,
    });
  });
}

function toInsightsDocument(
  userId: string,
  data: FirebaseFirestore.DocumentData | undefined,
  now: Timestamp,
): UserDestinationInsightsDocument {
  return {
    userId,
    totalBookingsTracked: data?.totalBookingsTracked ?? 0,
    destinationCounts: Array.isArray(data?.destinationCounts)
      ? data.destinationCounts.map(
        (item: FirebaseFirestore.DocumentData) => ({
          destinationName: item?.destinationName ?? "Unknown destination",
          bookingCount: item?.bookingCount ?? 0,
        }),
      )
      : [],
    topDestinations: Array.isArray(data?.topDestinations)
      ? data.topDestinations.map(
        (item: FirebaseFirestore.DocumentData, index: number) => ({
          destinationName: item?.destinationName ?? `Unknown ${index + 1}`,
          bookingCount: item?.bookingCount ?? 0,
          rank: item?.rank ?? index + 1,
        }),
      )
      : [],
    createdAt: data?.createdAt ?? now,
    updatedAt: now,
  };
}

function upsertDestinationCount(
  destinations: UserDestinationInsightsDocument["destinationCounts"],
  destinationName: string,
): UserDestinationInsightsDocument["destinationCounts"] {
  const existingDestination = destinations.find(
    (item) => item.destinationName === destinationName,
  );

  if (!existingDestination) {
    return [
      ...destinations,
      {
        destinationName,
        bookingCount: 1,
      },
    ];
  }

  return destinations.map((item) =>
    item.destinationName == destinationName
      ? {
        ...item,
        bookingCount: item.bookingCount + 1,
      }
      : item,
  );
}
