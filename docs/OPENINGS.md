# Utiliser et créer des ouvertures

Le framework distingue volontairement une **ouverture** d'un **plan stratégique**.

Une ouverture est une connaissance théorique fondée sur l'historique précis de la partie.

Un plan est un objectif adaptable.

---

## 1. Principe d'un OpeningBook

Un `OpeningBook` contient plusieurs `OpeningLine`.

Chaque ligne est une suite de demi-coups :

```text
d2d4 d7d5 g1f3 g8f6 c1f4 ...
```

La notation utilisée est la notation UCI simplifiée :

```text
e2e4
g1f3
e7e8q
```

Le framework fournit :

```java
Move.fromUci("e2e4");
```

---

## 2. Comment une ouverture est suivie

À chaque tour :

1. le framework consulte l'historique complet ;
2. il cherche les lignes dont cet historique est encore un préfixe ;
3. il récupère le prochain coup de chaque ligne ;
4. il élimine les coups devenus illégaux ;
5. les variantes restantes deviennent des coups candidats.

Exemple :

```text
Livre :
1. d4 d5 2. Nf3 Nf6 3. Bf4
2. d4 Nf6 2. Nf3 e6 3. Bf4

Partie :
1. d4 d5

Ligne 1 : correspond
Ligne 2 : ne correspond plus

Prochain coup :
Nf3
```

---

## 3. Que se passe-t-il si l'adversaire sort du livre ?

Supposons :

```text
Livre :
1. d4 d5 ...

Partie :
1. d4 a6
```

Aucune ligne ne correspond.

L'ouverture retourne donc :

```text
aucun candidat
```

La règle d'ouverture ne bloque pas le bot.

`ChessBot` continue simplement avec la règle suivante.

C'est exactement le comportement recherché :

```text
ouverture connue
    ↓
tant qu'elle fonctionne
    ↓
sinon stratégie normale / milieu de jeu
```

---

## 4. Ouvertures fournies

La première version contient un **petit répertoire pédagogique**, et non une encyclopédie complète.

### Système de Londres

```java
Openings.londonSystem()
```

Répertoire destiné aux Blancs avec plusieurs variantes simples.

### Défense Scandinave

```java
Openings.scandinavianDefense()
```

Répertoire destiné aux Noirs.

Le livre contient notamment une variante avec reprise par la dame et une branche avec `...Nf6`.

---

## 5. Plusieurs variantes peuvent être possibles

Après :

```text
1. e4 d5
2. exd5
```

le livre scandinave peut proposer plusieurs suites :

```text
Qxd5
Nf6
```

Chaque `OpeningLine` possède également des caractéristiques :

- agressivité ;
- sécurité ;
- risque.

Le `StrategyProfile` du bot peut donc influencer le choix de la variante.

Un bot aventureux peut préférer une branche plus dynamique.

Un bot solide peut favoriser une ligne plus prudente.

---

## 6. Insérer une ouverture dans un bot

Une ouverture devient une règle :

```java
Openings.londonSystem().asRule()
```

Exemple :

```java
@Override
protected List<Rule<?>> rules() {
    return List.of(
        Openings.londonSystem().asRule(),
        Plans.takeCenter().asRule(),
        Plans.developMinorPieces().asRule(),
        Plans.castleKingside().asRule()
    );
}
```

Dès que le livre ne sait plus jouer, « prendre le centre » prend naturellement le relais.

---

## 7. Priorité aux urgences tactiques

Dans un bot plus avancé, il sera préférable de placer certaines règles critiques **avant** l'ouverture.

Exemple conceptuel :

```java
return List.of(
    rule("Mat en un", ...),
    rule("Répondre à une menace critique", ...),

    Openings.londonSystem().asRule(),

    rule("Fourchette gagnante", ...),

    Plans.takeCenter().asRule(),
    Plans.castleKingside().asRule()
);
```

Une ouverture n'est pas sacrée.

Si une opportunité tactique prioritaire apparaît, le bot doit pouvoir quitter volontairement son livre.

Comme l'historique ne correspondra ensuite plus, le livre s'arrêtera automatiquement.

---

## 8. Créer une ligne personnalisée

Exemple :

```java
OpeningLine myLine = OpeningLine.styled(
    "Ma variante",
    8.0, // agressivité
    4.0, // sécurité
    7.0, // risque
    "e2e4",
    "e7e5",
    "g1f3",
    "b8c6",
    "f1c4"
);
```

Puis :

```java
OpeningBook myBook = new OpeningBook(
    "Mon répertoire e4",
    Color.WHITE,
    List.of(myLine)
);
```

---

## 9. Ajouter des variantes

Un livre devient plus robuste lorsque plusieurs réponses adverses sont connues.

Exemple conceptuel :

```text
1. d4 d5 2. Nf3 ...
1. d4 Nf6 2. Nf3 ...
1. d4 e6 2. Nf3 ...
```

Le framework ne force donc pas une seule ligne.

Il permet de construire progressivement un véritable arbre de théorie à partir de plusieurs séquences.

Dans une évolution future, cette représentation pourra être optimisée sous forme d'arbre ou de graphe sans modifier l'API étudiante.

---

## 10. Pourquoi ne pas coder toutes les ouvertures immédiatement ?

Parce que l'objectif du framework n'est pas de remplacer une base de données échiquéenne.

Quelques répertoires prêts à l'emploi suffisent pour :

- montrer le concept ;
- permettre aux étudiants de commencer rapidement ;
- leur donner envie d'ajouter leurs propres variantes ;
- comparer la stratégie « par connaissance » à la stratégie « par évaluation ».

Le tournoi sera plus intéressant si certains étudiants enrichissent leur répertoire et d'autres investissent davantage dans les heuristiques de milieu de jeu.
