import sys
import unicodedata
from pathlib import Path

from faces_curated import CATEGORIES as FACE_CATEGORIES
from symbol_rules import CATEGORY_IDS
from symbols import FEATURED

DEFAULT_DIR = Path("feature-symbols/src/main/assets/symbols")
MIN_SYMBOLS = 1500
MIN_FACES = 2500
MIN_PER_CATEGORY = 5
REQUIRED_FACES = ("^_^", "^o^", "(◔‿◔)")
FORBIDDEN_MARKS = {"Mn", "Mc", "Me", "Cf", "Cc", "Co", "Cn", "Cs"}
MAX_FACE = 40


def rows(path, columns):
    result = []
    for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if line.startswith("#") or not line:
            continue
        parts = line.split("\t")
        if len(parts) != columns:
            raise ValueError(f"{path.name}:{number}: expected {columns} columns, found {len(parts)}")
        result.append(parts)
    return result


def check_symbols(directory, errors):
    entries = rows(directory / "symbols.tsv", 7)
    codes = [int(entry[0], 16) for entry in entries]
    if len(set(codes)) != len(codes):
        errors.append("symbols.tsv has duplicate code points")
    if len(entries) < MIN_SYMBOLS:
        errors.append(f"symbols.tsv has only {len(entries)} entries")
    counts = {}
    for entry in entries:
        counts[entry[1]] = counts.get(entry[1], 0) + 1
        if entry[1] not in CATEGORY_IDS:
            errors.append(f"unknown category {entry[1]}")
    for category in CATEGORY_IDS:
        if counts.get(category, 0) < MIN_PER_CATEGORY:
            errors.append(f"category {category} has {counts.get(category, 0)} entries")
    return set(codes)


def check_featured(directory, codes, errors):
    listed = [int(entry[0], 16) for entry in rows(directory / "featured.tsv", 1)]
    for cp in FEATURED:
        if cp not in codes:
            errors.append(f"featured U+{cp:04X} is missing from symbols.tsv")
        if cp not in listed:
            errors.append(f"featured U+{cp:04X} is missing from featured.tsv")


def check_faces(directory, errors):
    entries = rows(directory / "faces.tsv", 2)
    known = {item[0] for item in FACE_CATEGORIES}
    faces = [entry[1] for entry in entries]
    if len(set(faces)) != len(faces):
        errors.append("faces.tsv has duplicate faces")
    if len(faces) < MIN_FACES:
        errors.append(f"faces.tsv has only {len(faces)} faces")
    for category, face in entries:
        if category not in known:
            errors.append(f"unknown face category {category}")
        if len(face) > MAX_FACE or face != face.strip():
            errors.append(f"bad face {face!r}")
        for ch in face:
            if unicodedata.category(ch) in FORBIDDEN_MARKS or ord(ch) >= 0x1F000:
                errors.append(f"face {face!r} has forbidden U+{ord(ch):04X}")
                break
    for needed in REQUIRED_FACES:
        if needed not in faces:
            errors.append(f"required face {needed} is missing")
    meta = rows(directory / "face_categories.tsv", 3)
    if {item[0] for item in meta} != known:
        errors.append("face_categories.tsv does not match the face categories")


def main(argv=None):
    args = sys.argv[1:] if argv is None else argv
    directory = Path(args[0]) if args else DEFAULT_DIR
    errors = []
    try:
        codes = check_symbols(directory, errors)
        check_featured(directory, codes, errors)
        check_faces(directory, errors)
    except (OSError, ValueError) as error:
        errors.append(str(error))
    for error in errors[:50]:
        print("FAIL:", error)
    if errors:
        return 1
    print("OK: symbol and face data verified")
    return 0


if __name__ == "__main__":
    sys.exit(main())
