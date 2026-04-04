import { FieldValue, Firestore, Timestamp } from "firebase-admin/firestore";
import {
  CancellationAnalyticsType,
  RideCancellationAnalyticsDocument,
  UserProfileDocument,
} from "../types/trust.js";

const ANALYTICS_RIDE_CANCELLATIONS_COLLECTION = "analytics_ride_cancellations";
const USERS_COLLECTION = "users";
const BOGOTA_TIMEZONE = "America/Bogota";

export function calculateHoursBeforeDeparture(params: {
  scheduledStartAt: Timestamp;
  canceledAt: Timestamp;
}): number {
  const millisecondsUntilRide =
    params.scheduledStartAt.toMillis() - params.canceledAt.toMillis();

  return Number((millisecondsUntilRide / (1000 * 60 * 60)).toFixed(2));
}

export function classifyCancellationType(
  hoursBeforeDeparture: number,
): CancellationAnalyticsType {
  if (hoursBeforeDeparture > 12) {
    return "early";
  }

  if (hoursBeforeDeparture >= 3) {
    return "normal";
  }

  return "late";
}

export async function recordRideCancellationAnalytics(params: {
  db: Firestore;
  rideId: string;
  userId: string;
  role: "driver" | "passenger";
  scheduledStartAt: Timestamp;
  canceledAt: Timestamp;
}): Promise<void> {
  const { db, rideId, userId, role, scheduledStartAt, canceledAt } = params;
  const hoursBeforeDeparture = calculateHoursBeforeDeparture({
    scheduledStartAt,
    canceledAt,
  });
  const driverProfile = await readUserProfile(db, userId);

  const analyticsDoc: RideCancellationAnalyticsDocument = {
    userId,
    rideId,
    role,
    cancelledAt: FieldValue.serverTimestamp(),
    cancellationHour: extractHourInBogota(canceledAt),
    cancellationDayOfWeek: extractIsoDayOfWeekInBogota(canceledAt),
    hoursBeforeDeparture,
    cancellationType: classifyCancellationType(hoursBeforeDeparture),
    driverName: driverProfile?.fullName,
    driverEmail: driverProfile?.email,
  };

  await db.collection(ANALYTICS_RIDE_CANCELLATIONS_COLLECTION).add(analyticsDoc);
}

async function readUserProfile(
  db: Firestore,
  userId: string,
): Promise<UserProfileDocument | null> {
  const snapshot = await db.collection(USERS_COLLECTION).doc(userId).get();

  if (!snapshot.exists) {
    return null;
  }

  return snapshot.data() as UserProfileDocument;
}

function extractHourInBogota(timestamp: Timestamp): number {
  const formattedHour = new Intl.DateTimeFormat("en-GB", {
    hour: "numeric",
    hour12: false,
    timeZone: BOGOTA_TIMEZONE,
  }).format(timestamp.toDate());

  return Number(formattedHour);
}

function extractIsoDayOfWeekInBogota(timestamp: Timestamp): number {
  const formattedDay = new Intl.DateTimeFormat("en-US", {
    weekday: "short",
    timeZone: BOGOTA_TIMEZONE,
  }).format(timestamp.toDate());

  switch (formattedDay) {
    case "Mon":
      return 1;
    case "Tue":
      return 2;
    case "Wed":
      return 3;
    case "Thu":
      return 4;
    case "Fri":
      return 5;
    case "Sat":
      return 6;
    default:
      return 7;
  }
}
