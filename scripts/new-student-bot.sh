#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 3 || $# -gt 4 ]]; then
  cat <<'USAGE'
Usage:
  bash scripts/new-student-bot.sh <ClassNameBot> "<Auteur>" "<Nom du bot>" ["Description"]

Exemple:
  bash scripts/new-student-bot.sh DeepRabbitBot "Alice Dupont" "Deep Rabbit" "Bot positionnel et prudent."
USAGE
  exit 2
fi

CLASS_NAME="$1"
AUTHOR="$2"
BOT_NAME="$3"
DESCRIPTION="${4:-Bot étudiant créé avec Chess Framework.}"

if [[ ! "$CLASS_NAME" =~ ^[A-Z][A-Za-z0-9]*Bot$ ]]; then
  echo "Erreur : le nom de classe doit commencer par une majuscule, ne contenir que des lettres/chiffres et se terminer par Bot."
  exit 2
fi

if [[ -z "${AUTHOR// }" || -z "${BOT_NAME// }" ]]; then
  echo "Erreur : l'auteur et le nom du bot ne doivent pas être vides."
  exit 2
fi

escape_java() {
  printf '%s' "$1"     | sed 's/\\/\\\\/g; s/"/\\"/g'
}

AUTHOR_ESCAPED="$(escape_java "$AUTHOR")"
BOT_NAME_ESCAPED="$(escape_java "$BOT_NAME")"
DESCRIPTION_ESCAPED="$(escape_java "$DESCRIPTION")"

MAIN_DIR="chess-bots/src/main/java/fr/astroware/chess/bots/students"
TEST_DIR="chess-bots/src/test/java/fr/astroware/chess/bots/students"

MAIN_FILE="$MAIN_DIR/$CLASS_NAME.java"
TEST_FILE="$TEST_DIR/${CLASS_NAME}Test.java"

if [[ -e "$MAIN_FILE" || -e "$TEST_FILE" ]]; then
  echo "Erreur : un fichier existe déjà pour $CLASS_NAME."
  exit 1
fi

mkdir -p "$MAIN_DIR" "$TEST_DIR"

cat > "$MAIN_FILE" <<EOF
package fr.astroware.chess.bots.students;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.situation.Situations;

import java.util.List;

/**
 * Bot étudiant.
 *
 * <p>Commencez par modifier l'ordre des règles et remplacez progressivement
 * le fallback aléatoire par votre propre stratégie.</p>
 */
public final class $CLASS_NAME extends ChessBot {

    public $CLASS_NAME() {
    }

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "$BOT_NAME_ESCAPED",
            "$AUTHOR_ESCAPED",
            "$DESCRIPTION_ESCAPED"
        );
    }

    @Override
    protected List<Rule<?>> rules() {
        return List.of(
            rule(
                "Mater en un",
                Situations.mateInOne(),
                Actions.playMateInOne()
            ),
            rule(
                "Sortir d'échec",
                Situations.inCheck(),
                Actions.bestCheckEscape()
            ),
            rule(
                "Secours",
                Situations.always(),
                Actions.randomLegalMove()
            )
        );
    }
}
EOF

cat > "$TEST_FILE" <<EOF
package fr.astroware.chess.bots.students;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ${CLASS_NAME}Test {

    @Test
    void exposesTournamentIdentity() {
        var metadata =
            new $CLASS_NAME().metadata();

        assertEquals(
            "$BOT_NAME_ESCAPED",
            metadata.botName()
        );
        assertEquals(
            "$AUTHOR_ESCAPED",
            metadata.authorName()
        );
    }
}
EOF

cat <<EOF

Bot créé :
  $MAIN_FILE

Test créé :
  $TEST_FILE

Étapes suivantes :
  1. mvn verify
  2. bash scripts/chess.sh validate-students
  3. bash scripts/chess.sh list

La clé CLI sera générée automatiquement à partir du nom de classe.
EOF
