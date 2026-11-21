from pathlib import Path
import json
import re
import sys

RAW = Path("core/src/main/res/raw")
CATALOG = Path("core/src/main/java/com/spacekeyboard/keyboard/core/layout/LayoutCatalog.kt")
ARABIC = set("ابتثجحخدذرزسشصضطظعغفقكلمنهويءئؤةى")
LATIN = set("abcdefghijklmnopqrstuvwxyz")
PERSIAN = set("ابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهی")
ESSENTIAL = ("DELETE", "SPACE", "ENTER", "LANGUAGE")
SYMBOLS = "num"
EDIT = "edit"
EDIT_ACTIONS = {"EDIT", "SELECT_ALL", "COPY", "CUT", "PASTE", "SELECT_MODE", "LEFT", "RIGHT", "UP", "DOWN", "WORD_LEFT", "WORD_RIGHT", "LINE_START", "LINE_END", "UNDO", "REDO"}
FIXED = ("SPACE", "SPACER")
ACTIONS = {"COMMIT", "SPACE", "DELETE", "ENTER", "LANGUAGE", "SPACER", "SHIFT", "NUMBERS"} | EDIT_ACTIONS


def derived(layout, label):
    return layout + "_" + "".join(format(ord(c), "x") for c in label)


def normalize(layout, node):
    if isinstance(node, str):
        return {"id": derived(layout, node), "label": node, "action": "COMMIT", "weight": 1.0, "movable": True}
    action = node.get("action", "COMMIT")
    label = node.get("label", "")
    key = node.get("id") or (derived(layout, label) if label else "")
    return {
        "id": key,
        "label": label,
        "action": action,
        "weight": float(node.get("weight", 1)),
        "movable": node.get("movable", action not in FIXED),
    }


def check(path, errors):
    data = json.loads(path.read_text(encoding="utf-8"))
    layout = data["id"]
    if layout != path.stem.replace("layout_", ""):
        errors.append(f"{path.name}: id {layout} does not match the file name")
    rows = [[normalize(layout, n) for n in row] for row in data["rows"]]
    keys = [k for row in rows for k in row]
    ids = [k["id"] for k in keys]
    if "" in ids or len(set(ids)) != len(ids):
        errors.append(f"{path.name}: missing or duplicate key ids")
    for k in keys:
        if k["action"] not in ACTIONS:
            errors.append(f"{path.name}: unknown action {k['action']}")
        if k["weight"] <= 0:
            errors.append(f"{path.name}: non positive weight for {k['id']}")
    for action in ESSENTIAL + ("NUMBERS",):
        if layout in (SYMBOLS, EDIT) and action == "LANGUAGE":
            continue
        if layout == EDIT and action == "NUMBERS":
            continue
        if sum(1 for k in keys if k["action"] == action) != 1:
            errors.append(f"{path.name}: expected exactly one {action} key")
    totals = [round(sum(k["weight"] for k in row), 3) for row in rows]
    if len(set(totals)) != 1:
        errors.append(f"{path.name}: rows have different total weights {totals}")
    if any(k["action"] in FIXED and k["movable"] for k in keys):
        errors.append(f"{path.name}: space and spacers must not be movable")
    letters = {k["label"] for k in keys if k["action"] == "COMMIT"}
    required = ARABIC if layout == "ar" else LATIN if layout == "en" else PERSIAN if layout == "fa" else set()
    missing = required - letters
    if missing:
        errors.append(f"{path.name}: missing letters {''.join(sorted(missing))}")


def main():
    errors = []
    files = sorted(RAW.glob("layout_*.json"))
    catalog = set(re.findall(r'LayoutEntry\("(\w+)"', CATALOG.read_text(encoding="utf-8")))
    if catalog != {f.stem.replace("layout_", "") for f in files}:
        errors.append(f"LayoutCatalog {sorted(catalog)} does not match files {[f.name for f in files]}")
    for f in files:
        check(f, errors)
    for e in errors:
        print("FAIL:", e)
    if errors:
        sys.exit(1)
    print(f"OK: {len(files)} keyboard layouts are valid")


main()
