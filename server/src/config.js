const LANGUAGES = [
  'af', 'ar', 'be', 'bg', 'bn', 'ca', 'cs', 'cy', 'da', 'de', 'el', 'en', 'eo', 'es', 'et', 'fa', 'fi', 'fr',
  'ga', 'gl', 'gu', 'he', 'hi', 'hr', 'ht', 'hu', 'id', 'is', 'it', 'ja', 'ka', 'kn', 'ko', 'lt', 'lv', 'mk',
  'mr', 'ms', 'mt', 'nl', 'no', 'pl', 'pt', 'ro', 'ru', 'sk', 'sl', 'sq', 'sv', 'sw', 'ta', 'te', 'th', 'tl',
  'tr', 'uk', 'ur', 'vi', 'zh',
];

const STYLES = ['neutral', 'formal', 'casual', 'chat', 'polite', 'concise'];

const LIMITS = {
  maxText: 2000,
  maxBody: 8192,
  perInstallPerDay: 30,
  globalPerDay: 20000,
  maxOutputTokens: 2048,
  providerTimeoutMs: 20000,
};

function readInt(value, fallback) {
  const parsed = Number.parseInt(value, 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

function limitsFromEnv(env) {
  return {
    ...LIMITS,
    perInstallPerDay: readInt(env.TRANSLATE_PER_INSTALL_PER_DAY, LIMITS.perInstallPerDay),
    globalPerDay: readInt(env.TRANSLATE_GLOBAL_PER_DAY, LIMITS.globalPerDay),
  };
}

module.exports = { LANGUAGES, STYLES, LIMITS, limitsFromEnv };
