#!/usr/bin/env node

const fs = require("fs");
const path = require("path");
const { initializeApp, cert, applicationDefault, getApps } = require("firebase-admin/app");
const { getFirestore, Timestamp } = require("firebase-admin/firestore");

const COLLECTION_NAME = "analytics_create_ride_location_usage";
const DEFAULT_COUNT = 50;

const DRIVER_IDS = [
  "driver_001",
  "driver_002",
  "driver_003",
  "driver_004",
  "driver_005",
  "driver_006",
  "driver_007",
  "driver_008",
];

async function main() {
  const options = parseArgs(process.argv.slice(2));
  const count = clampCount(options.count);
  const projectId = resolveProjectId(options.projectId);

  initializeFirebase(options.serviceAccount, projectId);
  const db = getFirestore();

  const docs = Array.from({ length: count }, (_, index) => buildLocationUsageEvent(index));

  const batch = db.batch();
  docs.forEach((doc) => {
    const ref = db.collection(COLLECTION_NAME).doc(doc.rideId);
    batch.set(ref, doc);
  });

  await batch.commit();

  console.log(`Created ${docs.length} mock documents in ${COLLECTION_NAME}.`);

  const summary = docs.reduce(
    (acc, doc) => {
      acc.total += 1;
      if (doc.usedCurrentLocationOrigin) acc.origin += 1;
      if (doc.usedCurrentLocationDestination) acc.destination += 1;
      if (doc.usedCurrentLocationOrigin && doc.usedCurrentLocationDestination) acc.both += 1;
      if (!doc.usedCurrentLocationOrigin && !doc.usedCurrentLocationDestination) acc.neither += 1;
      return acc;
    },
    { total: 0, origin: 0, destination: 0, both: 0, neither: 0 },
  );

  console.log("Summary:");
  console.log(`- Total rides: ${summary.total}`);
  console.log(`- Origin current location used: ${summary.origin}`);
  console.log(`- Destination current location used: ${summary.destination}`);
  console.log(`- Both fields used current location: ${summary.both}`);
  console.log(`- Neither field used current location: ${summary.neither}`);
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

function buildLocationUsageEvent(index) {
  const usagePattern = weightedPick([
    { value: "origin_only", weight: 12 },
    { value: "destination_only", weight: 2 },
    { value: "both", weight: 1 },
    { value: "neither", weight: 5 },
  ]).value;

  const publishedAt = Timestamp.fromDate(buildRecentPublishedDate(index));

  return {
    rideId: `seed-location-usage-${Date.now()}-${index}-${randomInt(1000, 9999)}`,
    driverId: weightedPick(
      DRIVER_IDS.map((driverId, driverIndex) => ({
        value: driverId,
        weight: Math.max(1, DRIVER_IDS.length - driverIndex),
      })),
    ).value,
    usedCurrentLocationOrigin:
      usagePattern === "origin_only" || usagePattern === "both",
    usedCurrentLocationDestination:
      usagePattern === "destination_only" || usagePattern === "both",
    publishedAt,
  };
}

function buildRecentPublishedDate(index) {
  const now = new Date();
  const daysAgo = randomInt(0, 20);
  const hour = weightedPick([
    { value: 6, weight: 1 },
    { value: 7, weight: 2 },
    { value: 8, weight: 3 },
    { value: 9, weight: 2 },
    { value: 12, weight: 2 },
    { value: 14, weight: 2 },
    { value: 16, weight: 4 },
    { value: 17, weight: 5 },
    { value: 18, weight: 5 },
    { value: 19, weight: 4 },
    { value: 20, weight: 2 },
  ]).value;
  const minute = [0, 10, 15, 20, 30, 40, 45, 50][index % 8];

  const date = new Date(now);
  date.setDate(now.getDate() - daysAgo);
  date.setHours(hour, minute, randomInt(0, 59), 0);
  return date;
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

  return Math.max(10, Math.min(200, Math.floor(count)));
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

main().catch((error) => {
  console.error(`Failed to seed ${COLLECTION_NAME}.`);
  console.error(error);
  process.exitCode = 1;
});
