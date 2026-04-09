#!/usr/bin/env node

const fs = require("fs");
const path = require("path");
const { initializeApp, cert, applicationDefault, getApps } = require("firebase-admin/app");
const { getFirestore, Timestamp } = require("firebase-admin/firestore");

const COLLECTION_NAME = "analytics_ride_cancellations";
const DEFAULT_COUNT = 15;
const BOGOTA_TIMEZONE = "America/Bogota";

const USER_PROFILES = [
  { userId: "user_001", fullName: "Andres", email: "andres@uniandes.edu.co", weight: 5 },
  { userId: "user_002", fullName: "Maria", email: "maria@uniandes.edu.co", weight: 4 },
  { userId: "user_003", fullName: "Juan", email: "juan@uniandes.edu.co", weight: 3 },
  { userId: "user_004", fullName: "Valentina", email: "valentina@uniandes.edu.co", weight: 2 },
  { userId: "user_005", fullName: "Camilo", email: "camilo@uniandes.edu.co", weight: 2 },
  { userId: "user_006", fullName: "Paula", email: "paula@uniandes.edu.co", weight: 2 },
  { userId: "user_007", fullName: "Santiago", email: "santiago@uniandes.edu.co", weight: 1 },
  { userId: "user_008", fullName: "Laura", email: "laura@uniandes.edu.co", weight: 1 },
];

async function main() {
  const options = parseArgs(process.argv.slice(2));
  const count = clampCount(options.count);
  const projectId = resolveProjectId(options.projectId);

  initializeFirebase(options.serviceAccount, projectId);
  const db = getFirestore();

  const docs = Array.from({ length: count }, (_, index) =>
    buildCancellationEvent(index),
  );

  const batch = db.batch();
  docs.forEach((doc) => {
    const ref = db.collection(COLLECTION_NAME).doc();
    batch.set(ref, doc);
  });

  await batch.commit();

  console.log(
    `Created ${docs.length} mock cancellation documents in ${COLLECTION_NAME}.`,
  );

  const perUserCount = docs.reduce((acc, doc) => {
    acc[doc.userId] = (acc[doc.userId] ?? 0) + 1;
    return acc;
  }, {});

  console.log("Distribution by user:");
  Object.entries(perUserCount)
    .sort((a, b) => b[1] - a[1])
    .forEach(([userId, total]) => {
      console.log(`- ${userId}: ${total}`);
    });
}

function initializeFirebase(serviceAccountPath, projectId) {
  if (getApps().length > 0) {
    return;
  }

  if (serviceAccountPath) {
    const resolvedPath = resolveServiceAccountPath(serviceAccountPath);
    const serviceAccount = JSON.parse(fs.readFileSync(resolvedPath, "utf8"));

    initializeApp({
      credential: cert(serviceAccount),
      projectId,
    });
    return;
  }

  initializeApp({
    credential: applicationDefault(),
    projectId,
  });
}

function buildCancellationEvent(index) {
  const user = weightedPick(USER_PROFILES);
  const activeRole = weightedPick([
    { value: "driver", weight: 7 },
    { value: "passenger", weight: 3 },
  ]).value;
  const cancellationType = weightedPick([
    { value: "late", weight: 5 },
    { value: "normal", weight: 3 },
    { value: "early", weight: 2 },
  ]).value;
  const cancelledAtDate = buildRecentCancellationDate();
  const hoursBeforeDeparture = sampleHoursBeforeDeparture(cancellationType);
  const cancelledAt = Timestamp.fromDate(cancelledAtDate);

  return {
    userId: user.userId,
    rideId: `seed-ride-${Date.now()}-${index}-${randomInt(1000, 9999)}`,
    activeRole,
    driverName: user.fullName,
    driverEmail: user.email,
    cancellationType,
    cancellationHour: extractHourInBogota(cancelledAtDate),
    cancellationDayOfWeek: extractIsoDayOfWeekInBogota(cancelledAtDate),
    hoursBeforeDeparture,
    cancelledAt,
  };
}

