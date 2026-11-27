import unittest

import emoji_data as d

SAMPLE = """# group: Smileys & Emotion
# subgroup: face-smiling
1F600                                                  ; fully-qualified     # \U0001F600 E1.0 grinning face
263A FE0F                                              ; fully-qualified     # ☺️ E0.6 smiling face
263A                                                   ; unqualified         # ☺ E0.6 smiling face
# subgroup: cat-face
1F63A                                                  ; fully-qualified     # \U0001F63A E0.6 grinning cat
# subgroup: heart
2764 FE0F                                              ; fully-qualified     # \u2764\ufe0f E0.6 red heart
# group: People & Body
# subgroup: hand-fingers-open
1F44B                                                  ; fully-qualified     # \U0001F44B E0.6 waving hand
1F44B 1F3FB                                            ; fully-qualified     # \U0001F44B\U0001F3FB E1.0 waving hand: light skin tone
1F44B 1F3FC                                            ; fully-qualified     # \U0001F44B\U0001F3FC E1.0 waving hand: medium-light skin tone
1F44B 1F3FD                                            ; fully-qualified     # \U0001F44B\U0001F3FD E1.0 waving hand: medium skin tone
1F44B 1F3FE                                            ; fully-qualified     # \U0001F44B\U0001F3FE E1.0 waving hand: medium-dark skin tone
1F44B 1F3FF                                            ; fully-qualified     # \U0001F44B\U0001F3FF E1.0 waving hand: dark skin tone
1F590 FE0F                                             ; fully-qualified     # \U0001F590️ E0.7 hand with fingers splayed
1F590 1F3FB                                            ; fully-qualified     # \U0001F590\U0001F3FB E1.0 hand with fingers splayed: light skin tone
1F91D                                                  ; fully-qualified     # \U0001F91D E3.0 handshake
1F91D 1F3FB                                            ; fully-qualified     # \U0001F91D\U0001F3FB E14.0 handshake: light skin tone
# subgroup: body-parts
1F9E0                                                  ; fully-qualified     # \U0001F9E0 E5.0 brain
# subgroup: person
1F9D1                                                  ; fully-qualified     # \U0001F9D1 E5.0 person
1FAF1 1F3FB 200D 1FAF2 1F3FC                           ; fully-qualified     # x E14.0 mixed
# group: Animals & Nature
# subgroup: animal-mammal
1F435                                                  ; fully-qualified     # \U0001F435 E0.6 monkey face
1F436                                                  ; fully-qualified     # \U0001F436 E0.6 dog face
# subgroup: animal-bird
1F426                                                  ; fully-qualified     # \U0001F426 E0.6 bird
# group: Component
1F3FB                                                  ; component           # \U0001F3FB E1.0 light skin tone
# group: Flags
1F3C1                                                  ; fully-qualified     # \U0001F3C1 E0.6 chequered flag
"""

ANNOTATIONS = """<ldml><annotations>
<annotation cp="\U0001F600">face | grin | grinning face</annotation>
<annotation cp="\U0001F600" type="tts">grinning face</annotation>
<annotation cp="☺">happy | smile</annotation>
<annotation cp="☺" type="tts">smiling face</annotation>
<annotation cp="\U0001F44B">hello | wave</annotation>
<annotation cp="\U0001F44B" type="tts">waving hand</annotation>
</annotations></ldml>"""


