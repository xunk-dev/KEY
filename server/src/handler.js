const { validateRequest, validInstallId } = require('./validate');

function reply(status, body) {
  return { status, body };
}

function createHandler({ verifyAppCheck, limiter, provider, limits, log = () => {} }) {
  async function authorize(headers) {
    if (!validInstallId(headers['x-install-id'])) return reply(400, { error: 'install' });
    const token = headers['x-firebase-appcheck'];
    if (typeof token !== 'string' || token.length === 0) return reply(401, { error: 'appcheck' });
    try {
      await verifyAppCheck(token);
    } catch {
      return reply(401, { error: 'appcheck' });
    }
    return null;
  }

  async function run(request, installId) {
    const slot = await limiter.take(installId);
    if (!slot.ok) return reply(429, { error: 'quota', remaining: 0 });
    try {
      const text = await provider(request);
      return reply(200, { text, remaining: slot.remaining });
    } catch {
      return reply(502, { error: 'provider' });
    }
  }

  return async function handle({ method, headers, rawBody }) {
    if (method !== 'POST') return reply(405, { error: 'method' });
    const denied = await authorize(headers);
    if (denied) return denied;
    const parsed = validateRequest(rawBody, limits);
    if (!parsed.ok) return reply(400, { error: parsed.message });
    const result = await run(parsed.value, headers['x-install-id']);
    log({ status: result.status });
    return result;
  };
}

module.exports = { createHandler };
