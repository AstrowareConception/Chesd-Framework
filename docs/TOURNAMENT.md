# Organiser un tournoi de bots

Le module `chess-tournament` peut maintenant organiser un tournoi **toutes rondes** entre plusieurs bots.

Chaque paire joue le nombre demandé de parties et les couleurs alternent automatiquement.

---

## 1. Lancer un tournoi

Depuis le module `chess-tournament` :

```bash
mvn exec:java -Dexec.args="tournament random greedy tactical"
```

Par défaut :

- 2 parties par paire ;
- chaque bot joue une fois avec les Blancs et une fois avec les Noirs ;
- seed de base : 42 ;
- limite technique : 400 demi-coups.

---

## 2. Choisir le nombre de parties

```bash
mvn exec:java -Dexec.args="tournament positional lookahead minimax --games=4"
```

Avec 3 bots et 4 parties par paire :

```text
3 paires × 4 parties = 12 parties
```

Les couleurs alternent :

```text
Partie 1 : A blancs / B noirs
Partie 2 : B blancs / A noirs
Partie 3 : A blancs / B noirs
Partie 4 : B blancs / A noirs
```

---

## 3. Lancer tous les bots

```bash
mvn exec:java -Dexec.args="tournament --all"
```

Attention : les bots utilisant une recherche plus profonde peuvent rendre ce tournoi sensiblement plus long.

En particulier :

- `positional` : profondeur 1 ;
- `lookahead` : profondeur 2 ;
- `minimax` : profondeur 3 bornée.

Pour une démonstration rapide, il est souvent préférable de sélectionner quelques bots.

---

## 4. Seed reproductible

```bash
mvn exec:java -Dexec.args="tournament random greedy tactical --seed=12345"
```

Chaque partie dérive sa propre seed de la seed de base.

Le même tournoi lancé avec :

- les mêmes bots ;
- le même ordre ;
- la même configuration ;
- la même seed ;

reste reproductible pour les décisions pseudo-aléatoires.

---

## 5. Limite de partie

```bash
mvn exec:java -Dexec.args="tournament random random --max-plies=150"
```

Lorsqu'une partie atteint cette limite sans résultat échiquéen naturel, elle devient une **nulle technique** pour le classement.

Elle rapporte :

```text
0,5 point à chaque bot
```

mais reste comptabilisée séparément des vraies nulles.

---

## 6. Barème

Le barème est celui utilisé classiquement en tournoi :

```text
victoire : 1 point
nulle    : 0,5 point
défaite  : 0 point
```

Les nulles techniques distribuent également 0,5 point à chaque participant mais disposent de leur propre compteur.

---

## 7. Classement

Le rapport console affiche notamment :

```text
#    Bot                      Pts    V    N    D    NT  Parties    Moy. ms
1    Tactical Bot             4.5    4    1    1     0        6       12.4
2    Positional Bot           3.5    3    1    2     0        6       37.9
3    Random Bot               1.0    1    0    5     0        6        0.2
```

`NT` signifie **nulle technique**.

Les égalités sont actuellement départagées de manière déterministe par :

1. points ;
2. nombre de victoires ;
3. nombre de nulles techniques ;
4. temps moyen de décision ;
5. nom du bot.

Ces critères pourront être remplacés plus tard par de vrais départages échiquéens si nécessaire.

---

## 8. Temps de décision

Chaque `PlayedMove` mémorise maintenant le temps passé dans :

```java
ChessBot.decide(...)
```

On peut lire :

```java
move.decisionMillis();
```

et sur un match :

```java
result.averageDecisionMillis(Color.WHITE);
result.maxDecisionMillis(Color.WHITE);
```

Le tournoi calcule également le temps moyen de décision de chaque participant.

Cette métrique sera utile pour :

- comparer des approches algorithmiques ;
- montrer le coût d'une profondeur supplémentaire ;
- préparer les futures limites de temps du tournoi final.

---

## 9. Progression des bots de recherche

Le framework fournit maintenant trois niveaux pédagogiques.

### PositionalBot

```text
mon coup
  ↓
évaluation de la position
```

### LookaheadBot

```text
mon coup
  ↓
meilleure réponse adverse
  ↓
évaluation
```

### MinimaxBot

```text
arbre de recherche
  ↓
Minimax profondeur 3
  ↓
move ordering
  ↓
alpha-bêta
  ↓
évaluation positionnelle aux feuilles
```

La configuration utilisée par `MinimaxBot` est volontairement bornée :

```java
SearchSettings.bounded(3, 6);
```

soit profondeur 3 avec au maximum 6 coups explorés par nœud.

---

## 10. API Minimax

Un étudiant avancé peut écrire :

```java
rule(
    "Recherche",
    Situations.always(),
    Actions.minimax(
        SearchSettings.bounded(3, 6)
    )
)
```

ou une recherche exhaustive :

```java
SearchSettings.exact(3)
```

Cette dernière peut devenir coûteuse lorsque beaucoup de coups sont légaux.

---

## 11. Alpha-bêta

L'élagage alpha-bêta est activé par défaut.

Pour le désactiver à des fins pédagogiques :

```java
SearchSettings
    .bounded(3, 6)
    .withoutAlphaBeta();
```

Le résultat de recherche expose :

```java
search.score();
search.principalVariation();
search.nodesVisited();
search.cutoffs();
```

Cela permet de comparer directement :

- même position ;
- même profondeur ;
- avec ou sans alpha-bêta.

---

## 12. Tournoi final étudiant

Le workflow visé devient :

```text
étudiant
  ↓
branche Git
  ↓
classe ChessBot
  ↓
tests
  ↓
Pull Request
  ↓
CI
  ↓
merge
  ↓
catalogue du tournoi
  ↓
toutes rondes
  ↓
classement final
  ↓
PGN / viewer / traces / statistiques
```

Le moteur de tournoi est maintenant suffisamment structuré pour servir de base à ce processus.


---

## 13. Exporter le tournoi

Le mode tournoi peut écrire automatiquement toutes les parties dans un PGN multi-parties :

```bash
mvn exec:java -Dexec.args="tournament tactical positional minimax --pgn=parties.pgn"
```

et le classement dans un CSV UTF-8 :

```bash
mvn exec:java -Dexec.args="tournament tactical positional minimax --csv=classement.csv"
```

Les deux peuvent être combinés :

```bash
mvn exec:java -Dexec.args="tournament tactical positional minimax --games=2 --pgn=parties.pgn --csv=classement.csv"
```

Le PGN contient toutes les parties à la suite.

Le CSV contient notamment :

```text
rank
bot
author
played
wins
draws
technical_draws
losses
points
average_decision_ms
```

Cela permet d'archiver un tournoi complet ou d'analyser facilement les résultats dans un tableur.
