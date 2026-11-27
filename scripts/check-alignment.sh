#!/usr/bin/env bash
set -euo pipefail
apk="$1"
zipalign="$(find "${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/usr/local/lib/android/sdk}}/build-tools" -name zipalign -type f | sort -V | tail -1)"
test -x "$zipalign"
"$zipalign" -c -P 16 -v 4 "$apk" | tail -n 5
