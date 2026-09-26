#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET_DIR="$ROOT_DIR/chess-tournament/target"

JAR=""

for candidate in "$TARGET_DIR"/chess-tournament-*-runner.jar; do
  if [[ -f "$candidate" && -s "$candidate" ]]; then
    JAR="$candidate"
  fi
done

if [[ -z "$JAR" ]]; then
  echo "Runner introuvable."
  echo "Exécutez d'abord : mvn verify"
  exit 1
fi

exec java -jar "$JAR" "$@"
