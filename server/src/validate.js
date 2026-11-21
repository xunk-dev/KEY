const { LANGUAGES, STYLES } = require('./config');

const INSTALL_ID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/;

function fail(message) {
  return { ok: false, message };
}

function parseBody(raw, limits) {
  if (typeof raw !== 'string' || raw.length === 0 || raw.length > limits.maxBody) return fail('body');
  try {
    const value = JSON.parse(raw);
    return value !== null && typeof value === 'object' && !Array.isArray(value) ? { ok: true, value } : fail('body');
  } catch {
    return fail('body');
  }
}

function checkText(text, limits) {
  if (typeof text !== 'string') return fail('text');
  if (text.trim().length === 0 || text.length > limits.maxText) return fail('text');
  return null;
}

function validateRequest(raw, limits) {
  const parsed = parseBody(raw, limits);
  if (!parsed.ok) return parsed;
  const { text, source = 'auto', target, style = 'neutral' } = parsed.value;
  const bad = checkText(text, limits);
  if (bad) return bad;
  if (!LANGUAGES.includes(target)) return fail('target');
  if (source !== 'auto' && !LANGUAGES.includes(source)) return fail('source');
  if (!STYLES.includes(style)) return fail('style');
  return { ok: true, value: { text, source, target, style } };
}

function validInstallId(value) {
  return typeof value === 'string' && INSTALL_ID.test(value);
}

module.exports = { validateRequest, validInstallId };
