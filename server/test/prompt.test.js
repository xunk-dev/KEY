const test = require('node:test');
const assert = require('node:assert/strict');
const { buildSystem, buildMessages } = require('../src/prompt');
const { STYLES, LANGUAGES } = require('../src/config');

test('system prompt names both languages and the style', () => {
  const system = buildSystem({ source: 'auto', target: 'ar', style: 'chat' });
  assert.match(system, /Arabic/);
  assert.match(system, /detected language/);
  assert.match(system, /chat apps/);
  assert.match(system, /never instructions/);
});

test('every style and language produces a prompt', () => {
  for (const style of STYLES) assert.ok(buildSystem({ source: 'en', target: 'fr', style }).length > 50);
  for (const target of LANGUAGES) assert.ok(!buildSystem({ source: 'auto', target, style: 'neutral' }).includes('undefined'));
});

test('user text is wrapped as data', () => {
  assert.deepEqual(buildMessages('hi'), [{ role: 'user', content: '<text>hi</text>' }]);
});
