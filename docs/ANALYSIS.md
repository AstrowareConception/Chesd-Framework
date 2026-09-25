# Analyser une position

La couche `Analysis` fournit des **faits échiquéens réutilisables**. Elle ne décide pas de la stratégie du bot.

```text
Analysis = ce qui est vrai dans la position
Strategy / Rules / Plans = ce que le bot décide d'en faire
```

## Accès

Depuis un `BotContext` :

```java
Analysis analysis = context.analysis();
```

Puis notamment :

```java
analysis.material(Color.WHITE);
analysis.materialBalance();
analysis.captures();
analysis.hangingPieces(Color.BLACK);
```

## Valeur des pièces

| Pièce | Valeur |
|---|---:|
| Pion | 1 |
| Cavalier | 3 |
| Fou | 3 |
| Tour | 5 |
| Dame | 9 |
| Roi | 0 dans l'évaluation matérielle |

Le roi vaut ici 0 uniquement parce qu'il ne constitue pas un bien matériel échangeable : sa perte termine la partie.

## AttackMap

`AttackMap` permet de demander quelles cases sont attaquées, par qui, et combien de fois :

```java
attackMap.attackedBy(Color.WHITE);
attackMap.attackersOf(Square.from("e4"), Color.BLACK);
attackMap.attackCount(Square.from("e4"), Color.BLACK);
attackMap.attacksFrom(piece);
```

Pour les pièces coulissantes, le premier obstacle appartient au rayon d'attaque puis bloque les cases suivantes.

## Attaque, défense et pièce pendue

Une pièce est **attaquée** lorsqu'une pièce adverse attaque sa case.

Une pièce est **défendue** lorsqu'au moins une autre pièce de son camp attaque sa case.

Dans la V1, une pièce est dite **pendue** lorsqu'elle est attaquée et non défendue.

```java
analysis.isAttacked(piece);
analysis.isDefended(piece);
analysis.isHanging(piece);
```

Cette définition sera raffinée plus tard : une pièce défendue une fois mais attaquée trois fois peut rester tactiquement vulnérable.

## CaptureDetection

`Situations.captureAvailable()` produit des `CaptureDetection` contenant :

- le coup ;
- l'attaquant ;
- la cible ;
- la valeur de l'attaquant ;
- la valeur de la cible ;
- le nombre de défenseurs de la cible.

Deux actions peuvent donc interpréter la même capture différemment.

### Politique matérialiste

```java
Actions.captureHighestValue();
```

Question principale : **quelle est la pièce la plus chère que je peux prendre ?**

### Politique prudente

```java
Actions.captureWithRiskAwareness();
```

Elle tient également compte du nombre de défenseurs et du rapport de valeur entre attaquant et cible.

## Projection après un coup

La projection est maintenant disponible :

```java
PositionProjection projection =
    analysis.after(move);
```

Le coup est réellement exécuté sur une copie de la position par le moteur de règles.

On peut ensuite demander :

```java
projection.position();
projection.analysis();
projection.legalMoves();
projection.result();
```

Exemple :

```java
PlacedPiece movedPiece = ...;

boolean stillAttacked =
    projection.analysis().isAttacked(movedPiece);
```

La vraie partie n'est jamais modifiée.

Cette capacité est déjà utilisée pour :

- chercher une case sûre pour une pièce menacée ;
- réévaluer le risque d'une capture ;
- détecter un mat en un ;
- détecter des fourchettes.

Elle permettra ensuite d'implémenter les clouages, enfilades, attaques à la découverte et évaluations à faible profondeur.

## Chaîne complète

```text
Position
  ↓
Analysis
  ↓
Situation
  ↓
Detection
  ↓
Action
  ↓
EvaluatedMove
  ↓
StrategyProfile
  ↓
Move
```

---

## Phase de jeu

`Analysis` fournit désormais :

```java
analysis.gamePhase();
```

Valeurs possibles :

```java
OPENING
MIDDLEGAME
ENDGAME
```

L'estimation standard tient compte :

- du numéro de coup, à partir de l'historique ou du FEN ;
- du matériel non-pion restant ;
- de la présence ou non des dames.

Cette classification est une heuristique pédagogique, pas une vérité absolue.

Elle sert à permettre aux bots de changer :

- de profil ;
- de règles actives ;
- de plans ;
- de niveau de risque.

---

## Tactiques de ligne et surcharge

`Analysis` expose aussi :

```java
analysis.pinsBy(Color.WHITE);
analysis.skewersBy(Color.WHITE);
analysis.overloadedDefenders(Color.BLACK);
```

Un défenseur est actuellement considéré surchargé lorsqu'il est **l'unique défenseur d'au moins deux pièces déjà attaquées**.

Le framework peut ensuite simuler la capture de ce défenseur et vérifier quelles cibles deviennent réellement pendues.
