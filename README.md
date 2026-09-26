# Chess Framework

Framework pédagogique Java destiné à la création de bots d'échecs et à l'organisation de tournois entre étudiants.

## Quick start étudiant

Prérequis : **Java 25**, **Maven** et **Git**.

```bash
git clone https://github.com/AstrowareConception/Chess-Framework.git
cd Chess-Framework
```

Créer automatiquement son bot sous Linux/macOS/Git Bash :

```bash
bash scripts/new-student-bot.sh DeepRabbitBot "Alice Dupont" "Deep Rabbit"
```

Sous Windows PowerShell :

```powershell
powershell -ExecutionPolicy Bypass -File scripts/new-student-bot.ps1 DeepRabbitBot "Alice Dupont" "Deep Rabbit"
```

Compiler, tester et produire le runner autonome :

```bash
mvn verify
```

Puis valider et lancer le bot :

```bash
bash scripts/chess.sh validate-students
bash scripts/chess.sh list
bash scripts/chess.sh console student-deep-rabbit random --isolated
```

Sous Windows, remplacez `bash scripts/chess.sh` par `powershell -ExecutionPolicy Bypass -File scripts/chess.ps1`.

Le guide complet est dans [docs/STUDENT_TOURNAMENT_BOT.md](docs/STUDENT_TOURNAMENT_BOT.md).

## Vision

Le projet fournit un moteur de jeu fiable et une API orientée objet permettant à un étudiant de créer son propre joueur d'échecs en étendant `ChessBot`.

Le comportement d'un bot reste volontairement lisible :

```text
Situation reconnue
    ↓
plusieurs coups candidats
    ↓
évaluation
    ↓
choix selon la stratégie du bot
```

Un bot peut également suivre :

- une **personnalité stratégique** : défensive, équilibrée, offensive, aventureuse ;
- une **ouverture** connue tant que la partie reste dans son livre ;
- des **plans multi-coups** comme prendre le centre, développer ou préparer le roque ;
- des règles tactiques ordonnées ;
- un fallback de sécurité.

L'objectif pédagogique est de pratiquer la programmation orientée objet, l'héritage, la composition, le polymorphisme, les interfaces, l'encapsulation, les génériques, les tests, Git/GitHub et plusieurs design patterns dans un projet ludique dont l'aboutissement est un tournoi de bots.

## Principes

- Java 25 LTS.
- Maven multi-module.
- API simple pour les étudiants, architecture interne rigoureuse.
- Le framework gère les règles des échecs et la légalité des coups.
- Les positions sont exposées en lecture seule.
- Une situation peut produire plusieurs détections.
- Une action peut produire plusieurs coups candidats.
- Chaque candidat peut être noté de 0 à 10.
- Le profil du bot peut privilégier l'agressivité, la sécurité ou la prise de risque.
- Les plans stratégiques peuvent durer plusieurs coups sans imposer une séquence rigide.
- Les ouvertures sont suivies tant qu'une ligne reste compatible et légale.
- Les règles restent ordonnées : l'ordre exprime les priorités du bot.
- Chaque décision peut être tracée et expliquée.
- Les matchs sont reproductibles grâce à des graines aléatoires contrôlées.
- Les bots étudiants ont vocation à être intégrés par Pull Request.
- Le tournoi peut exécuter chaque bot dans une JVM enfant isolée avec timeout dur et plafond mémoire.

## Stack

- **Java 25 LTS**
- **Maven**
- **JUnit**
- **JaCoCo**
- **GitHub Actions**
- moteur de règles d'échecs masqué derrière une interface interne
- CLI de duel et de tournoi, plus viewer Swing
- exports PGN/CSV, rapports de tournoi et replay

## Exemple de bot stratégique

```java
public final class MyBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Deep Rabbit",
            "Alice Dupont",
            "Bot solide qui privilégie la sécurité avant l'activité."
        );
    }

    @Override
    protected StrategyProfile strategyProfile() {
        return StrategyProfiles.solid();
    }

    @Override
    protected List<Rule<?>> rules() {
        return List.of(
            // Les règles tactiques critiques viendront ici.

            Openings.londonSystem().asRule(),
            Openings.scandinavianDefense().asRule(),

            Plans.castleKingside().asRule(),
            Plans.developMinorPieces().asRule(),
            Plans.takeCenter().asRule(),

            rule(
                "Secours",
                Situations.always(),
                Actions.randomLegalMove()
            )
        );
    }
}
```

L'ordre est volontaire : ici le bot préfère mettre son roi à l'abri avant de chercher davantage d'espace central.

Un autre étudiant peut conserver les mêmes briques mais changer :

- leur ordre ;
- le profil stratégique ;
- les poids d'évaluation ;
- les ouvertures ;
- les plans ;
- les situations ;
- les actions.

Il obtient alors un bot au comportement différent sans réécrire le moteur.

## Documentation

