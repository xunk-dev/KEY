from dataclasses import dataclass

from symbol_rules import classify

FEATURED = (
    0x20AC, 0x7B, 0x7D, 0x2713, 0xB6, 0x3C0, 0xAE, 0xA9, 0x2206, 0x2605, 0x272A, 0x2765, 0x2667, 0xB0,
    0x25CF, 0xD7, 0x221A, 0x2122, 0x2022, 0x2026, 0xA7, 0x2020, 0x2021, 0x2190, 0x2192, 0x2191, 0x2193,
    0x2194, 0x2661, 0x2665, 0x2714, 0x2717, 0x221E, 0x2248, 0x2260, 0x2264, 0x2265, 0xB1, 0xF7, 0xAB,
    0xBB, 0xBF, 0xA1, 0x2030, 0x2116, 0x263A, 0x2606, 0x266A, 0x266B, 0xA2, 0xA3, 0xA5, 0x20B9, 0xFDFC,
    0x66A, 0x61F, 0x61B, 0x60C, 0xFD3E, 0xFD3F, 0xFDFD, 0xFDFA,
)
FALLBACK_CATEGORY = "signs"


@dataclass(frozen=True)
class Symbol:
    cp: int
    category: str
    unicode_name: str
    english: str
    english_keywords: tuple
    arabic: str
    arabic_keywords: tuple


def clean(text):
    return " ".join(text.replace("\t", " ").replace("|", " ").split())


def clean_all(words):
    seen = []
    for item in words:
        value = clean(item).lower()
        if value and value not in seen:
            seen.append(value)
    return tuple(seen)


def build_symbols(characters, emoji, english, arabic):
    found = []
    for cp in sorted(characters):
        character = characters[cp]
        category = classify(cp, character)
        if category is None or cp in emoji:
            continue
        found.append(make(cp, category, character, english, arabic))
    return found


def make(cp, category, character, english, arabic):
    en = english.get(cp)
    ar = arabic.get(cp)
    return Symbol(
        cp=cp,
        category=category,
        unicode_name=clean(character.name),
        english=clean(en.name).lower() if en else "",
        english_keywords=clean_all(en.keywords) if en else (),
        arabic=clean(ar.name) if ar else "",
        arabic_keywords=clean_all(ar.keywords) if ar else (),
    )


def force_featured(symbols, characters, english, arabic):
    present = {symbol.cp for symbol in symbols}
    extra = []
    for cp in FEATURED:
        if cp not in present and cp in characters:
            extra.append(make(cp, FALLBACK_CATEGORY, characters[cp], english, arabic))
    return sorted(symbols + extra, key=lambda symbol: symbol.cp)


def render_symbols(symbols):
    lines = ["#space-keyboard-symbols 1"]
    for symbol in symbols:
        lines.append("\t".join([
            format(symbol.cp, "X"),
            symbol.category,
            symbol.unicode_name,
            symbol.english,
            "|".join(symbol.english_keywords),
            symbol.arabic,
            "|".join(symbol.arabic_keywords),
        ]))
    return "\n".join(lines) + "\n"


def render_featured(symbols):
    present = {symbol.cp for symbol in symbols}
    lines = ["#space-keyboard-featured 1"]
    lines += [format(cp, "X") for cp in FEATURED if cp in present]
    return "\n".join(lines) + "\n"
