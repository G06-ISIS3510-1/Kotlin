import { FieldValue, Firestore } from "firebase-admin/firestore";
import { PublishFromDraftAnalyticsDocument } from "../types/trust.js";

const ANALYTICS_PUBLISH_FROM_DRAFT_COLLECTION = "analytics_publish_from_draft";

export async function recordPublishFromDraftAnalytics(params: {
  db: Firestore;
  rideId: string;
  driverId: string;
  publishedFromDraft: boolean;
  sourceDraftId?: string | null;
}) {
  const analyticsDoc: PublishFromDraftAnalyticsDocument = {
    rideId: params.rideId,
    driverId: params.driverId,
    publishedFromDraft: params.publishedFromDraft,
    sourceDraftId: params.sourceDraftId ?? null,
    publishedAt: FieldValue.serverTimestamp(),
  };

  await params.db
    .collection(ANALYTICS_PUBLISH_FROM_DRAFT_COLLECTION)
    .doc(params.rideId)
    .set(analyticsDoc);
}
