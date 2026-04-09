// Firebase Cloud Messaging `data` payloads must be string maps.
export interface PeakNotificationPayload extends Record<string, string> {
  title: string;
  body: string;
  type: "habit_based_notification";
  uid: string;
  peakHalfHourBucket: string;
  timezone: string;
}
