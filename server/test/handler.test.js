const test = require('node:test');
const assert = require('node:assert/strict');
const { createHandler } = require('../src/handler');
const { createLimiter, createMemoryStore } = require('../src/limiter');
const { LIMITS } = require('../src/config');

const ID = '123e4567-e89b-12d3-a456-426614174000';
const BODY = JSON.stringify({ text: 'secret words', target: 'ar' });

function build(overrides = {}) {
  const logs = [];
  const calls = [];
  const limits = { ...LIMITS, perInstallPerDay: 2 };
  const handle = createHandler({
    verifyAppCheck: overrides.verify || (async () => ({})),
    limiter: createLimiter({ store: createMemoryStore(), limits, now: () => 0 }),
    provider: overrides.provider || (async (request) => { calls.push(request); return 'مترجم'; }),
    limits,
    log: (entry) => logs.push(entry),
  });
  return { handle, logs, calls };
}

const good = { method: 'POST', headers: { 'x-install-id': ID, 'x-firebase-appcheck': 't' }, rawBody: BODY };

test('happy path returns text and remaining', async () => {
  const { handle, calls } = build();
  const result = await handle(good);
  assert.deepEqual(result, { status: 200, body: { text: 'مترجم', remaining: 1 } });
  assert.equal(calls[0].text, 'secret words');
});

test('only POST is accepted', async () => {
  assert.equal((await build().handle({ ...good, method: 'GET' })).status, 405);
});

test('missing or invalid app check is rejected before any quota or provider use', async () => {
  const none = build();
  assert.equal((await none.handle({ ...good, headers: { 'x-install-id': ID } })).status, 401);
  const bad = build({ verify: async () => { throw new Error('no'); } });
  assert.equal((await bad.handle(good)).status, 401);
  assert.equal(bad.calls.length, 0);
});

test('bad install id and bad body are 400', async () => {
  const { handle } = build();
  assert.equal((await handle({ ...good, headers: { 'x-install-id': 'x', 'x-firebase-appcheck': 't' } })).status, 400);
  assert.equal((await handle({ ...good, rawBody: '{"text":"a","target":"zz"}' })).status, 400);
});

test('quota exhaustion returns 429 and never calls the provider again', async () => {
  const { handle, calls } = build();
  await handle(good);
  await handle(good);
  const third = await handle(good);
  assert.deepEqual(third, { status: 429, body: { error: 'quota', remaining: 0 } });
  assert.equal(calls.length, 2);
});

test('provider failure is 502 without leaking details', async () => {
  const { handle } = build({ provider: async () => { throw new Error('boom secret words'); } });
  const result = await handle(good);
  assert.deepEqual(result, { status: 502, body: { error: 'provider' } });
});

test('logs never contain user text', async () => {
  const { handle, logs } = build();
  await handle(good);
  await handle({ ...good, method: 'GET' });
  assert.ok(logs.length > 0);
  assert.ok(!JSON.stringify(logs).includes('secret'));
  assert.deepEqual(logs[0], { status: 200 });
});
