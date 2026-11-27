import re
import unittest
from pathlib import Path

import support
from faces_curated import CATEGORIES
from symbol_rules import CATEGORY_IDS

MODEL = Path(__file__).resolve().parents[3] / "feature-symbols/src/main/java/com/spacekeyboard/keyboard/symbols/model"


def ids(name):
    text = (MODEL / name).read_text(encoding="utf-8")
    return re.findall(r'^\s+[A-Z_]+\("([a-z]+)", R\.string\.', text, re.MULTILINE)


class KotlinSync(unittest.TestCase):
    def test_symbol_categories_match_the_enum(self):
        self.assertEqual({"featured"} | set(CATEGORY_IDS), set(ids("SymbolCategory.kt")))

    def test_face_categories_match_the_enum(self):
        self.assertEqual([item[0] for item in CATEGORIES], ids("FaceEntry.kt"))


if __name__ == "__main__":
    unittest.main()
