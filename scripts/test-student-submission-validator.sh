#!/usr/bin/env bash
set -euo pipefail

SOURCE_ROOT="$(git rev-parse --show-toplevel)"
TEMP_ROOT="$(mktemp -d)"
trap 'rm -rf "$TEMP_ROOT"' EXIT

git clone --quiet "$SOURCE_ROOT" "$TEMP_ROOT/repo"
cd "$TEMP_ROOT/repo"

git config user.name "Chess Framework CI"
git config user.email "ci@example.invalid"

BASE_SHA="$(git rev-parse HEAD)"

echo "== Scénario 1 : soumission étudiante conforme =="

bash scripts/new-student-bot.sh   ValidatorSelfTestBot   "CI Validator"   "Validator Self Test"   "Fixture locale du validateur de Pull Request."

git add   chess-bots/src/main/java/fr/astroware/chess/bots/students   chess-bots/src/test/java/fr/astroware/chess/bots/students

git commit --quiet -m "test: synthetic valid student submission"

bash scripts/validate-student-submission.sh "$BASE_SHA"

echo "Soumission conforme acceptée : OK"

git reset --hard --quiet "$BASE_SHA"
git clean -fdq

echo "== Scénario 2 : soumission qui modifie le framework/repo =="

bash scripts/new-student-bot.sh   ValidatorSelfTestBot   "CI Validator"   "Validator Self Test"   "Fixture locale du validateur de Pull Request."

printf '\nSynthetic forbidden change.\n' >> README.md

git add   chess-bots/src/main/java/fr/astroware/chess/bots/students   chess-bots/src/test/java/fr/astroware/chess/bots/students   README.md

git commit --quiet -m "test: synthetic invalid student submission"

if bash scripts/validate-student-submission.sh "$BASE_SHA"; then
  echo "ERREUR : une soumission hors périmètre a été acceptée."
  exit 1
fi

echo "Soumission hors périmètre refusée : OK"
