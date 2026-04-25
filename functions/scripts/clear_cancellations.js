#!/usr/bin/env node

const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

const COLLECTION_NAME = "analytics_ride_cancellations";
const BATCH_SIZE = 200;

async function main() {
  const args = parseArgs(process.argv.slice(2));
  initializeFirebase(args);

  const db = admin.firestore();
  let deletedCount = 0;

  while (true) {
    const snapshot = await db
      .collection(COLLECTION_NAME)
      .limit(BATCH_SIZE)
      .get();

    if (snapshot.empty) {
      break;
    }

    const batch = db.batch();
    snapshot.docs.forEach((doc) => batch.delete(doc.ref));
    await batch.commit();

    deletedCount += snapshot.size;
    console.log(`Deleted ${deletedCount} documents so far...`);
  }

  console.log(`Finished. Deleted ${deletedCount} documents from ${COLLECTION_NAME}.`);
}

function initializeFirebase(args) {
  const serviceAccountPath = resolveServiceAccountPath(args.serviceAccountPath);
  const projectId = args.projectId || readProjectIdFromFirebaseRc();

  if (serviceAccountPath) {
    const serviceAccount = require(serviceAccountPath);
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
      projectId: projectId || serviceAccount.project_id,
    });
    return;
  }

  admin.initializeApp({
    credential: admin.credential.applicationDefault(),
    ...(projectId ? { projectId } : {}),
  });
}

function parseArgs(argv) {
  const args = {
    serviceAccountPath: undefined,
    projectId: undefined,
  };

  for (let index = 0; index < argv.length; index += 1) {
    const current = argv[index];

    if (current === "--service-account") {
      args.serviceAccountPath = argv[index + 1];
      index += 1;
      continue;
    }

    if (current === "--project-id") {
      args.projectId = argv[index + 1];
      index += 1;
    }
  }

  return args;
}

function resolveServiceAccountPath(providedPath) {
  const candidate =
    providedPath ||
    process.env.FIREBASE_SERVICE_ACCOUNT_PATH ||
    process.env.GOOGLE_APPLICATION_CREDENTIALS;

  if (!candidate) {
    return null;
  }

  const absolutePath = path.resolve(candidate);
  if (!fs.existsSync(absolutePath)) {
    throw new Error(`Service account file not found: ${candidate}`);
  }

  return absolutePath;
}

function readProjectIdFromFirebaseRc() {
  const firebaseRcPath = path.resolve(__dirname, "..", "..", ".firebaserc");
  if (!fs.existsSync(firebaseRcPath)) {
    return undefined;
  }

  const raw = fs.readFileSync(firebaseRcPath, "utf8");
  const parsed = JSON.parse(raw);

  return parsed?.projects?.default || parsed?.projects?.wheels;
}

main().catch((error) => {
  console.error(`Failed to clear ${COLLECTION_NAME}.`);
  console.error(error);
  process.exitCode = 1;
});
