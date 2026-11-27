import tempfile
import unittest
from pathlib import Path

import support
import build_data
import faces
import symbols
import ucd
import verify_data
from symbol_rules import CATEGORY_IDS, classify


class Pipeline(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.root = tempfile.TemporaryDirectory()
        base = Path(cls.root.name)
        (base / "src").mkdir()
        support.write_sources(base / "src")
        cls.code = build_data.main(["--source", str(base / "src"), "--out", str(base / "out")])
        cls.out = base / "out"
        cls.characters = ucd.read_characters(base / "src" / "UnicodeData.txt")

    @classmethod
    def tearDownClass(cls):
        cls.root.cleanup()

    def table(self):
        result = {}
        for line in (self.out / "symbols.tsv").read_text(encoding="utf-8").splitlines():
            if not line.startswith("#"):
                parts = line.split("\t")
                result[int(parts[0], 16)] = parts
        return result

    def test_build_succeeds_and_verifies(self):
        self.assertEqual(0, self.code)
        self.assertEqual(0, verify_data.main([str(self.out)]))

    def test_featured_symbols_get_expected_categories(self):
        table = self.table()
        expected = {
            0x20AC: "currency", 0x7B: "brackets", 0x7D: "brackets", 0x2713: "marks", 0xB6: "signs",
            0x3C0: "greek", 0xAE: "signs", 0xA9: "signs", 0x2206: "math", 0x2605: "stars", 0x272A: "stars",
            0x2765: "hearts", 0x2667: "games", 0xB0: "signs", 0x25CF: "shapes", 0xD7: "math", 0x221A: "math",
            0xFDFC: "currency", 0x61F: "arabic", 0x2190: "arrows", 0x2460: "circled", 0x2500: "frames",
            0x2764: "hearts", 0x266A: "music", 0x2122: "signs", 0x30: "numerals", 0x21: "punctuation",
        }
        for cp, category in expected.items():
            self.assertEqual(category, table[cp][1], hex(cp))

    def test_emoji_presentation_and_letters_are_excluded(self):
        table = self.table()
        for cp in (0x1F600, 0x2B50, 0x2705, 0x61, 0x41, 0x300, 0x20, 0x200D):
            self.assertNotIn(cp, table, hex(cp))

    def test_names_come_from_cldr_with_unicode_fallback(self):
        table = self.table()
        self.assertEqual("euro", table[0x20AC][3])
        self.assertEqual("currency|euro", table[0x20AC][4])
        self.assertEqual("يورو", table[0x20AC][5])
        self.assertEqual("عملة|يورو", table[0x20AC][6])
        self.assertEqual("", table[0x2665][3])
        self.assertEqual("black heart suit", table[0x2665][2])
        self.assertEqual("red heart", table[0x2764][3])

    def test_every_symbol_has_one_known_category(self):
        for parts in self.table().values():
            self.assertIn(parts[1], CATEGORY_IDS)

    def test_each_row_is_tab_clean(self):
        for line in (self.out / "symbols.tsv").read_text(encoding="utf-8").splitlines()[1:]:
            self.assertEqual(7, len(line.split("\t")))

    def test_featured_file_keeps_the_manual_order(self):
        listed = [int(x, 16) for x in (self.out / "featured.tsv").read_text().splitlines()[1:]]
        self.assertEqual(list(symbols.FEATURED[:16]), listed[:16])
        self.assertEqual(listed, [cp for cp in symbols.FEATURED if cp in listed])

    def test_classify_drops_unmatched_characters(self):
        self.assertIsNone(classify(0x1D400, self.characters[0x1D400]))
        self.assertIsNone(classify(0x2800, self.characters[0x2800]))


class Faces(unittest.TestCase):
    def test_counts_and_required_faces(self):
        rows = faces.build_faces()
        texts = [face for _, face in rows]
        self.assertGreaterEqual(len(texts), 3000)
        self.assertEqual(len(texts), len(set(texts)))
        for needed in verify_data.REQUIRED_FACES:
            self.assertIn(needed, texts)

    def test_every_category_has_faces(self):
        found = {category for category, _ in faces.build_faces()}
        self.assertEqual({item[0] for item in faces.CATEGORIES}, found)

    def test_faces_are_one_line_without_marks(self):
        for _, face in faces.build_faces():
            self.assertNotIn("\t", face)
            self.assertNotIn("\n", face)
            self.assertEqual(face, face.strip())

    def test_generation_is_deterministic(self):
        self.assertEqual(faces.build_faces(), faces.build_faces())


class Annotations(unittest.TestCase):
    def test_sequences_are_skipped_and_variation_selector_is_ignored(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "en.xml"
            path.write_text(support.ENGLISH.replace('cp="❤"', 'cp="❤\ufe0f"'), encoding="utf-8")
            table = ucd.read_annotations(path)
        self.assertIn(0x2764, table)
        self.assertNotIn(0x1F468, table)
        self.assertEqual("red heart", table[0x2764].name)


if __name__ == "__main__":
    unittest.main()
