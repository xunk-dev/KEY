#!/usr/bin/env bash
set -euo pipefail
ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"
echo "[1/2] project checks"
bash scripts/check-all.sh
if [ -n "${TERMUX_VERSION:-}" ] || [[ "${PREFIX:-}" == *com.termux* ]]; then
  echo "[2/2] Termux detected: compile skipped, CI will compile"
  echo "OK: safe to push"
  exit 0
fi
echo "[2/2] compile"
if [ -x ./gradlew ]; then
  if [ "${FULL:-0}" = "1" ]; then
    ./gradlew :app:lintDebug
  else
    ./gradlew compileDebugKotlin
  fi
else
  echo "gradlew not found, skipping compile"
fi
echo "OK: safe to push"
