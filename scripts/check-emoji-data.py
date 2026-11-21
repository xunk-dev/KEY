from pathlib import Path
import struct
import sys

ASSETS = Path("feature-emoji/src/main/assets/emoji")
GROUPS = {"smileys", "people", "animals", "food", "travel", "activities", "objects", "symbols", "flags"}
MIN_EMOJI = 1300
MIN_NAMED = {"en": 0.95, "ar": 0.6}
MIN_FONT_BYTES = 1_000_000
LICENSE_MARKERS = ("sil open font license", "openfontlicense", "scripts.sil.org/ofl")


def tables(font):
    count = struct.unpack(">H", font[4:6])[0]
    result = {}
    for i in range(count):
        tag, _sum, offset, length = struct.unpack(">4sIII", font[12 + 16 * i:28 + 16 * i])
        result[tag.decode("latin-1")] = (offset, length)
    return result


def name_records(font, offset):
    _fmt, count, string_offset = struct.unpack(">HHH", font[offset:offset + 6])
    for i in range(count):
        start = offset + 6 + 12 * i
        platform, _enc, _lang, name_id, length, rel = struct.unpack(">HHHHHH", font[start:start + 12])
        raw = font[offset + string_offset + rel:offset + string_offset + rel + length]
        text = raw.decode("utf-16-be", "ignore") if platform in (0, 3) else raw.decode("latin-1")
        yield name_id, text


def check_font(path, errors):
    font = path.read_bytes()
    if len(font) < MIN_FONT_BYTES or font[:4] not in (b"\x00\x01\x00\x00", b"true", b"OTTO"):
        errors.append(f"{path.name}: not a usable font file")
        return
    found = tables(font)
    if "CBDT" not in found or "CBLC" not in found:
        errors.append(f"{path.name}: needs CBDT/CBLC bitmap glyphs to render on every supported Android version")
    if "name" not in found:
        errors.append(f"{path.name}: no name table")
        return
    licenses = " ".join(t.lower() for n, t in name_records(font, found["name"][0]) if n in (13, 14))
    if not any(marker in licenses for marker in LICENSE_MARKERS):
        errors.append(f"{path.name}: license is not the SIL Open Font License")


def check_catalog(errors):
    rows = [line.split("\t") for line in (ASSETS / "emoji.tsv").read_text(encoding="utf-8").splitlines() if line]
    if len(rows) < MIN_EMOJI:
        errors.append(f"emoji.tsv has {len(rows)} rows, expected {MIN_EMOJI} or more")
    seen = set()
    for row in rows:
        if len(row) != 3 or row[0] not in GROUPS or not row[1] or row[1] in seen:
            errors.append(f"emoji.tsv: bad or duplicate row {row}")
            break
        seen.add(row[1])
        if row[2] and len(row[2].split("|")) != 5:
            errors.append(f"emoji.tsv: tone list of {row[1]} must have five slots")
            break
    for group in GROUPS - {r[0] for r in rows}:
        errors.append(f"emoji.tsv: group {group} is empty")
    return seen


def check_names(catalog, errors):
    for lang, minimum in MIN_NAMED.items():
        rows = [line.split("\t") for line in (ASSETS / f"names_{lang}.tsv").read_text(encoding="utf-8").splitlines() if line]
        unknown = [r[0] for r in rows if r[0] not in catalog]
        if unknown:
            errors.append(f"names_{lang}.tsv: {len(unknown)} names point to unknown emoji, first {unknown[0]}")
        if len(rows) < minimum * len(catalog):
            errors.append(f"names_{lang}.tsv: only {len(rows)} of {len(catalog)} emoji are named")


def main():
    errors = []
    needed = ["emoji.tsv", "names_en.tsv", "names_ar.tsv", "NotoColorEmoji.ttf"]
    missing = [n for n in needed if not (ASSETS / n).is_file()]
    for name in missing:
        errors.append(f"missing {ASSETS / name}")
    if not missing:
        check_font(ASSETS / "NotoColorEmoji.ttf", errors)
        check_names(check_catalog(errors), errors)
    for error in errors:
        print("FAIL:", error)
    if errors:
        sys.exit(1)
    print("OK: emoji data, names and font are valid")


main()
