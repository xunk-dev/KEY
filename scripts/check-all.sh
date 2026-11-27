#!/usr/bin/env bash
set -uo pipefail
FAILED=()
run() {
  echo "::group::$*"
  if ! "$@"; then
    FAILED+=("$*")
  fi
  echo "::endgroup::"
}
run bash scripts/check-lines.sh
run bash scripts/check-comments.sh
run bash scripts/check-binaries.sh
run bash scripts/check-function-lines.sh
run python3 scripts/check-contrast.py
run python3 scripts/check-themes.py
run python3 scripts/check-layouts.py
run python3 scripts/check-strings.py
run bash scripts/check-fonts.sh
run python3 scripts/check-manifest-permissions.py
if [ "${#FAILED[@]}" -ne 0 ]; then
  echo "FAILED CHECKS (${#FAILED[@]}):"
  printf ' - %s\n' "${FAILED[@]}"
  exit 1
fi
echo "OK: all checks passed"
