import { FieldValue, Firestore } from "firebase-admin/firestore";
import { CreateRideLocationUsageAnalyticsDocument } from "../types/trust.js";

const ANALYTICS_CREATE_RIDE_LOCATION_USAGE_COLLECTION =
  "analytics_create_ride_location_usage";

export async function recordCreateRideLocationUsageAnalytics(params: {
  db: Firestore;
  rideId: string;
  driverId: string;
  usedCurrentLocationOrigin: boolean;
  usedCurrentLocationDestination: boolean;
}) {
  const analyticsDoc: CreateRideLocationUsageAnalyticsDocument = {
    rideId: params.rideId,
    driverId: params.driverId,
    usedCurrentLocationOrigin: params.usedCurrentLocationOrigin,
    usedCurrentLocationDestination: params.usedCurrentLocationDestination,
    publishedAt: FieldValue.serverTimestamp(),
  };

  await params.db
    .collection(ANALYTICS_CREATE_RIDE_LOCATION_USAGE_COLLECTION)
    .doc(params.rideId)
    .set(analyticsDoc);
}
