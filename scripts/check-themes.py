from pathlib import Path
import re
import sys

CATALOG = Path("core/src/main/java/com/spacekeyboard/keyboard/core/theme/ThemeCatalog.kt")
TEXT_MINIMUM = 4.5
KEY_SEPARATION = 1.25
DECOR = 2.0
LIGHT_TEXT = 0xFFF4F6FF
DARK_TEXT = 0xFF10131A
PRESS_BLEND = 0.6


def channels(value):
    return [(value >> shift) & 0xFF for shift in (16, 8, 0)]


def luminance(value):
    out = []
    for c in channels(value):
        v = c / 255
        out.append(v / 12.92 if v <= 0.04045 else ((v + 0.055) / 1.055) ** 2.4)
    return 0.2126 * out[0] + 0.7152 * out[1] + 0.0722 * out[2]


def contrast(a, b):
    hi, lo = sorted((luminance(a), luminance(b)), reverse=True)
    return (hi + 0.05) / (lo + 0.05)


def mix(a, b, t):
    parts = [round(x + (y - x) * t) for x, y in zip(channels(a), channels(b))]
    return 0xFF000000 | (parts[0] << 16) | (parts[1] << 8) | parts[2]


def ensure(color, background, minimum):
    base = 0xFF000000 | (color & 0xFFFFFF)
    if contrast(base, background) >= minimum:
        return base
    end = 0xFFFFFFFF if luminance(background) < 0.4 else 0xFF000000
    for step in range(1, 51):
        candidate = mix(base, end, step / 50)
        if contrast(candidate, background) >= minimum:
            return candidate
    return end


def readable(background):
    for color in (LIGHT_TEXT, DARK_TEXT):
        if contrast(color, background) >= TEXT_MINIMUM:
            return color
    return 0xFFFFFFFF if contrast(0xFFFFFFFF, background) >= contrast(0xFF000000, background) else 0xFF000000


def resolve(background, keys, glow, border):
    background = 0xFF000000 | (background & 0xFFFFFF)
    keys = ensure(keys, background, KEY_SEPARATION)
    glow = ensure(glow, background, DECOR)
    border = ensure(border, background, DECOR)
    pressed = mix(keys, glow, PRESS_BLEND)
    return background, keys, pressed, glow, border


def main():
    source = CATALOG.read_text(encoding="utf-8")
    pattern = re.compile(r'ThemeDef\("(\w+)",[^,]+,[^,]+,\s*ThemeColors\(((?:0x[0-9A-Fa-f]{8}\.toInt\(\)(?:,\s*)?){4})\)(?:,\s*light = true)?\)')
    themes = pattern.findall(source)
    declared = len(re.findall(r'ThemeDef\("', source))
    failed = len(themes) == 0 or len(themes) != declared
    print(f"themes found: {len(themes)}")
    for key, colors in themes:
        values = [int(v, 16) for v in re.findall(r"0x([0-9A-Fa-f]{8})", colors)]
        background, keys, pressed, glow, border = resolve(*values)
        unchanged = [keys, glow, border] == [0xFF000000 | (v & 0xFFFFFF) for v in values[1:]]
        checks = [
            (f"{key}: text/keys", contrast(readable(keys), keys), TEXT_MINIMUM),
            (f"{key}: text/pressed", contrast(readable(pressed), pressed), TEXT_MINIMUM),
            (f"{key}: label/background", contrast(readable(background), background), TEXT_MINIMUM),
            (f"{key}: keys/background", contrast(keys, background), KEY_SEPARATION),
            (f"{key}: glow/background", contrast(glow, background), DECOR),
            (f"{key}: border/background", contrast(border, background), DECOR),
        ]
        for label, ratio, minimum in checks:
            print(f"{label}: {ratio:.2f}:1")
            if ratio < minimum:
                failed = True
        if not unchanged:
            print(f"{key}: default colors need adjusting by the resolver")
            failed = True
    if failed:
        sys.exit(1)
    print("OK: all themes pass")


main()
