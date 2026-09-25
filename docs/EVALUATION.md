# Évaluer une position et comparer plusieurs coups

Ce document explique une idée centrale du Chess Framework : **reconnaître une situation ne suffit pas toujours**.

Dans de nombreuses positions, plusieurs coups correspondent à la même idée tactique ou stratégique. Le bot doit alors pouvoir les comparer.

---

## 1. Détection, évaluation, sélection

Le cycle de décision devient :

```text
POSITION
   ↓
SITUATION
   ↓
une ou plusieurs DÉTECTIONS
   ↓
ACTION
   ↓
plusieurs COUPS CANDIDATS
   ↓
ÉVALUATION de chaque coup
   ↓
sélection du meilleur coup légal
```

Exemple :

```text
Situation : "je peux capturer une pièce non défendue"

Détections :
- cavalier noir en c6
- pion noir en e5
- tour noire en a8

Coups candidats :
- Bxc6 → 6,2 / 10
- Nxe5 → 5,7 / 10
- Qxa8 → 9,1 / 10

Coup retenu :
- Qxa8
```

Le framework ne se contente donc pas de répondre :

```text
oui, une prise existe
```

Il peut conserver plusieurs opportunités puis les comparer.

---

## 2. Pourquoi une note de 0 à 10 ?

Le framework utilise une échelle normalisée :

| Note | Interprétation générale |
|---:|---|
| 0 | catastrophique |
| 1–2 | très mauvais |
| 3–4 | défavorable |
| 5 | neutre / moyen |
| 6–7 | intéressant |
| 8–9 | très bon |
| 10 | excellent |

Cette note reste une **heuristique**.

Elle ne signifie pas :

> « ce coup vaut objectivement 8,3 aux échecs ».

Elle signifie plutôt :

> « selon les critères choisis par ce bot, ce coup obtient 8,3/10 ».

Deux bots peuvent donc donner une note différente au même coup.

C'est précisément ce qui permet de créer des personnalités stratégiques différentes.

---

## 3. EvaluationScore

La note est encapsulée dans un objet valeur :

```java
EvaluationScore.of(8.5);
```

Le framework refuse automatiquement :

```java
EvaluationScore.of(-2);
EvaluationScore.of(14);
```

Le type garantit donc que toute note reste dans l'échelle prévue.

---

## 4. EvaluatedMove

Un coup candidat est représenté avec sa note :

```java
EvaluatedMove.of(
    Move.of("d2", "d4"),
    8.5,
    "Bon contrôle du centre"
);
```

Un `EvaluatedMove` contient :

- le coup ;
- sa note globale ;
- éventuellement le détail des critères ;
- une explication.

---

## 5. Plusieurs candidats pour une même règle

Une `Action` retourne maintenant :

```java
List<EvaluatedMove>
```

et non un seul coup.

Exemple conceptuel :

```java
return List.of(
    EvaluatedMove.of(moveA, 6.0, "Prise correcte"),
    EvaluatedMove.of(moveB, 8.0, "Prise plus rentable"),
    EvaluatedMove.of(moveC, 4.0, "Expose la dame")
);
```

La règle :

1. conserve tous les candidats ;
2. élimine les coups illégaux ;
3. compare les notes ;
4. choisit le meilleur candidat légal.

---

## 6. Pourquoi conserver les mauvais candidats ?

On pourrait simplement retourner le meilleur coup.

Le framework conserve volontairement le listing complet parce qu'il est pédagogiquement très utile.

Une trace pourra afficher :

```text
Règle : prise d'une pièce pendue

3 candidats :

1. Qxa8
   score : 9,1
   matériel : 10
   sécurité : 8
   développement : 6

2. Bxc6
   score : 6,2
   matériel : 6
   sécurité : 7
   développement : 6

3. Nxe5
   score : 5,7
   matériel : 5
   sécurité : 5
   développement : 8

Choix final : Qxa8
```

Cela permettra à l'étudiant de comprendre pourquoi son bot prend de mauvaises décisions.

---

## 7. Décomposer une note en critères

Une note globale peut être accompagnée de plusieurs `EvaluationCriterion`.

Exemple :

```java
new EvaluationCriterion(
    "Matériel",
    EvaluationScore.of(9.0),
    1.0,
    "Gain d'une tour contre un fou"
);
```

Un coup pourra être évalué selon :

- gain matériel ;
- sécurité du roi ;
- sécurité de la pièce déplacée ;
- mobilité ;
- contrôle du centre ;
- développement ;
- structure de pions ;
- activité ;
- création d'une menace ;
- risque tactique.

---

## 8. Exemple de calcul pondéré

Un étudiant pourra imaginer :

```text
matériel        = 9 / 10, poids 1.0
sécurité        = 7 / 10, poids 1.5
centre          = 5 / 10, poids 0.5
développement   = 6 / 10, poids 0.5
```

La note finale peut être une moyenne pondérée :

```text
(9×1 + 7×1,5 + 5×0,5 + 6×0,5)
--------------------------------
        1 + 1,5 + 0,5 + 0,5
```

Ce mécanisme ouvrira une partie très intéressante du travail étudiant :

> deux bots peuvent reconnaître exactement les mêmes situations mais jouer différemment parce qu'ils ne donnent pas les mêmes poids aux critères.

---

## 9. Évaluation globale d'une position

Le même principe s'applique à une position entière.

Convention :

- **0/10** : position pratiquement perdue ;
- **5/10** : position équilibrée ;
- **10/10** : position très favorable.

Le contrat est :

```java
public interface PositionEvaluator {

    PositionEvaluation evaluate(BotContext context);
}
```

Une évaluation pourra contenir par exemple :

```text
Position : 6,8 / 10

Matériel          7,0
Mobilité          6,0
Sécurité du roi   8,0
Centre            7,5
Structure pions   5,5
```

---

## 10. Évaluer un coup en évaluant la position après le coup

Une technique particulièrement importante sera :

```text
pour chaque coup candidat :
    simuler le coup
    évaluer la nouvelle position
    comparer les notes
```

Par exemple :

```text
Position actuelle : 5,4

Après Nf3 : 6,1
Après d4  : 6,8
Après a3  : 5,2

→ d4 est préféré
```

Le futur service de projection du framework permettra de faire cela sans modifier la vraie partie.

---

## 11. Attention : une bonne note locale n'est pas toujours un bon coup

Une évaluation simple peut se tromper.

Exemple :

```text
Qxa8 gagne une tour
→ score matériel élevé

mais...

la dame se retrouve enfermée
et sera capturée deux coups plus tard
```

C'est précisément la différence entre :

- une heuristique immédiate ;
- une recherche sur plusieurs demi-coups.

La V1 du framework privilégie les heuristiques lisibles.

Les étudiants avancés pourront ensuite enrichir leurs évaluations ou introduire une profondeur de recherche.

---

## 12. Ce que cela apporte au framework

Avec cette architecture, le framework peut exprimer :

```text
Situation
→ plusieurs occurrences
→ plusieurs coups possibles
→ plusieurs évaluations
→ meilleur coup
```

Cela rend possibles des comportements beaucoup plus fins qu'une suite de simples tests booléens.

Et surtout, le code reste explicable : le bot peut non seulement dire **ce qu'il a joué**, mais également **quels autres coups il avait envisagés et pourquoi il les a écartés**.
