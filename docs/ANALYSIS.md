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

## Limite actuelle : pas encore de projection complète

Le framework ne sait pas encore calculer parfaitement :

```java
analysis.after(move)
```

Cette projection sera bâtie au-dessus du moteur de règles choisi afin de gérer correctement captures, roque, promotion, prise en passant et droits de roque.

Nous évitons volontairement de coder une seconde implémentation partielle des règles des échecs uniquement pour la simulation.

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