# Chess Framework

Framework pédagogique Java destiné à la création de bots d'échecs et à l'organisation de tournois entre étudiants.

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

## Stack

- **Java 25 LTS**
- **Maven**
- **JUnit**
- **JaCoCo**
- **GitHub Actions**
- moteur de règles d'échecs masqué derrière une interface interne
- CLI de tournoi dans un premier temps
- export PGN et rapports de tournoi à terme

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
- [Spécifications fonctionnelles et pédagogiques](docs/SPECIFICATIONS.md)
- [Architecture cible](docs/ARCHITECTURE.md)
- [Évaluation des positions et des coups](docs/EVALUATION.md)
- [Analyse de position](docs/ANALYSIS.md)
- [Bots de référence](docs/REFERENCE_BOTS.md)
- [Stratégies, postures et plans multi-coups](docs/STRATEGIES.md)
- [Ouvertures et répertoires](docs/OPENINGS.md)
- [Roadmap](docs/ROADMAP.md)
- [Contribution des bots](CONTRIBUTING.md)

## État actuel

Déjà disponibles :

- modèle objet de base : couleurs, pièces, cases, coups ;
- `Analysis`, `AttackMap`, matériel, attaquants, défenseurs et pièces pendues ;
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
- `SolidPlannerBot` comme exemple pédagogique ;
- tests unitaires et CI GitHub Actions.

La couche d'analyse de base existe désormais. La prochaine grande brique est la **projection après un coup**, puis l'enrichissement tactique : sécurité après déplacement, fourchettes, clouages et enfilades.

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
