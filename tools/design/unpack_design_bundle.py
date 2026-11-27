import base64
import gzip
import json
import re
import sys
from pathlib import Path


def block(source, name):
    match = re.search(r'<script type="__bundler/%s">\s*(.*?)\s*</script>' % name, source, re.S)
    return match.group(1)


def decode(entry):
    data = base64.b64decode(entry["data"])
    return gzip.decompress(data) if entry.get("compressed") else data


def main(bundle, out):
    out = Path(out)
    out.mkdir(parents=True, exist_ok=True)
    source = Path(bundle).read_text(encoding="utf-8")
    manifest = json.loads(block(source, "manifest"))
    for uuid in json.loads(block(source, "page_order")):
        page = decode(manifest[uuid]).decode("utf-8")
        inner = json.loads(block(page, "template"))
        (out / (uuid[:8] + ".html")).write_text(inner, encoding="utf-8")
        print(uuid[:8], len(inner))


main(sys.argv[1], sys.argv[2])
