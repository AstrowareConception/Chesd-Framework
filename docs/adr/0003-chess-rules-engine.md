# ADR 0003 — Moteur de règles d'échecs

- **Statut :** accepté
- **Date :** 2026-09-25

## Contexte

Le framework doit fournir aux bots :

- les coups légaux ;
- le roque ;
- la prise en passant ;
- les promotions ;
- l'échec et le mat ;
- le pat ;
- les répétitions ;
- la règle des cinquante coups ;
- le matériel insuffisant ;
- FEN ;
- la simulation fiable d'un coup.

Réimplémenter l'ensemble de ces règles dans le projet pédagogique créerait beaucoup de code complexe qui n'est pas le sujet principal du cours.

Le besoin réel est donc double :

1. utiliser un moteur de règles fiable ;
2. empêcher cette dépendance de contaminer l'API utilisée par les étudiants.

## Décision

Le backend initial du framework utilise :

```text
io.github.wolfraam:chessgame:2.3
```

La bibliothèque est encapsulée derrière :

```java
ChessRulesEngine
```

et son implémentation :

```text
WolfraamChessRulesEngine
```

Aucun type de la bibliothèque externe ne doit apparaître dans l'API publique destinée aux bots.

## Pourquoi cette bibliothèque ?

Le spike a confirmé qu'elle fournit directement :

- construction d'une partie depuis la position initiale ;
- construction depuis FEN ;
- liste des coups légaux ;
- validation d'un coup ;
- clonage d'une partie ;
- exécution d'un coup ;
- état d'échec ;
- résultat de partie ;
- historique ;
- accès aux pièces et aux cases.

Ces fonctionnalités correspondent exactement aux besoins du framework.

## Adapter

Le reste de l'application dépend de :

```java
public interface ChessRulesEngine {

    PositionView initialPosition();

    PositionView fromFen(String fen);

    List<Move> legalMoves(PositionView position);

    PositionView play(PositionView position, Move move);

    GameResult result(PositionView position);

    boolean isKingAttacked(PositionView position);
}
```

Cela signifie que nous pouvons remplacer la bibliothèque plus tard sans réécrire les bots étudiants.

## Immutabilité apparente

La bibliothèque possède un objet de partie mutable.

Notre Adapter clone cet objet avant chaque `play`.

Ainsi :

```java
PositionView after = engine.play(before, move);
```

ne modifie jamais `before`.

C'est un contrat beaucoup plus simple à raisonner pour les étudiants.

## Tests de contrat

Le moteur est notamment testé sur :

- 20 coups légaux dans la position initiale ;
- `e2e4` sans mutation de la position source ;
- rejet de `e2e5` depuis la position initiale ;
- mat du sot ;
- chargement d'une position FEN ;
- détection d'un roi en échec.

Des tests perft plus complets seront ajoutés avant de figer la version tournoi.

## Conséquence pour Analysis

Grâce au moteur, `Analysis` peut désormais fournir :

```java
analysis.after(move)
```

Cette opération joue réellement le coup sur une copie de la position puis reconstruit :

- la position ;
- l'AttackMap ;
- le matériel ;
- les attaques ;
- les défenses ;
- les coups légaux suivants ;
- le résultat éventuel.

Cette projection devient la fondation des tactiques avancées.
