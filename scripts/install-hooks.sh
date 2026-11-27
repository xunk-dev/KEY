#!/usr/bin/env bash
set -euo pipefail
ROOT="$(git rev-parse --show-toplevel)"
HOOK="$ROOT/.git/hooks/pre-push"
SHELL_PATH="$(command -v bash)"
printf '#!%s\nexec bash scripts/pre-push.sh\n' "$SHELL_PATH" > "$HOOK"
chmod +x "$HOOK" "$ROOT/scripts/pre-push.sh"
if ! command -v python3 >/dev/null 2>&1; then
  echo "python3 missing: run  pkg install python"
fi
echo "pre-push hook installed"
