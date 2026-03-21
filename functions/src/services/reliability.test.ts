import test from "node:test";
import assert from "node:assert/strict";
import {
  calculateDriverReliabilityScore,
  classifyCancellationPenalty,
  DRIVER_COMPLETED_RIDE_BONUS_CAP,
} from "./reliability.js";

test("classifyCancellationPenalty returns -2 for cancellations above 12 hours", () => {
  const result = classifyCancellationPenalty(24);

  assert.equal(result.bucket, "gt_12h");
  assert.equal(result.penaltyPoints, 2);
});

test("classifyCancellationPenalty returns -5 between 3 and 12 hours", () => {
  const result = classifyCancellationPenalty(6);

  assert.equal(result.bucket, "between_3h_12h");
  assert.equal(result.penaltyPoints, 5);
});

test("classifyCancellationPenalty returns -10 between 1 and 3 hours", () => {
  const result = classifyCancellationPenalty(2);

  assert.equal(result.bucket, "between_1h_3h");
  assert.equal(result.penaltyPoints, 10);
});

test("classifyCancellationPenalty returns -15 under 1 hour", () => {
  const result = classifyCancellationPenalty(0.5);

  assert.equal(result.bucket, "lt_1h");
  assert.equal(result.penaltyPoints, 15);
});

test("calculateDriverReliabilityScore caps completed ride bonus at 10", () => {
  const result = calculateDriverReliabilityScore({
    completedRideCount: DRIVER_COMPLETED_RIDE_BONUS_CAP + 8,
    totalCancellationPenaltyPoints: 0,
  });

  assert.equal(result.completedRideBonusApplied, 10);
  assert.equal(result.score, 100);
});

test("calculateDriverReliabilityScore applies cancellation penalties and clamps to zero", () => {
  const result = calculateDriverReliabilityScore({
    completedRideCount: 0,
    totalCancellationPenaltyPoints: 150,
  });

  assert.equal(result.cancellationPenaltyApplied, 150);
  assert.equal(result.score, 0);
});

test("calculateDriverReliabilityScore follows the v1 formula", () => {
  const result = calculateDriverReliabilityScore({
    completedRideCount: 4,
    totalCancellationPenaltyPoints: 12,
  });

  assert.equal(result.score, 92);
  assert.equal(result.completedRideBonusApplied, 4);
  assert.equal(result.cancellationPenaltyApplied, 12);
});
