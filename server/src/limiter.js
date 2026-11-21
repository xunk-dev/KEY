const crypto = require('node:crypto');

function dayKey(now) {
  return new Date(now).toISOString().slice(0, 10);
}

function hashId(installId) {
  return crypto.createHash('sha256').update(installId).digest('hex').slice(0, 32);
}

function createLimiter({ store, limits, now = Date.now }) {
  return {
    async take(installId) {
      const day = dayKey(now());
      const device = await store.increment(`${day}_${hashId(installId)}`, limits.perInstallPerDay, day);
      if (!device.ok) return { ok: false, remaining: 0 };
      const total = await store.increment(`${day}_all`, limits.globalPerDay, day);
      if (!total.ok) return { ok: false, remaining: 0 };
      return { ok: true, remaining: Math.max(0, limits.perInstallPerDay - device.count) };
    },
  };
}

function createMemoryStore() {
  const counts = new Map();
  return {
    async increment(key, limit) {
      const current = counts.get(key) || 0;
      if (current >= limit) return { ok: false, count: current };
      counts.set(key, current + 1);
      return { ok: true, count: current + 1 };
    },
  };
}

module.exports = { createLimiter, createMemoryStore, hashId, dayKey };
