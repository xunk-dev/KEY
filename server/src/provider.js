const { buildSystem, buildMessages } = require('./prompt');

const ENDPOINT = 'https://api.anthropic.com/v1/messages';
const API_VERSION = '2023-06-01';
const DEFAULT_MODEL = 'claude-haiku-5-5';

class ProviderError extends Error {}

function extractText(payload) {
  const blocks = Array.isArray(payload && payload.content) ? payload.content : [];
  const text = blocks.filter((block) => block.type === 'text').map((block) => block.text).join('');
  return text.trim();
}

function createProvider({ apiKey, model = DEFAULT_MODEL, fetchImpl = fetch, limits }) {
  return async function translate(request) {
    const response = await fetchImpl(ENDPOINT, {
      method: 'POST',
      headers: { 'content-type': 'application/json', 'x-api-key': apiKey, 'anthropic-version': API_VERSION },
      body: JSON.stringify({
        model,
        max_tokens: limits.maxOutputTokens,
        system: buildSystem(request),
        messages: buildMessages(request.text),
      }),
      signal: AbortSignal.timeout(limits.providerTimeoutMs),
    });
    if (!response.ok) throw new ProviderError(`provider status ${response.status}`);
    const text = extractText(await response.json());
    if (text.length === 0) throw new ProviderError('provider empty');
    return text;
  };
}

module.exports = { createProvider, ProviderError, DEFAULT_MODEL };
