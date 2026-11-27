import re
import time
import urllib.request
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from pathlib import Path

UCD_BASE = "https://www.unicode.org/Public/UCD/latest/ucd/"
CLDR_BASE = "https://raw.githubusercontent.com/unicode-org/cldr/{ref}/common/annotations/"
SOURCES = {
    "UnicodeData.txt": UCD_BASE + "UnicodeData.txt",
    "emoji-data.txt": UCD_BASE + "emoji/emoji-data.txt",
    "en.xml": CLDR_BASE + "en.xml",
    "ar.xml": CLDR_BASE + "ar.xml",
}
VS16 = "\ufe0f"
TRIES = 4


@dataclass(frozen=True)
class Character:
    name: str
    category: str


@dataclass
class Annotation:
    name: str = ""
    keywords: list = field(default_factory=list)


def fetch(url, destination):
    if destination.exists() and destination.stat().st_size > 0:
        return destination
    destination.parent.mkdir(parents=True, exist_ok=True)
    request = urllib.request.Request(url, headers={"User-Agent": "space-keyboard-data/1"})
    last = None
    for attempt in range(TRIES):
        try:
            with urllib.request.urlopen(request, timeout=60) as response:
                data = response.read()
            if not data:
                raise OSError("empty response")
            destination.write_bytes(data)
            return destination
        except OSError as error:
            last = error
            time.sleep(2 * (attempt + 1))
    raise SystemExit(f"download failed: {url}: {last}")


def locate(name, source, cache, cldr_ref):
    if source is not None:
        path = Path(source) / name
        if not path.exists():
            raise SystemExit(f"missing source file {path}")
        return path
    return fetch(SOURCES[name].format(ref=cldr_ref), Path(cache) / name)


def read_characters(path):
    table = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        parts = line.split(";")
        if len(parts) < 3:
            continue
        name = parts[1]
        if name.startswith("<"):
            continue
        table[int(parts[0], 16)] = Character(name.lower(), parts[2])
    return table


def read_emoji_presentation(path):
    found = set()
    pattern = re.compile(r"^([0-9A-Fa-f]+)(?:\.\.([0-9A-Fa-f]+))?\s*;\s*(\w+)")
    for line in path.read_text(encoding="utf-8").splitlines():
        match = pattern.match(line)
        if match and match.group(3) == "Emoji_Presentation":
            low = int(match.group(1), 16)
            high = int(match.group(2) or match.group(1), 16)
            found.update(range(low, high + 1))
    return found


def single(cp):
    text = cp[:-1] if cp.endswith(VS16) else cp
    return ord(text) if len(text) == 1 else None


def read_annotations(path):
    table = {}
    for _, node in ET.iterparse(path):
        if node.tag != "annotation":
            continue
        cp = single(node.get("cp", ""))
        text = (node.text or "").strip()
        if cp is not None and text:
            entry = table.setdefault(cp, Annotation())
            if node.get("type") == "tts":
                entry.name = text
            else:
                entry.keywords = [word.strip() for word in text.split("|") if word.strip()]
        node.clear()
    return table
