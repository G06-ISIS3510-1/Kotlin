#!/usr/bin/env node

const fs = require("fs");
const path = require("path");
const { initializeApp, cert, applicationDefault, getApps } = require("firebase-admin/app");
const { getFirestore, Timestamp } = require("firebase-admin/firestore");

const COLLECTION_NAME = "reviews";

const DRIVERS = [
  { driverId: "yNkepp6neATKtock2IcnFl9TPmf1", driverName: "Mauricio Urrego" },
  { driverId: "DWPsINMl6Nf5jO4neLkR1nEC7163", driverName: "Andrés Neira" },
  { driverId: "bIyByFUjGgZKP3nwO05ufgw28m12", driverName: "prueba8" },
  { driverId: "Hb9CicocP9M2962OuVEL07kwScT2", driverName: "Prueba3" },
  { driverId: "xCJd7yevQhZmrecBG2u7a9Rf35l1", driverName: "samara" },
];

const PASSENGERS = [
  { passengerId: "x3yRqd8CasS0Q8HplVTM6WmyQ0H2", passengerName: "Laura" },
  { passengerId: "uI4QRtMHbihwGggTHxZt5cYd0d23", passengerName: "Manuela González" },
  { passengerId: "uTSFShPeH6Ydfs1gTFR3Rji983g1", passengerName: "María González" },
  { passengerId: "lTOApe9t6RhcYWVLPh1756Amo3G2", passengerName: "Martin Del Gordo" },
];

const REVIEWS = [
  {
    driverId: "yNkepp6neATKtock2IcnFl9TPmf1",
    passengerId: "x3yRqd8CasS0Q8HplVTM6WmyQ0H2",
    stars: 5,
    comment: "Very smooth ride and excellent communication.",
    createdAt: "2026-04-10T14:35:00.000Z",
  },
  {
    driverId: "yNkepp6neATKtock2IcnFl9TPmf1",
    passengerId: "uI4QRtMHbihwGggTHxZt5cYd0d23",
    stars: 4,
    comment: "Punctual and friendly. Would ride again.",
    createdAt: "2026-04-12T15:10:00.000Z",
  },
  {
    driverId: "yNkepp6neATKtock2IcnFl9TPmf1",
    passengerId: "uTSFShPeH6Ydfs1gTFR3Rji983g1",
    stars: 5,
    comment: "Clean car and great driving.",
    createdAt: "2026-04-16T17:05:00.000Z",
  },
  {
    driverId: "DWPsINMl6Nf5jO4neLkR1nEC7163",
    passengerId: "uI4QRtMHbihwGggTHxZt5cYd0d23",
    stars: 5,
    comment: "Excellent trip, arrived right on time.",
    createdAt: "2026-04-11T13:20:00.000Z",
  },
  {
    driverId: "DWPsINMl6Nf5jO4neLkR1nEC7163",
    passengerId: "lTOApe9t6RhcYWVLPh1756Amo3G2",
    stars: 4,
    comment: "Good conversation and safe driving.",
    createdAt: "2026-04-13T16:45:00.000Z",
  },
  {
    driverId: "DWPsINMl6Nf5jO4neLkR1nEC7163",
    passengerId: "x3yRqd8CasS0Q8HplVTM6WmyQ0H2",
    stars: 4,
    comment: "Very helpful and easy to coordinate with.",
    createdAt: "2026-04-17T18:00:00.000Z",
  },
  {
    driverId: "bIyByFUjGgZKP3nwO05ufgw28m12",
    passengerId: "uTSFShPeH6Ydfs1gTFR3Rji983g1",
    stars: 5,
    comment: "Great experience, smooth route.",
    createdAt: "2026-04-09T12:15:00.000Z",
  },
  {
    driverId: "bIyByFUjGgZKP3nwO05ufgw28m12",
    passengerId: "x3yRqd8CasS0Q8HplVTM6WmyQ0H2",
    stars: 3,
    comment: "Ride was fine, but departure was a bit late.",
    createdAt: "2026-04-14T19:30:00.000Z",
  },
  {
    driverId: "bIyByFUjGgZKP3nwO05ufgw28m12",
    passengerId: "lTOApe9t6RhcYWVLPh1756Amo3G2",
    stars: 4,
    comment: "Good overall and very polite.",
    createdAt: "2026-04-18T11:40:00.000Z",
  },
  {
    driverId: "Hb9CicocP9M2962OuVEL07kwScT2",
    passengerId: "uI4QRtMHbihwGggTHxZt5cYd0d23",
    stars: 5,
    comment: "Super comfortable ride and clear communication.",
    createdAt: "2026-04-08T08:55:00.000Z",
  },
  {
    driverId: "Hb9CicocP9M2962OuVEL07kwScT2",
    passengerId: "uTSFShPeH6Ydfs1gTFR3Rji983g1",
    stars: 4,
    comment: "Nice driver, would book again.",
    createdAt: "2026-04-12T09:25:00.000Z",
  },
  {
    driverId: "Hb9CicocP9M2962OuVEL07kwScT2",
    passengerId: "x3yRqd8CasS0Q8HplVTM6WmyQ0H2",
    stars: 5,
    comment: "Excellent driving and very reliable.",
    createdAt: "2026-04-16T10:50:00.000Z",
  },
  {
    driverId: "xCJd7yevQhZmrecBG2u7a9Rf35l1",
    passengerId: "lTOApe9t6RhcYWVLPh1756Amo3G2",
    stars: 5,
    comment: "Very friendly and professional.",
    createdAt: "2026-04-07T14:00:00.000Z",
  },
  {
    driverId: "xCJd7yevQhZmrecBG2u7a9Rf35l1",
    passengerId: "uI4QRtMHbihwGggTHxZt5cYd0d23",
    stars: 4,
    comment: "Great coordination and comfortable ride.",
    createdAt: "2026-04-13T14:45:00.000Z",
  },
  {
    driverId: "xCJd7yevQhZmrecBG2u7a9Rf35l1",
    passengerId: "uTSFShPeH6Ydfs1gTFR3Rji983g1",
    stars: 5,
    comment: "Smooth, safe and on time.",
    createdAt: "2026-04-18T16:20:00.000Z",
  },
];

