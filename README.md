# Chess Framework

Framework pédagogique Java destiné à la création de bots d'échecs et à l'organisation de tournois entre étudiants.

## Vision

Le projet fournit un moteur de jeu fiable et une API orientée objet permettant à un étudiant de créer un bot en étendant une classe `ChessBot`. Un bot est principalement décrit comme une suite ordonnée de règles :

```text
Situation reconnue -> Action à tenter
```

Le moteur parcourt les règles dans l'ordre. Lorsqu'une situation est détectée, l'action associée propose un coup. Si aucun coup légal ne peut être produit, le moteur continue avec la règle suivante.

L'objectif pédagogique est de faire travailler la programmation orientée objet, l'héritage, la composition, le polymorphisme, les interfaces, l'encapsulation, les génériques, les tests et plusieurs design patterns dans un projet ludique dont l'aboutissement est un tournoi de bots.

## Principes

- Java comme langage principal.
- API simple pour les étudiants, architecture interne rigoureuse.
- Le framework gère les règles des échecs et la légalité des coups.
- Les étudiants travaillent principalement sur le comportement de leur bot.
- Les situations tactiques et stratégiques sont composables et réutilisables.
- Les règles d'un bot sont ordonnées : la première action applicable produisant un coup légal gagne.
- Chaque décision peut être tracée pour expliquer pourquoi un bot a joué un coup.
- Les matchs sont reproductibles grâce à des graines aléatoires contrôlées.
- Le moteur de tournoi impose les mêmes contraintes à tous les bots.
- Les bots étudiants ont vocation à être intégrés par Pull Request.

## Stack cible

- **Java 25 LTS**
- **Maven**
- **JUnit**
- moteur de règles d'échecs masqué derrière une interface interne afin de ne pas coupler l'API étudiante à une bibliothèque tierce
- CLI de tournoi dans un premier temps
- export PGN et rapports de tournoi

## Exemple d'utilisation visé

```java
public final class MyBot extends ChessBot {

    @Override
    public String name() {
        return "MyBot";
    }

    @Override
    protected List<Rule<?>> rules() {
        return List.of(
            rule("Mat en un", Situations.mateInOne(), Actions.playDetectedMove()),
            rule("Sortir d'échec", Situations.inCheck(), Actions.bestEscape()),
            rule("Prendre une pièce pendue", Situations.hangingEnemyPiece(), Actions.captureHighestValue()),
            rule("Créer une fourchette", Situations.forkOpportunity(), Actions.playDetectedMove()),
            rule("Développer", Situations.canDevelopPiece(), Actions.developBestPiece()),
            rule("Secours", Situations.always(), Actions.randomLegalMove())
        );
    }
}
```

L'API exacte sera raffinée pendant l'implémentation ; cet exemple illustre l'expérience développeur recherchée.

## Documentation

- [Spécifications fonctionnelles et pédagogiques](docs/SPECIFICATIONS.md)
- [Architecture cible](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)
- [Contribution des bots](CONTRIBUTING.md)

## Bots de référence prévus

1. **RandomBot** — joue un coup légal aléatoire.
2. **GreedyBot** — privilégie les prises selon la valeur matérielle.
3. **TacticalBot** — applique plusieurs règles tactiques simples et ordonnées.

Ces bots servent à tester une soumission et fournissent trois niveaux de comportement faciles à comprendre.

## Tournoi final

Le format initial visé est un tournoi toutes rondes avec alternance des couleurs. Le moteur enregistre les résultats, les coups, les temps de décision, la raison de chaque choix lorsque le mode trace est actif, et produit des parties au format PGN.

Le détail du protocole de tournoi sera figé avant le lancement de l'exercice étudiant.
