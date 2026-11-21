from itertools import product

from faces_curated import CATEGORIES, CURATED
from faces_data import BRACKETS, CHEEKS, EYES, MOUTHS, PLAIN, TAILS

CAP = 260
CLASSIC_CAP = 240
ARMS = (("", ""), ("ヽ", "ﾉ"), ("ヾ", "ﾉ"), ("٩", "۶"), ("ლ", "ლ"), ("ᕕ", "ᕗ"), ("o", "o"), ("つ", "づ"))
ANIMAL_TEMPLATES = (
    "ʕ{e}ᴥ{e}ʔ", "(=^{e}ω{e}^=)", "U{e}ᴥ{e}U", "({e}ω{e})", "ฅ^{e}ω{e}^ฅ", "(´{e}ω{e}`)", "૮ {e}ᴥ{e} ა",
    "(ᵔ{e}ᴥ{e}ᵔ)", "(◍{e}ω{e}◍)", "V{e}ᴥ{e}V",
)


def spread(items, cap):
    if len(items) <= cap:
        return list(items)
    return [items[index * len(items) // cap] for index in range(cap)]


def unique(items):
    seen = set()
    result = []
    for item in items:
        if item not in seen:
            seen.add(item)
            result.append(item)
    return result


def core(bracket, cheek, eyes, mouth, tail):
    left, right = bracket
    return f"{left}{cheek}{eyes[0]}{mouth}{eyes[1]}{cheek}{right}{tail}"


def mood_faces(category):
    brackets = PLAIN if category in ("sad", "angry", "sleepy") else BRACKETS
    tails = TAILS.get(category, TAILS["default"])
    pool = product(brackets, CHEEKS[category], EYES[category], MOUTHS[category], tails)
    return [core(*combo) for combo in pool]


def classic_faces():
    pool = product((":", ";", "=", "8"), ("", "-", "^", "o"), (")", "(", "D", "P", "O", "/", "|", "*", "3", "]", "[", "c", "S", "$", "@"))
    return [f"{eyes}{nose}{mouth}" for eyes, nose, mouth in pool]


def animal_faces():
    pool = product(ANIMAL_TEMPLATES, EYES["animals"], MOUTHS["animals"][:1])
    return [template.format(e=eyes[0]) for template, eyes, _ in pool]


def action_faces():
    pool = product(ARMS, EYES["actions"], MOUTHS["actions"])
    return [f"{arms[0]}({eyes[0]}{mouth}{eyes[1]}){arms[1]}" for arms, eyes, mouth in pool]


def generated(category):
    if category == "classic":
        return spread(classic_faces(), CLASSIC_CAP)
    if category == "animals":
        return spread(animal_faces(), CAP)
    if category == "actions":
        return spread(action_faces(), CAP)
    return spread(mood_faces(category), CAP)


def build_faces():
    taken = set()
    rows = []
    for category, _, _ in CATEGORIES:
        for face in unique(list(CURATED[category]) + generated(category)):
            if face not in taken:
                taken.add(face)
                rows.append((category, face))
    return rows


def render_faces(rows):
    lines = ["#space-keyboard-faces 1"]
    lines += [f"{category}\t{face}" for category, face in rows]
    return "\n".join(lines) + "\n"


def render_categories():
    lines = ["#space-keyboard-face-categories 1"]
    lines += [f"{category}\t{english}\t{arabic}" for category, english, arabic in CATEGORIES]
    return "\n".join(lines) + "\n"
