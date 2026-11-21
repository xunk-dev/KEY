import os
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

import emoji_data as data

TEST_URLS = [
    "https://unicode.org/Public/emoji/latest/emoji-test.txt",
    "https://www.unicode.org/Public/emoji/latest/emoji-test.txt",
    "https://unicode.org/Public/UCD/latest/emoji/emoji-test.txt",
]
FONT_URLS = [
    "https://raw.githubusercontent.com/googlefonts/noto-emoji/main/2D/fonts/NotoColorEmoji.ttf",
    "https://github.com/googlefonts/noto-emoji/raw/main/2D/fonts/NotoColorEmoji.ttf",
    "https://raw.githubusercontent.com/googlefonts/noto-emoji/main/fonts/NotoColorEmoji.ttf",
    "https://github.com/googlefonts/noto-emoji/raw/main/fonts/NotoColorEmoji.ttf",
]
CLDR = "https://raw.githubusercontent.com/unicode-org/cldr/main/common/{kind}/{lang}.xml"
LANGUAGES = ("en", "ar")
MIN_EMOJI = 1300
ATTEMPTS = 3


def get(url):
    request = urllib.request.Request(url, headers={"User-Agent": "space-keyboard-data"})
    with urllib.request.urlopen(request, timeout=120) as response:
        return response.read()


def fetch(urls, optional=False):
    errors = []
    for url in urls:
        for attempt in range(ATTEMPTS):
            try:
                return get(url)
            except (urllib.error.URLError, TimeoutError, OSError) as error:
                errors.append(f"{url}: {error}")
                time.sleep(2 * (attempt + 1))
    if optional:
        return None
    sys.exit("download failed:\n" + "\n".join(errors))


def candidates(env_name, defaults):
    override = os.environ.get(env_name)
    return [override] if override else defaults


def annotations(lang):
    parts = []
    for kind in ("annotationsDerived", "annotations"):
        raw = fetch([CLDR.format(kind=kind, lang=lang)], optional=kind == "annotationsDerived")
        if raw:
            parts.append(data.parse_annotations(raw.decode("utf-8")))
    return data.merge_annotations(parts)


def main(out_dir):
    out = Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)
    entries = data.parse_test_file(fetch(candidates("EMOJI_TEST_URL", TEST_URLS)).decode("utf-8"))
    bases = data.build_catalog(entries)
    if len(bases) < MIN_EMOJI:
        sys.exit(f"only {len(bases)} emoji parsed, expected at least {MIN_EMOJI}")
    (out / "emoji.tsv").write_text(data.write_catalog(bases), encoding="utf-8")
    for lang in LANGUAGES:
        names, keywords = annotations(lang)
        fallback = (lambda item: item["name"]) if lang == "en" else None
        (out / f"names_{lang}.tsv").write_text(data.write_names(bases, names, keywords, fallback), encoding="utf-8")
    (out / "NotoColorEmoji.ttf").write_bytes(fetch(candidates("EMOJI_FONT_URL", FONT_URLS)))
    print(f"OK: {len(bases)} emoji written to {out}")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "feature-emoji/src/main/assets/emoji")