function buildRecentCancellationDate() {
  const now = new Date();
  const daysAgo = weightedPick([
    { value: 0, weight: 1 },
    { value: 1, weight: 2 },
    { value: 2, weight: 3 },
    { value: 3, weight: 3 },
    { value: 4, weight: 2 },
    { value: 5, weight: 2 },
    { value: 6, weight: 2 },
    { value: 7, weight: 1 },
    { value: 10, weight: 1 },
    { value: 12, weight: 1 },
  ]).value;

  const hour = weightedPick([
    { value: 7, weight: 1 },
    { value: 8, weight: 1 },
    { value: 9, weight: 2 },
    { value: 10, weight: 2 },
    { value: 11, weight: 2 },
    { value: 12, weight: 3 },
    { value: 13, weight: 3 },
    { value: 14, weight: 4 },
    { value: 15, weight: 5 },
    { value: 16, weight: 5 },
    { value: 17, weight: 5 },
    { value: 18, weight: 4 },
    { value: 19, weight: 3 },
    { value: 20, weight: 2 },
    { value: 21, weight: 1 },
  ]).value;

  const minute = weightedPick([
    { value: 0, weight: 2 },
    { value: 10, weight: 1 },
    { value: 15, weight: 2 },
    { value: 20, weight: 1 },
    { value: 30, weight: 3 },
    { value: 45, weight: 2 },
    { value: 50, weight: 1 },
  ]).value;

  const date = new Date(now);
  date.setDate(now.getDate() - daysAgo);
  date.setHours(hour, minute, randomInt(0, 59), 0);
  return date;
}

function sampleHoursBeforeDeparture(cancellationType) {
  switch (cancellationType) {
    case "late":
      return roundToTwoDecimals(randomFloat(0.25, 2.99));
    case "normal":
      return roundToTwoDecimals(randomFloat(3, 12));
    default:
      return roundToTwoDecimals(randomFloat(12.1, 36));
  }
}

function extractHourInBogota(date) {
  const formattedHour = new Intl.DateTimeFormat("en-GB", {
    hour: "numeric",
    hour12: false,
    timeZone: BOGOTA_TIMEZONE,
  }).format(date);

  return Number(formattedHour);
}

function extractIsoDayOfWeekInBogota(date) {
  const formattedDay = new Intl.DateTimeFormat("en-US", {
    weekday: "short",
    timeZone: BOGOTA_TIMEZONE,
  }).format(date);

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

function parseArgs(args) {
  const parsed = {
    count: DEFAULT_COUNT,
    serviceAccount: process.env.FIREBASE_SERVICE_ACCOUNT_PATH || "",
    projectId: process.env.FIREBASE_PROJECT_ID || "",
  };

  for (let index = 0; index < args.length; index += 1) {
    const arg = args[index];

    if (arg === "--count" && args[index + 1]) {
      parsed.count = Number(args[index + 1]);
      index += 1;
      continue;
    }

    if ((arg === "--service-account" || arg === "--serviceAccount") && args[index + 1]) {
      parsed.serviceAccount = args[index + 1];
      index += 1;
      continue;
    }

    if ((arg === "--project-id" || arg === "--projectId") && args[index + 1]) {
      parsed.projectId = args[index + 1];
      index += 1;
    }
  }

  return parsed;
}

function resolveProjectId(inputProjectId) {
  if (inputProjectId) {
    return inputProjectId;
  }

  const firebasercPath = path.resolve(process.cwd(), "..", ".firebaserc");
  if (!fs.existsSync(firebasercPath)) {
    throw new Error(
      "Missing project id. Use --project-id wheels-fd8c0 or set FIREBASE_PROJECT_ID.",
    );
  }

  const firebaserc = JSON.parse(fs.readFileSync(firebasercPath, "utf8"));
  const projects = firebaserc.projects || {};
  return projects.default || projects.wheels || Object.values(projects)[0];
}

function resolveServiceAccountPath(inputPath) {
  const resolvedPath = path.resolve(process.cwd(), inputPath);
  if (!fs.existsSync(resolvedPath)) {
    throw new Error(`Service account file not found: ${resolvedPath}`);
  }

  return resolvedPath;
}

function clampCount(count) {
  if (!Number.isFinite(count)) {
    return DEFAULT_COUNT;
  }

  return Math.max(10, Math.min(20, Math.floor(count)));
}

function weightedPick(items) {
  const totalWeight = items.reduce((sum, item) => sum + item.weight, 0);
  let threshold = Math.random() * totalWeight;

  for (const item of items) {
    threshold -= item.weight;
    if (threshold <= 0) {
      return item;
    }
  }

  return items[items.length - 1];
}

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomFloat(min, max) {
  return Math.random() * (max - min) + min;
}

function roundToTwoDecimals(value) {
  return Number(value.toFixed(2));
}

main().catch((error) => {
  console.error("Failed to seed analytics_ride_cancellations.");
  console.error(error);
  process.exitCode = 1;
});
