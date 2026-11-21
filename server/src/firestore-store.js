const { getFirestore, Timestamp } = require('firebase-admin/firestore');

const KEEP_DAYS = 3;

function createFirestoreStore(db = getFirestore()) {
  return {
    async increment(key, limit, day) {
      const ref = db.collection('quota').doc(key);
      return db.runTransaction(async (tx) => {
        const snapshot = await tx.get(ref);
        const count = snapshot.exists ? snapshot.get('count') : 0;
        if (count >= limit) return { ok: false, count };
        const expireAt = Timestamp.fromMillis(Date.parse(day) + KEEP_DAYS * 86400000);
        tx.set(ref, { count: count + 1, expireAt });
        return { ok: true, count: count + 1 };
      });
    },
  };
}

module.exports = { createFirestoreStore };