async function main() {
  const options = parseArgs(process.argv.slice(2));
  const projectId = resolveProjectId(options.projectId);

  initializeFirebase(options.serviceAccount, projectId, options.emulatorHost);
  const db = getFirestore();

  console.log(`Preparing to seed ${COLLECTION_NAME} for project ${projectId}...`);

  const batch = db.batch();
  REVIEWS.forEach((review) => {
    const passenger = PASSENGERS.find((entry) => entry.passengerId === review.passengerId);
    const driver = DRIVERS.find((entry) => entry.driverId === review.driverId);
    const docId = buildReviewId(review.driverId, review.passengerId);
    const ref = db.collection(COLLECTION_NAME).doc(docId);
    batch.set(ref, {
      reviewId: docId,
      driverId: review.driverId,
      driverName: driver ? driver.driverName : review.driverId,
      passengerId: review.passengerId,
      passengerName: passenger ? passenger.passengerName : review.passengerId,
      stars: review.stars,
      comment: review.comment,
      createdAt: Timestamp.fromDate(new Date(review.createdAt)),
      updatedAt: Timestamp.fromDate(new Date(review.createdAt)),
    });
  });

  console.log(`Writing ${REVIEWS.length} documents...`);
  await batch.commit();

  console.log(`Seeded ${REVIEWS.length} review documents into ${COLLECTION_NAME}.`);

  DRIVERS.forEach((driver) => {
    const count = REVIEWS.filter((review) => review.driverId === driver.driverId).length;
    console.log(`- ${driver.driverName}: ${count} reviews`);
  });
}

function initializeFirebase(serviceAccountPath, projectId, emulatorHost) {
  if (getApps().length > 0) {
    return;
  }

  if (serviceAccountPath) {
    const resolvedPath = resolveServiceAccountPath(serviceAccountPath);
    const serviceAccount = JSON.parse(fs.readFileSync(resolvedPath, "utf8"));

    console.log(`Using service account: ${resolvedPath}`);
    delete process.env.FIRESTORE_EMULATOR_HOST;
    initializeApp({
      credential: cert(serviceAccount),
      projectId,
    });
    return;
  }

  const resolvedEmulatorHost = emulatorHost || process.env.FIRESTORE_EMULATOR_HOST;
  if (resolvedEmulatorHost) {
    process.env.FIRESTORE_EMULATOR_HOST = resolvedEmulatorHost;
    console.log(`Using Firestore emulator at ${resolvedEmulatorHost}`);
    initializeApp({
      projectId,
    });
    return;
  }

  if (process.env.GOOGLE_APPLICATION_CREDENTIALS) {
    initializeApp({
      credential: applicationDefault(),
      projectId,
    });
    return;
  }

  throw new Error(
    "No Firestore emulator host or service account was provided. Start the Firestore emulator and set FIRESTORE_EMULATOR_HOST=127.0.0.1:8080, or pass --service-account <path>."
  );
}

function buildReviewId(driverId, passengerId) {
  return `${driverId}_${passengerId}`;
}

function parseArgs(argv) {
  const result = {};

  for (let i = 0; i < argv.length; i += 1) {
    const current = argv[i];
    if (current === "--service-account") {
      result.serviceAccount = argv[i + 1];
      i += 1;
    } else if (current === "--project-id") {
      result.projectId = argv[i + 1];
      i += 1;
    } else if (current === "--emulator-host") {
      result.emulatorHost = argv[i + 1];
      i += 1;
    }
  }

  return result;
}

function resolveProjectId(explicitProjectId) {
  if (explicitProjectId) {
    return explicitProjectId;
  }

  const envProjectId = process.env.GOOGLE_CLOUD_PROJECT || process.env.FIREBASE_CONFIG_PROJECT_ID;
  if (envProjectId) {
    return envProjectId;
  }

  return "wheels-app";
}

function resolveServiceAccountPath(serviceAccountPath) {
  return path.isAbsolute(serviceAccountPath)
    ? serviceAccountPath
    : path.resolve(process.cwd(), serviceAccountPath);
}

main().catch((error) => {
  console.error("Failed to seed reviews.");
  console.error(error);
  process.exitCode = 1;
});
