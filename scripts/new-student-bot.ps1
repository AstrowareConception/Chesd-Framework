param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$ClassName,

    [Parameter(Mandatory = $true, Position = 1)]
    [string]$Author,

    [Parameter(Mandatory = $true, Position = 2)]
    [string]$BotName,

    [Parameter(Mandatory = $false, Position = 3)]
    [string]$Description = "Bot étudiant créé avec Chess Framework."
)

$ErrorActionPreference = "Stop"

if ($ClassName -notmatch '^[A-Z][A-Za-z0-9]*Bot$') {
    throw "Le nom de classe doit commencer par une majuscule, ne contenir que des lettres/chiffres et se terminer par Bot."
}

if ([string]::IsNullOrWhiteSpace($Author) -or [string]::IsNullOrWhiteSpace($BotName)) {
    throw "L'auteur et le nom du bot ne doivent pas être vides."
}

function Escape-JavaString([string]$Value) {
    $slash = [string][char]92
    $quote = [string][char]34

    $escaped = $Value.Replace(
        $slash,
        $slash + $slash
    )

    return $escaped.Replace(
        $quote,
        $slash + $quote
    )
}

$authorEscaped = Escape-JavaString $Author
$botNameEscaped = Escape-JavaString $BotName
$descriptionEscaped = Escape-JavaString $Description

$mainDir = "chess-bots/src/main/java/fr/astroware/chess/bots/students"
$testDir = "chess-bots/src/test/java/fr/astroware/chess/bots/students"

$mainFile = Join-Path $mainDir "$ClassName.java"
$testFile = Join-Path $testDir "$($ClassName)Test.java"

if ((Test-Path $mainFile) -or (Test-Path $testFile)) {
    throw "Un fichier existe déjà pour $ClassName."
}

New-Item -ItemType Directory -Force -Path $mainDir | Out-Null
New-Item -ItemType Directory -Force -Path $testDir | Out-Null

$mainContent = @"
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
public final class $ClassName extends ChessBot {

    public $ClassName() {
    }

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "$botNameEscaped",
            "$authorEscaped",
            "$descriptionEscaped"
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
"@

$testContent = @"
package fr.astroware.chess.bots.students;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class $($ClassName)Test {

    @Test
    void exposesTournamentIdentity() {
        var metadata =
            new $ClassName().metadata();

        assertEquals(
            "$botNameEscaped",
            metadata.botName()
        );
        assertEquals(
            "$authorEscaped",
            metadata.authorName()
        );
    }
}
"@

Set-Content -Path $mainFile -Value $mainContent -Encoding utf8
Set-Content -Path $testFile -Value $testContent -Encoding utf8

Write-Host ""
Write-Host "Bot créé : $mainFile"
Write-Host "Test créé : $testFile"
Write-Host ""
Write-Host "Étapes suivantes :"
Write-Host "  1. mvn verify"
Write-Host "  2. powershell -ExecutionPolicy Bypass -File scripts/chess.ps1 validate-students"
Write-Host "  3. powershell -ExecutionPolicy Bypass -File scripts/chess.ps1 list"
Write-Host ""
Write-Host "La clé CLI sera générée automatiquement à partir du nom de classe."
