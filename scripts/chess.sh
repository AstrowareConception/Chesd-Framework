#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET_DIR="$ROOT_DIR/chess-tournament/target"

JAR="$(find "$TARGET_DIR" -maxdepth 1 -type f -name 'chess-tournament-*-runner.jar' -print 2>/dev/null | sort | tail -n 1 || true)"

if [[ -z "$JAR" || ! -s "$JAR" ]]; then
  echo "Runner introuvable."
  echo "Exécutez d'abord : mvn verify"
  exit 1
fi

exec java -jar "$JAR" "$@"
