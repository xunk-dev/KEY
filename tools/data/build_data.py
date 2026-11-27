import argparse
import sys
from pathlib import Path

import faces
import symbols
import ucd

DEFAULT_OUT = Path("feature-symbols/src/main/assets/symbols")
DEFAULT_CACHE = Path("build/data-cache")
DEFAULT_REF = "main"


def parse(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument("--out", default=str(DEFAULT_OUT))
    parser.add_argument("--cache", default=str(DEFAULT_CACHE))
    parser.add_argument("--source")
    parser.add_argument("--cldr-ref", default=DEFAULT_REF)
    return parser.parse_args(argv)


def load(args):
    def path(name):
        return ucd.locate(name, args.source, args.cache, args.cldr_ref)

    return (
        ucd.read_characters(path("UnicodeData.txt")),
        ucd.read_emoji_presentation(path("emoji-data.txt")),
        ucd.read_annotations(path("en.xml")),
        ucd.read_annotations(path("ar.xml")),
    )


def write_all(out, found, rows):
    out.mkdir(parents=True, exist_ok=True)
    (out / "symbols.tsv").write_text(symbols.render_symbols(found), encoding="utf-8")
    (out / "featured.tsv").write_text(symbols.render_featured(found), encoding="utf-8")
    (out / "faces.tsv").write_text(faces.render_faces(rows), encoding="utf-8")
    (out / "face_categories.tsv").write_text(faces.render_categories(), encoding="utf-8")


def main(argv=None):
    args = parse(sys.argv[1:] if argv is None else argv)
    characters, emoji, english, arabic = load(args)
    found = symbols.build_symbols(characters, emoji, english, arabic)
    found = symbols.force_featured(found, characters, english, arabic)
    rows = faces.build_faces()
    write_all(Path(args.out), found, rows)
    print(f"symbols: {len(found)}, faces: {len(rows)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
