import re
import xml.etree.ElementTree as ET

MODIFIERS = {0x1F3FB: 0, 0x1F3FC: 1, 0x1F3FD: 2, 0x1F3FE: 3, 0x1F3FF: 4}
GROUPS = {
    "Smileys & Emotion": "smileys",
    "People & Body": "people",
    "Animals & Nature": "animals",
    "Food & Drink": "food",
    "Travel & Places": "travel",
    "Activities": "activities",
    "Objects": "objects",
    "Symbols": "symbols",
    "Flags": "flags",
}
GROUP_ORDER = ["smileys", "body", "people", "animals", "food", "travel", "activities", "objects", "symbols", "flags"]
GROUP_LINE = re.compile(r"^#\s*group:\s*(.+?)\s*$")
SUBGROUP_LINE = re.compile(r"^#\s*subgroup:\s*(.+?)\s*$")
# Unicode keeps hearts, cat faces and body parts inside the faces and people groups.
# Each tab here holds one kind of thing: faces only, hands and body parts only, and so on.
# subgroup -> (tab, subgroup it follows or None for the start of the tab, nudge that keeps their own order)
MOVED = {
    "cat-face": ("animals", "animal-mammal", 0.1),
    "monkey-face": ("animals", "animal-mammal", 0.2),
    "heart": ("symbols", None, 0.1),
    "emotion": ("symbols", None, 0.2),
}
BODY_SUBGROUPS = {"hands", "body-parts"}
FIRST = -100.0
ENTRY_LINE = re.compile(r"^([0-9A-Fa-f ]+?)\s*;\s*([a-z-]+)\s*#\s*\S+\s+E\d+\.\d+\s+(.*?)\s*$")
VS16 = 0xFE0F
VS15 = 0xFE0E


def classify(group, subgroup):
    if subgroup in MOVED:
        return MOVED[subgroup][0]
    if group == "people" and (subgroup in BODY_SUBGROUPS or subgroup.startswith("hand-")):
        return "body"
    return group


def subgroup_position(subgroup, ordinal, ordinals):
    moved = MOVED.get(subgroup)
    if moved is None:
        return float(ordinal)
    _tab, after, nudge = moved
    return (FIRST if after is None else float(ordinals.get(after, ordinal))) + nudge


def parse_test_file(text):
    rows = []
    ordinals = {}
    group = None
    subgroup = ""
    for line in text.splitlines():
        match = GROUP_LINE.match(line)
        if match:
            group = GROUPS.get(match.group(1))
            continue
        match = SUBGROUP_LINE.match(line)
        if match:
            subgroup = match.group(1)
            ordinals.setdefault(subgroup, len(ordinals))
            continue
        entry = ENTRY_LINE.match(line)
        if entry and group and entry.group(2) == "fully-qualified":
            codes = tuple(int(part, 16) for part in entry.group(1).split())
            rows.append((classify(group, subgroup), subgroup, codes, entry.group(3)))
    rows.sort(key=lambda r: (GROUP_ORDER.index(r[0]), subgroup_position(r[1], ordinals.get(r[1], 0), ordinals)))
    return [(g, codes, name) for g, _sub, codes, name in rows]


def strip_selectors(codes):
    return tuple(c for c in codes if c not in (VS16, VS15))


def tone_of(codes):
    tones = {MODIFIERS[c] for c in codes if c in MODIFIERS}
    return tones.pop() if len(tones) == 1 else None


def has_modifier(codes):
    return any(c in MODIFIERS for c in codes)


def without_modifiers(codes):
    return tuple(c for c in codes if c not in MODIFIERS)


def build_catalog(entries):
    bases = []
    index = {}
    for group, codes, name in entries:
        if not has_modifier(codes):
            index[strip_selectors(codes)] = len(bases)
            bases.append({"group": group, "codes": codes, "name": name, "tones": [None] * 5})
    for _group, codes, _name in entries:
        if not has_modifier(codes):
            continue
        slot = tone_of(codes)
        position = index.get(strip_selectors(without_modifiers(codes)))
        if slot is not None and position is not None:
            bases[position]["tones"][slot] = codes
    return bases


def hex_of(codes):
    return " ".join(format(c, "X") for c in codes)


def write_catalog(bases):
    lines = []
    for item in bases:
        tones = item["tones"]
        packed = "|".join(hex_of(t) if t else "" for t in tones) if all(tones) else ""
        lines.append("\t".join([item["group"], hex_of(item["codes"]), packed]))
    return "\n".join(lines) + "\n"


def text_of(codes):
    return "".join(chr(c) for c in codes)


def parse_annotations(xml_text):
    names = {}
    keywords = {}
    root = ET.fromstring(xml_text)
    for node in root.iter("annotation"):
        cp = node.get("cp")
        if not cp or node.text is None:
            continue
        key = strip_selectors(tuple(ord(c) for c in cp))
        if node.get("type") == "tts":
            names[key] = node.text.strip()
        else:
            keywords[key] = [part.strip() for part in node.text.split("|") if part.strip()]
    return names, keywords


def merge_annotations(parsed):
    names = {}
    keywords = {}
    for part_names, part_keywords in parsed:
        names.update(part_names)
        keywords.update(part_keywords)
    return names, keywords


def write_names(bases, names, keywords, fallback=None):
    lines = []
    for item in bases:
        key = strip_selectors(item["codes"])
        name = names.get(key) or (fallback(item) if fallback else None)
        if not name:
            continue
        words = keywords.get(key, [])
        lines.append("\t".join([hex_of(item["codes"]), name, "|".join(words)]))
    return "\n".join(lines) + "\n"