- [Bien démarrer](docs/GETTING_STARTED.md)
- [Créer et soumettre son bot de tournoi](docs/STUDENT_TOURNAMENT_BOT.md)
- [Dépannage étudiant](docs/TROUBLESHOOTING.md)
- [Spécifications fonctionnelles et pédagogiques](docs/SPECIFICATIONS.md)
- [Architecture cible](docs/ARCHITECTURE.md)
- [Évaluation des positions et des coups](docs/EVALUATION.md)
- [Analyse de position](docs/ANALYSIS.md)
- [Bots de référence](docs/REFERENCE_BOTS.md)
- [Console, PGN et viewer graphique](docs/INTERFACES.md)
- [Stratégies, postures et plans multi-coups](docs/STRATEGIES.md)
- [Ouvertures et répertoires](docs/OPENINGS.md)
- [Roadmap](docs/ROADMAP.md)
- [Contribution des bots](CONTRIBUTING.md)

## État actuel

Déjà disponibles :

- modèle objet de base : couleurs, pièces, cases, coups ;
- `Analysis`, `AttackMap`, matériel, attaquants, défenseurs et pièces pendues ;
- `PositionEvaluation` notée de 0 à 10 : matériel, mobilité, centre, pions et sécurité du roi ;
- structures de pions : isolés, doublés, passés et passés protégés ;
- colonnes ouvertes et semi-ouvertes ;
- évitement des mats en un adverses ;
- recherche adversariale profondeur 2 : meilleur coup → meilleure réponse adverse → score robuste ;
- situations `captureAvailable()` et `hangingEnemyPiece()` ;
- actions de capture matérialiste et prudente ;
- notation UCI simple des coups ;
- `ChessBot`, `Situation`, `Detection`, `Action`, `Rule` ;
- candidats évalués de 0 à 10 ;
- profils stratégiques ;
- tolérance au risque ;
- livres d'ouvertures ;
- Système de Londres ;
- Défense Scandinave ;
- plans « prendre le centre », « développer les pièces mineures » et « préparer le petit roque » ;
- `RandomBot` ;
- `GreedyBot` ;
- `CautiousBot` ;
- `BerserkerBot` ;
- `GuardianBot` ;
- `TacticalBot` ;
- `PressureBot` ;
- `ChameleonBot` ;
- `PositionalBot` ;
- `LookaheadBot` ;
- `MinimaxBot` ;
- `SolidPlannerBot` comme exemple pédagogique ;
- `MatchRunner` pour faire jouer réellement deux bots avec une seed reproductible ;
- tests unitaires et CI GitHub Actions ;
- validation automatique des Pull Requests étudiantes ;
- découverte automatique des bots du package `students` dans le catalogue.

La couche d'analyse dispose désormais de la **projection après un coup**. Mat en un et fourchette sont déjà détectés par simulation réelle. Le catalogue couvre désormais mat en un, sortie d'échec, fourchette, clouage, enfilade, double échec, attaque à la découverte et surcharge d'un défenseur. Le framework sait aussi adapter une stratégie à l'ouverture, au milieu de jeu et à la finale.

## Bots de référence prévus pour le tournoi

1. **RandomBot** — joue un coup légal aléatoire.
2. **GreedyBot** — privilégie les gains matériels immédiats.
3. **TacticalBot** — exploite plusieurs motifs tactiques simples.

`SolidPlannerBot` est actuellement un exemple pédagogique supplémentaire destiné à illustrer les ouvertures, les plans et les profils.

## Tournoi final

Le format initial visé est un tournoi toutes rondes avec alternance des couleurs.

Le moteur devra enregistrer :

- résultats ;
- coups ;
- temps de décision ;
- traces des règles ;
- candidats envisagés ;
- raisons du choix ;
- parties au format PGN ;
- classement final.

Les étudiants soumettront leur bot par Pull Request afin de pratiquer également le workflow GitHub.


## Lancer un duel

Après `mvn verify`, le projet produit un runner autonome :

```text
chess-tournament/target/chess-tournament-0.1.0-SNAPSHOT-runner.jar
```

Lister les bots :

```bash
bash scripts/chess.sh list
```

Suivre une partie en console :

```bash
bash scripts/chess.sh console tactical cautious
```

Exporter un PGN :

```bash
bash scripts/chess.sh pgn tactical guardian partie.pgn
```

Ouvrir le viewer graphique :

```bash
bash scripts/chess.sh gui architect tactical
```

Voir `docs/INTERFACES.md` pour le détail.


### Lancer un tournoi

```bash
bash scripts/chess.sh tournament positional lookahead minimax --games=2
```

Avec exports :

```bash
bash scripts/chess.sh tournament tactical positional minimax --pgn=parties.pgn --csv=classement.csv
```

Voir `docs/TOURNAMENT.md`.


### Exécution isolée recommandée pour le tournoi

```bash
bash scripts/chess.sh tournament positional lookahead minimax --games=2 --isolated --timeout-ms=3000 --heap-mb=256
```

En mode isolé, chaque bot tourne dans une JVM enfant. Une décision qui dépasse le timeout provoque un forfait sans bloquer le tournoi.
