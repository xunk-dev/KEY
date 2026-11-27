#!/usr/bin/env bash
set -uo pipefail
MAX=4
WAIT=20
LOG="$(mktemp)"
trap 'rm -f "$LOG"' EXIT
attempt=1
while true; do
  gradle "$@" 2>&1 | tee "$LOG"
  code=${PIPESTATUS[0]}
  [ "$code" -eq 0 ] && exit 0
  if [ "$attempt" -ge "$MAX" ] || ! grep -qE "Could not GET|status code (429|5[0-9][0-9])|timed out|Connection reset|Remote host terminated" "$LOG"; then
    exit "$code"
  fi
  echo "Network problem while resolving dependencies. Retry $attempt/$MAX in ${WAIT}s"
  sleep "$WAIT"
  attempt=$((attempt + 1))
  WAIT=$((WAIT * 2))
done
