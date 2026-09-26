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
  echo "::error::Les pom.xml, le SDK, le moteur et le tournoi sont gelés pour une soumission de bot."
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

  # Contrôle statique volontairement simple : l'isolation JVM reste la
  # protection d'exécution, mais une soumission pédagogique n'a aucune raison
  # d'utiliser directement le réseau, le disque, les processus ou la réflexion.
  if grep -Eq '(^import[[:space:]]+java\.net\.)|java\.net\.' "$file"; then
    echo "::error file=$file::Les API réseau java.net sont interdites dans un bot étudiant."
    exit 1
  fi

  if grep -Eq '(^import[[:space:]]+java\.nio\.file\.)|java\.nio\.file\.' "$file"; then
    echo "::error file=$file::Les API d'accès fichiers java.nio.file sont interdites dans un bot étudiant."
    exit 1
  fi

  if grep -Eq '(^import[[:space:]]+java\.io\.)|java\.io\.' "$file"; then
    echo "::error file=$file::Les API java.io sont interdites dans un bot étudiant."
    exit 1
  fi

  if grep -Eq '(^import[[:space:]]+java\.lang\.reflect\.)|java\.lang\.reflect\.|Class\.forName[[:space:]]*\(|\.getDeclared(Field|Method|Constructor)s?[[:space:]]*\(|\.setAccessible[[:space:]]*\(|MethodHandles|Unsafe' "$file"; then
    echo "::error file=$file::La réflexion et les API de contournement sont interdites dans un bot étudiant."
    exit 1
  fi

  if grep -Eq '\b(ProcessBuilder|ClassLoader)\b|Runtime\.getRuntime[[:space:]]*\(|System\.exit[[:space:]]*\(' "$file"; then
    echo "::error file=$file::Les processus, chargeurs de classes, Runtime et System.exit sont interdits dans un bot étudiant."
    exit 1
  fi
done

if [[ "$BOT_CLASS_COUNT" -ne 1 ]]; then
  echo "::error::Une Pull Request de bot doit ajouter ou modifier exactement une classe étendant ChessBot. Trouvé : $BOT_CLASS_COUNT."
  exit 1
fi

echo "Périmètre Git et API interdites : OK"
echo "La compilation, les métadonnées et les smoke-tests isolés seront vérifiés par mvn verify."