class CatalogTest(unittest.TestCase):
    def setUp(self):
        self.entries = d.parse_test_file(SAMPLE)
        self.bases = d.build_catalog(self.entries)

    def test_only_fully_qualified_known_groups(self):
        groups = {e[0] for e in self.entries}
        self.assertEqual(groups, {"smileys", "body", "people", "animals", "symbols", "flags"})
        self.assertNotIn((0x263A,), [e[1] for e in self.entries])

    def test_modifier_entries_fold_into_their_base(self):
        texts = [b["codes"] for b in self.bases]
        self.assertIn((0x1F44B,), texts)
        self.assertNotIn((0x1F44B, 0x1F3FB), texts)
        self.assertEqual(len(self.bases), 13)

    def test_tone_slots_follow_fitzpatrick_order(self):
        wave = next(b for b in self.bases if b["codes"] == (0x1F44B,))
        self.assertEqual([t[1] for t in wave["tones"]], [0x1F3FB, 0x1F3FC, 0x1F3FD, 0x1F3FE, 0x1F3FF])

    def test_variation_selector_does_not_break_matching(self):
        splayed = next(b for b in self.bases if b["codes"] == (0x1F590, 0xFE0F))
        self.assertEqual(splayed["tones"][0], (0x1F590, 0x1F3FB))

    def test_incomplete_or_mixed_tones_are_not_exported(self):
        text = d.write_catalog(self.bases)
        splayed = [line for line in text.splitlines() if line.split("\t")[1] == "1F590 FE0F"][0]
        self.assertEqual(splayed.split("\t")[2], "")
        handshake = [line for line in text.splitlines() if line.split("\t")[1] == "1F91D"][0]
        self.assertEqual(handshake.split("\t")[2], "")

    def test_full_tone_list_is_written(self):
        text = d.write_catalog(self.bases)
        wave = [line for line in text.splitlines() if line.split("\t")[1] == "1F44B"][0]
        self.assertEqual(wave.split("\t")[2].count("|"), 4)
        self.assertIn("1F44B 1F3FD", wave)

    def test_each_tab_holds_one_kind_of_thing(self):
        by_code = {b["codes"]: b["group"] for b in self.bases}
        self.assertEqual(by_code[(0x1F600,)], "smileys")
        self.assertEqual(by_code[(0x1F44B,)], "body")
        self.assertEqual(by_code[(0x1F9E0,)], "body")
        self.assertEqual(by_code[(0x1F9D1,)], "people")
        self.assertEqual(by_code[(0x1F63A,)], "animals")
        self.assertEqual(by_code[(0x2764, 0xFE0F)], "symbols")

    def test_moved_subgroups_sit_where_they_belong_in_the_tab(self):
        animals = [b["codes"][0] for b in self.bases if b["group"] == "animals"]
        self.assertEqual(animals, [0x1F435, 0x1F436, 0x1F63A, 0x1F426])
        order = [b["group"] for b in self.bases]
        self.assertEqual(order, sorted(order, key=d.GROUP_ORDER.index))

    def test_group_order_and_lines(self):
        first = d.write_catalog(self.bases).splitlines()[0].split("\t")
        self.assertEqual(first[:2], ["smileys", "1F600"])


class AnnotationTest(unittest.TestCase):
    def test_names_and_keywords_match_through_selectors(self):
        bases = d.build_catalog(d.parse_test_file(SAMPLE))
        names, keywords = d.parse_annotations(ANNOTATIONS)
        text = d.write_names(bases, names, keywords)
        lines = {line.split("\t")[0]: line.split("\t") for line in text.splitlines()}
        self.assertEqual(lines["263A FE0F"][1], "smiling face")
        self.assertEqual(lines["263A FE0F"][2], "happy|smile")
        self.assertEqual(lines["1F44B"][2], "hello|wave")
        self.assertNotIn("1F3C1", lines)

    def test_fallback_name_fills_gaps(self):
        bases = d.build_catalog(d.parse_test_file(SAMPLE))
        text = d.write_names(bases, {}, {}, fallback=lambda item: item["name"])
        self.assertIn("1F3C1\tchequered flag\t", text)

    def test_merge_prefers_later_sources(self):
        merged = d.merge_annotations([({(1,): "a"}, {}), ({(1,): "b"}, {(2,): ["k"]})])
        self.assertEqual(merged[0][(1,)], "b")
        self.assertEqual(merged[1][(2,)], ["k"])


if __name__ == "__main__":
    unittest.main()
