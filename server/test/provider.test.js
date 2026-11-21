const test = require('node:test');
const assert = require('node:assert/strict');
const { createProvider, ProviderError, DEFAULT_MODEL } = require('../src/provider');

const limits = { maxOutputTokens: 100, providerTimeoutMs: 1000 };
const request = { text: 'hello', source: 'en', target: 'ar', style: 'formal' };

function fakeFetch(status, payload, seen) {
  return async (url, init) => {
    seen.push({ url, init });
    return { ok: status >= 200 && status < 300, status, json: async () => payload };
  };
}

test('sends the key in a header and text as data', async () => {
  const seen = [];
  const provider = createProvider({ apiKey: 'k', limits, fetchImpl: fakeFetch(200, { content: [{ type: 'text', text: ' مرحبا ' }] }, seen) });
  assert.equal(await provider(request), 'مرحبا');
  const body = JSON.parse(seen[0].init.body);
  assert.equal(seen[0].init.headers['x-api-key'], 'k');
  assert.equal(body.model, DEFAULT_MODEL);
  assert.equal(body.messages[0].content, '<text>hello</text>');
  assert.equal(body.max_tokens, 100);
  assert.ok(!seen[0].init.body.includes('"k"'));
});

test('model can be overridden', async () => {
  const seen = [];
  const provider = createProvider({ apiKey: 'k', model: 'm1', limits, fetchImpl: fakeFetch(200, { content: [{ type: 'text', text: 'x' }] }, seen) });
  await provider(request);
  assert.equal(JSON.parse(seen[0].init.body).model, 'm1');
});

test('provider errors and empty output throw', async () => {
  const failing = createProvider({ apiKey: 'k', limits, fetchImpl: fakeFetch(500, {}, []) });
  await assert.rejects(() => failing(request), ProviderError);
  const empty = createProvider({ apiKey: 'k', limits, fetchImpl: fakeFetch(200, { content: [] }, []) });
  await assert.rejects(() => empty(request), ProviderError);
  const odd = createProvider({ apiKey: 'k', limits, fetchImpl: fakeFetch(200, null, []) });
  await assert.rejects(() => odd(request), ProviderError);
});
