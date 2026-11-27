# Emoji suggestions, suggestions per app and field, stricter incognito

## Incognito

- The toggle already existed (tools panel and Privacy and data). It stopped learning but still ranked suggestions from the learned words.
- Now, while incognito is on (and in fields that ask for no personalised learning), suggestions come only from the built-in lists: the learned words, learned pairs and per-app words are neither read nor written.
- The learning flag is read live from TypingExtras, so a glide right after the toggle no longer uses the previous field's state.
- Long-pressing a suggestion still removes it everywhere (also from the per-app words).

## Emoji while typing

- When the word being typed exactly matches an emoji, that emoji is the last item of the suggestion bar. Tapping it keeps the word and adds a space, the emoji and a space.
- Matching: a curated list (assets/emoji/hints.tsv, Arabic dialect and English words, checked against the installed emoji set so unsupported glyphs are dropped) wins; otherwise only an unambiguous match is used (a name of one word, or a keyword that exactly one emoji has). Words of one or two Latin letters are matched only from the curated list. Arabic is normalised like emoji search and a leading definite article is tried off.
- Not offered when the cursor is inside a word, in fields without suggestions (password, email, URL, raw keys), or when the switch Settings > Typing > Suggest an emoji is off. Holding the emoji does nothing (it is never blocked as a word). Skin tone follows the chosen tone. The pick goes to recent emoji only when learning is on.
- Limit: the bar only shows while a word is being typed, so the emoji is offered before the space, not after it.

## Suggestions per app and field

- Words are also counted per scope: the app package for ordinary text, and one shared scope each for search boxes, name fields and address fields (kind from the input type, or the Search action).
- In an app, its own words rank above other learned words. In search, name and address fields the learned words of normal writing are not used, only that kind's own words and the built-in lists, and these fields do not teach the normal words (so names do not pollute chat suggestions).
- Stored in a private file (space_scoped_words): at most 40 scopes and 300 words each, least recently used scope dropped first. Not part of backup. Cleared by Clear learned words. Not recorded in password fields, in incognito or when the field asks for no personalised learning.
- Switch: Settings > Typing > Adapt suggestions to each app and field (on by default; off restores the old single list).

## Files

- core/suggest: Personal, FieldKind, ScopedStats, ScopedStore, Suggester. core/layout: KeyboardPrefs, KeyboardPrefsStore (emojiSuggestions, appSuggestions).
- feature-emoji: model/EmojiHints, model/HintParser, data/EmojiRepository, EmojiController, assets/emoji/hints.tsv.
- ime: EmojiOffer, PersonalVocabulary, GlideStyle, SuggestionController, FieldSetup, TypingExtras, SpaceInputService, layout/LayoutController.
- app: TypingSection, DataSettingsScreen, strings (en, ar). docs/PRIVACY.md.
