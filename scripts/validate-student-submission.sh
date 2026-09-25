#!/usr/bin/env bash
set -euo pipefail

BASE_SHA="${1:-}"

if [[ -z "$BASE_SHA" ]]; then
  echo "::error::Base SHA manquant pour la validation de la soumission."
  exit 1
fi

mapfile -t CHANGED_FILES < <(
  git diff --name-only --diff-filter=ACMRT "$BASE_SHA...HEAD"
)

STUDENT_MAIN_PREFIX="chess-bots/src/main/java/fr/astroware/chess/bots/students/"
STUDENT_TEST_PREFIX="chess-bots/src/test/java/fr/astroware/chess/bots/students/"

STUDENT_MAIN_FILES=()
STUDENT_TEST_FILES=()

for file in "${CHANGED_FILES[@]}"; do
  if [[ "$file" == "$STUDENT_MAIN_PREFIX"*.java ]]; then
    STUDENT_MAIN_FILES+=("$file")
  elif [[ "$file" == "$STUDENT_TEST_PREFIX"*.java ]]; then
    STUDENT_TEST_FILES+=("$file")
  fi
done

if [[ ${#STUDENT_MAIN_FILES[@]} -eq 0 ]]; then
  echo "Aucune soumission étudiante détectée : validation structurelle ignorée."
  exit 0
fi

echo "Soumission étudiante détectée."
printf '  - %s\n' "${STUDENT_MAIN_FILES[@]}"

DISALLOWED=()

for file in "${CHANGED_FILES[@]}"; do
  if [[ "$file" == "$STUDENT_MAIN_PREFIX"*.java ]]; then
    continue
  fi

  if [[ "$file" == "$STUDENT_TEST_PREFIX"*.java ]]; then
    continue
  fi

  DISALLOWED+=("$file")
done

if [[ ${#DISALLOWED[@]} -gt 0 ]]; then
  echo "::error::Une Pull Request de bot ne doit modifier que le package students et ses tests."
  printf '::error::Fichier non autorisé : %s\n' "${DISALLOWED[@]}"
  exit 1
fi

if [[ ${#STUDENT_TEST_FILES[@]} -eq 0 ]]; then
  echo "::error::Ajoutez au moins un test dans chess-bots/src/test/java/fr/astroware/chess/bots/students/."
  exit 1
fi

BOT_CLASS_COUNT=0

for file in "${STUDENT_MAIN_FILES[@]}"; do
  if ! grep -Eq '^package[[:space:]]+fr\.astroware\.chess\.bots\.students;' "$file"; then
    echo "::error file=$file::Le package doit être exactement fr.astroware.chess.bots.students."
    exit 1
  fi

  if grep -Eq 'extends[[:space:]]+([[:alnum:]_$.]+\.)?ChessBot' "$file"; then
    BOT_CLASS_COUNT=$((BOT_CLASS_COUNT + 1))

    if [[ "$file" != *Bot.java ]]; then
      echo "::error file=$file::La classe de tournoi doit être dans un fichier dont le nom se termine par Bot.java."
      exit 1
    fi
  fi
done

if [[ "$BOT_CLASS_COUNT" -ne 1 ]]; then
  echo "::error::Une Pull Request de bot doit ajouter ou modifier exactement une classe étendant ChessBot. Trouvé : $BOT_CLASS_COUNT."
  exit 1
fi

echo "Structure Git de la soumission : OK"
