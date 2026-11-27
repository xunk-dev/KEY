from pathlib import Path
import sys
import xml.etree.ElementTree as ET


def names(path):
    return {e.get("name") for e in ET.parse(path).getroot()}


def main():
    failed = False
    for base in sorted(Path(".").glob("*/src/main/res/values/strings.xml")):
        arabic = base.parent.parent / "values-ar" / "strings.xml"
        if not arabic.exists():
            print(f"FAIL: {arabic} is missing")
            failed = True
            continue
        only_en = names(base) - names(arabic)
        only_ar = names(arabic) - names(base)
        for name in sorted(only_en):
            print(f"FAIL: {base.parent.parent.parent.parent.parent.name} string {name} has no Arabic translation")
        for name in sorted(only_ar):
            print(f"FAIL: {base.parent.parent.parent.parent.parent.name} string {name} has no English source")
        failed = failed or bool(only_en or only_ar)
    if failed:
        sys.exit(1)
    print("OK: English and Arabic strings match")


main()
