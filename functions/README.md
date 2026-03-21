# Wheels Driver Reliability Score

## Minimal Firestore shape

### `bookings/{bookingId}`

```ts
{
  rideId: string;
  driverId: string;
  passengerId: string;
  status: "requested" | "confirmed" | "canceled" | "completed";
  scheduledStartAt: Timestamp;
  canceledAt?: Timestamp;
  canceledByRole?: "driver" | "passenger" | "system";
  updatedAt?: Timestamp;
}
```

### `rides/{rideId}`

```ts
{
  driverId: string;
  status: "draft" | "published" | "in_progress" | "completed" | "canceled";
  scheduledStartAt?: Timestamp;
  completedAt?: Timestamp;
  updatedAt?: Timestamp;
}
```

### `trustMetrics/{userId}`

```ts
{
  userId: string;
  completedRideCount: number;
  completedRideBonusPoints: number;
  lateCancellationCount: number;
  totalCancellationPenaltyPoints: number;
  cancellationBuckets: {
    gt_12h: number;
    between_3h_12h: number;
    between_1h_3h: number;
    lt_1h: number;
  };
  createdAt: Timestamp;
  updatedAt: Timestamp;
}
```

Subcollection for idempotency:

### `trustMetrics/{userId}/appliedEvents/{eventKey}`

```ts
{
  appliedAt: Timestamp;
  type: "ride_completed" | "driver_late_cancellation";
  bucket?: string;
  penaltyPoints?: number;
}
```

### `trustScores/{userId}`

```ts
{
  userId: string;
  reliabilityScore: number; // 0..100
  breakdown: {
    baseScore: 100;
    completedRideBonusApplied: number; // capped at 10
    cancellationPenaltyApplied: number;
  };
  explanation: string;
  version: "driver_reliability_v1";
  updatedAt: Timestamp;
}
```

## Behavior

- Completed ride:
  - trigger on `rides/{rideId}` update to `status = completed`
  - increment `completedRideCount`
  - recalculate `trustScores/{driverId}`

- Driver late cancellation:
  - trigger on `bookings/{bookingId}` update to `status = canceled`
  - require `canceledByRole = driver`
  - classify penalty by hours before `scheduledStartAt`
  - increment penalty metrics
  - recalculate `trustScores/{driverId}`

## Design note

`appliedEvents` is included to keep the system defendable and idempotent when Cloud Functions retries the same event.
