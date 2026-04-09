import { getApps, initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";

const app = getApps().length > 0 ? getApps()[0] : initializeApp();

export const db = getFirestore(app);
export const messaging = getMessaging(app);
