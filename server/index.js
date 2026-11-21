const { onRequest } = require('firebase-functions/v2/https');
const { defineSecret } = require('firebase-functions/params');
const logger = require('firebase-functions/logger');
const { initializeApp } = require('firebase-admin/app');
const { getAppCheck } = require('firebase-admin/app-check');
const { limitsFromEnv } = require('./src/config');
const { createHandler } = require('./src/handler');
const { createLimiter } = require('./src/limiter');
const { createFirestoreStore } = require('./src/firestore-store');
const { createProvider } = require('./src/provider');

const apiKey = defineSecret('ANTHROPIC_API_KEY');

initializeApp();

let handler = null;

function resolveHandler() {
  if (handler) return handler;
  const limits = limitsFromEnv(process.env);
  handler = createHandler({
    verifyAppCheck: (token) => getAppCheck().verifyToken(token),
    limiter: createLimiter({ store: createFirestoreStore(), limits }),
    provider: createProvider({ apiKey: apiKey.value(), model: process.env.TRANSLATE_MODEL || undefined, limits }),
    limits,
    log: (entry) => logger.info('translate', entry),
  });
  return handler;
}

exports.translate = onRequest(
  { secrets: [apiKey], cors: false, maxInstances: 10, timeoutSeconds: 30, memory: '256MiB' },
  async (req, res) => {
    const result = await resolveHandler()({ method: req.method, headers: req.headers, rawBody: req.rawBody ? req.rawBody.toString('utf8') : '' });
    res.status(result.status).json(result.body);
  },
);
