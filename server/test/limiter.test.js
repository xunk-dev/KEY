const test = require('node:test');
const assert = require('node:assert/strict');
const { createLimiter, createMemoryStore, hashId, dayKey } = require('../src/limiter');

const limits = { perInstallPerDay: 3, globalPerDay: 5 };
const A = '123e4567-e89b-12d3-a456-426614174000';
const B = '223e4567-e89b-12d3-a456-426614174000';

test('counts down per install and then blocks', async () => {
  const limiter = createLimiter({ store: createMemoryStore(), limits, now: () => 0 });
  assert.deepEqual(await limiter.take(A), { ok: true, remaining: 2 });
  assert.deepEqual(await limiter.take(A), { ok: true, remaining: 1 });
  assert.deepEqual(await limiter.take(A), { ok: true, remaining: 0 });
  assert.deepEqual(await limiter.take(A), { ok: false, remaining: 0 });
});

test('installs do not share a quota', async () => {
  const limiter = createLimiter({ store: createMemoryStore(), limits, now: () => 0 });
  for (let i = 0; i < 3; i++) await limiter.take(A);
  assert.equal((await limiter.take(B)).ok, true);
});

test('global cap stops everyone', async () => {
  const limiter = createLimiter({ store: createMemoryStore(), limits, now: () => 0 });
  for (let i = 0; i < 3; i++) await limiter.take(A);
  for (let i = 0; i < 2; i++) await limiter.take(B);
  assert.equal((await limiter.take(B)).ok, false);
});

test('a new day resets the quota', async () => {
  let time = 0;
  const limiter = createLimiter({ store: createMemoryStore(), limits, now: () => time });
  for (let i = 0; i < 3; i++) await limiter.take(A);
  assert.equal((await limiter.take(A)).ok, false);
  time = 86400000;
  assert.equal((await limiter.take(A)).ok, true);
});

test('keys never contain the raw install id', () => {
  assert.ok(!hashId(A).includes(A));
  assert.equal(hashId(A).length, 32);
  assert.equal(dayKey(0), '1970-01-01');
});
