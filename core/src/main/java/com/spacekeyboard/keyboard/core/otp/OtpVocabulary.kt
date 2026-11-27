package com.spacekeyboard.keyboard.core.otp

internal object OtpVocabulary {
    private val IC = RegexOption.IGNORE_CASE

    private const val LATIN = "otp|2fa|pin|passcode|pass code|codes?|verification|verify|verifying|authenticat\\w*|one[- ]?time|" +
        "log[- ]?in|sign[- ]?in|token|password|passwort|kod|kodu|kodunuz|şifre\\w*|doğrulama|onay|codice|c[oó]digo|kode|" +
        "verifikasi|v[ée]rification|bestätigungscode|sicherheitscode|verificaci[oó]n|clave|contraseña|code de"

    private const val ARABIC = "رمز|كود|كلمة\\s+(?:ال)?(?:مرور|سر)|تحقق|تأكيد|تاكيد|تفعيل|مصادقة|الرقم\\s+السري|مرة\\s+واحدة"

    private const val PERSIAN = "کد|رمز|تایید|تأیید|یکبار\\s*مصرف|عبور"

    private const val CYRILLIC = "код\\w*|пароль|подтвержден\\w*|проверочн\\w*|одноразов\\w*"

    val KEYWORD = Regex(
        "(?<![\\p{L}])(?:$LATIN|[بكلوف]?(?:ال)?(?:$ARABIC)(?:ك|كم|ه|ها)?|$PERSIAN|$CYRILLIC)(?![\\p{L}])",
        IC,
    )

    private const val MONEY = "rs|inr|usd|eur|sar|aed|ils|egp|jod|try|gbp|kwd|qar|bhd|omr"

    private const val SYMBOLS = "[$€£₪₹]"

    private const val NEGATIVE_WORDS = "promo|coupon|voucher|referral|discount|gift|zip|postal|area|country|tracking|booking|" +
        "order|invoice|reference|ref|txn|transaction|trans|receipt|account|acct|a/c|card|ending|ends|balance|amount|amt|total|" +
        "price|paid|payment|pay|$MONEY|$SYMBOLS|مبلغ|المبلغ|رصيد|الرصيد|رصيدك|دفع|سداد|فاتورة|حساب|بطاقة|بطاقتك|خصم|كوبون|قسيمة|" +
        "رقم\\s+(?:ال)?(?:طلب|عملية|مرجع|حساب|بطاقة|فاتورة|شحنة|تتبع|هاتف|جوال|حجز|هوية)|" +
        "(?:رمز|كود)\\s+(?:ال)?(?:خصم|دعوة|إحالة|ترويجي|بريدي|دولة|منطقة)|الرمز\\s+البريدي"

    val NEGATIVE_BEFORE = Regex(
        "(?<![\\p{L}])(?:$NEGATIVE_WORDS)(?:\\s*(?:codes?|no\\.?|number|num\\.?|id|nr\\.?|n°|ref\\.?|رقم))?[\\s:#.\\-=]{0,4}$",
        IC,
    )

    private const val UNITS = "minutes?|mins?|seconds?|secs?|hours?|hrs?|days?|dk|dakika|saniye|saat|minuten|stunden|heures?|minutos|" +
        "دقيقة|دقائق|دقيقه|ثانية|ثواني|ثوان|ساعة|ساعات|يوم|أيام|دينار|ريال|شيكل|جنيه|درهم|ر\\.?س|د\\.?إ|ج\\.?م|$MONEY"

    val NEGATIVE_AFTER = Regex("^\\s{0,2}(?:%|$SYMBOLS|(?:$UNITS)(?![\\p{L}]))", IC)

    val VALIDITY = Regex(
        "(?:valid|expires?|expiry|within|for|صالح\\w*|صلاحية|تنتهي|خلال|لمدة|geçerli|valable|gültig)\\D{0,25}?" +
            "(\\d{1,3})\\s*(minutes?|mins?|دقيقة|دقائق|دقيقه|dk|dakika|minuten|minutos|hours?|hrs?|heures?|ساعة|ساعات|seconds?|secs?|ثانية|ثواني|ثوان)(?![\\p{L}])",
        IC,
    )

    private const val FIELD_OTP_WORDS = "otp|one[- ]?time|verification|verify|sms|security code|auth\\w*|2fa|pin|code|passcode|token|" +
        "رمز|كود|تحقق|التحقق|مرة واحدة|الرقم السري|کد|تایید|код|kod|codice|c[oó]digo|kode"

    val FIELD_OTP = Regex("(?<![\\p{L}])(?:$FIELD_OTP_WORDS)(?![\\p{L}])", IC)

    val FIELD_VERIFY = Regex("otp|one[- ]?time|verification|verify|sms|auth|2fa|التحقق|تحقق|مرة واحدة|تایید|подтвержд", IC)

    val FIELD_DENY = Regex(
        "zip|postal|post code|promo|coupon|voucher|referral|invite|discount|gift|country code|area code|qr|tracking|" +
            "الرمز البريدي|رمز بريدي|كود الخصم|رمز الخصم|رمز الدعوة|رمز الدولة|كود الدولة|رمز المنطقة|كوبون|الترويجي|الإحالة",
        IC,
    )

    val FIELD_PASSWORD = Regex("password|passwort|contrase|mot de passe|şifre|كلمة المرور|كلمة السر|رمز عبور|пароль", IC)
}
