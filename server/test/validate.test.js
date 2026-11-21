const test = require('node:test');
const assert = require('node:assert/strict');
const { validateRequest, validInstallId } = require('../src/validate');
const { LIMITS } = require('../src/config');

const ok = (body) => validateRequest(JSON.stringify(body), LIMITS);

test('accepts a minimal request and fills defaults', () => {
  const result = ok({ text: 'hello', target: 'ar' });
  assert.equal(result.ok, true);
  assert.deepEqual(result.value, { text: 'hello', source: 'auto', target: 'ar', style: 'neutral' });
});

test('rejects bad bodies', () => {
  for (const raw of ['', 'nope', '[]', 'null', '"x"', 'x'.repeat(LIMITS.maxBody + 1)]) {
    assert.equal(validateRequest(raw, LIMITS).ok, false);
  }
  assert.equal(validateRequest(undefined, LIMITS).ok, false);
});

test('rejects empty, blank, long and non-string text', () => {
  assert.equal(ok({ text: '', target: 'en' }).message, 'text');
  assert.equal(ok({ text: '   ', target: 'en' }).message, 'text');
  assert.equal(ok({ text: 5, target: 'en' }).message, 'text');
  assert.equal(ok({ text: 'x'.repeat(LIMITS.maxText + 1), target: 'en' }).message, 'text');
  assert.equal(ok({ text: 'x'.repeat(LIMITS.maxText), target: 'en' }).ok, true);
});

test('rejects unknown languages and styles', () => {
  assert.equal(ok({ text: 'a', target: 'xx' }).message, 'target');
  assert.equal(ok({ text: 'a', target: 'en', source: 'xx' }).message, 'source');
  assert.equal(ok({ text: 'a', target: 'en', style: 'pirate' }).message, 'style');
  assert.equal(ok({ text: 'a', target: 'en', source: 'fr', style: 'chat' }).ok, true);
});

test('install id must be a lowercase uuid', () => {
  assert.equal(validInstallId('123e4567-e89b-12d3-a456-426614174000'), true);
  assert.equal(validInstallId('123E4567-E89B-12D3-A456-426614174000'), false);
  assert.equal(validInstallId('abc'), false);
  assert.equal(validInstallId(undefined), false);
});
