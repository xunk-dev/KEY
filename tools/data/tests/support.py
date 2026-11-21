import sys
import unicodedata
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

EMOJI_PRESENTATION = (
    "231A..231B", "23E9..23EC", "23F0..23F0", "23F3..23F3", "25FD..25FE", "2614..2615", "2648..2653",
    "267F..267F", "2693..2693", "26A1..26A1", "26AA..26AB", "26BD..26BE", "26C4..26C5", "26CE..26CE",
    "26D4..26D4", "26EA..26EA", "26F2..26F3", "26F5..26F5", "26FA..26FA", "26FD..26FD", "2705..2705",
    "270A..270B", "2728..2728", "274C..274C", "274E..274E", "2753..2755", "2757..2757", "2795..2797",
    "27B0..27B0", "27BF..27BF", "2B1B..2B1C", "2B50..2B50", "2B55..2B55", "1F300..1FAFF",
)

ENGLISH = """<ldml><annotations>
<annotation cp="€">currency | euro</annotation>
<annotation cp="€" type="tts">euro</annotation>
<annotation cp="★">favorite | star</annotation>
<annotation cp="★" type="tts">black star</annotation>
<annotation cp="❤">heart | love</annotation>
<annotation cp="❤" type="tts">red heart</annotation>
<annotation cp="👨‍👩">family</annotation>
</annotations></ldml>"""

ARABIC = """<ldml><annotations>
<annotation cp="€">عملة | يورو</annotation>
<annotation cp="€" type="tts">يورو</annotation>
<annotation cp="★">نجمة | مفضل</annotation>
<annotation cp="★" type="tts">نجمة سوداء</annotation>
</annotations></ldml>"""


def unicode_data_text(limit=0x20000):
    lines = []
    for cp in range(limit):
        name = unicodedata.name(chr(cp), "")
        if name:
            lines.append(f"{cp:04X};{name};{unicodedata.category(chr(cp))};0;ON;;;;;N;;;;;")
    return "\n".join(lines) + "\n"


def emoji_data_text():
    return "\n".join(f"{item} ; Emoji_Presentation # fixture" for item in EMOJI_PRESENTATION) + "\n"


def write_sources(directory):
    directory = Path(directory)
    (directory / "UnicodeData.txt").write_text(unicode_data_text(), encoding="utf-8")
    (directory / "emoji-data.txt").write_text(emoji_data_text(), encoding="utf-8")
    (directory / "en.xml").write_text(ENGLISH, encoding="utf-8")
    (directory / "ar.xml").write_text(ARABIC, encoding="utf-8")
