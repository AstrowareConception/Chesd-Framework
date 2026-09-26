# Organiser un tournoi de bots

Le module `chess-tournament` peut maintenant organiser un tournoi **toutes rondes** entre plusieurs bots.

Chaque paire joue le nombre demandé de parties et les couleurs alternent automatiquement.

---

## 1. Lancer un tournoi

```bash
bash scripts/chess.sh tournament random greedy tactical
```

Par défaut :

- 2 parties par paire ;
- chaque bot joue une fois avec les Blancs et une fois avec les Noirs ;
- seed de base : 42 ;
- limite technique : 400 demi-coups.

---

## 2. Choisir le nombre de parties

```bash
bash scripts/chess.sh tournament positional lookahead minimax --games=4
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
bash scripts/chess.sh tournament --all
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
bash scripts/chess.sh tournament random greedy tactical --seed=12345
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
bash scripts/chess.sh tournament random random --max-plies=150
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
#    Bot                      Pts    V    N    D    F    NT  Parties    Moy. ms
1    Tactical Bot             4.5    4    1    1    0     0        6       12.4
2    Positional Bot           3.5    3    1    2    0     0        6       37.9
3    Random Bot               1.0    1    0    5    0     0        6        0.2
```

`F` signifie **défaite par forfait** et `NT` signifie **nulle technique**.

Les égalités sont départagées de manière déterministe par :

1. points ;
2. nombre de victoires ;
3. nombre de nulles techniques ;
4. nom du bot.

Le temps moyen reste affiché comme **statistique de performance**, mais n'intervient pas dans le classement : deux exécutions identiques ne doivent pas changer d'ordre simplement parce qu'une machine est plus rapide qu'une autre.

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

Cette métrique permet de :

- comparer des approches algorithmiques ;
- montrer le coût d'une profondeur supplémentaire ;
- contrôler le respect du budget de calcul du tournoi.

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

Le workflow opérationnel est :

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

Ce workflow est testé automatiquement en CI avec un bot étudiant généré, validé, exécuté en duel isolé puis en mini-tournoi avec exports PGN/CSV.


---

## 13. Exporter le tournoi

Le mode tournoi peut écrire automatiquement toutes les parties dans un PGN multi-parties :

```bash
bash scripts/chess.sh tournament tactical positional minimax --pgn=parties.pgn
```

et le classement dans un CSV UTF-8 :

```bash
bash scripts/chess.sh tournament tactical positional minimax --csv=classement.csv
```

Les deux peuvent être combinés :

```bash
bash scripts/chess.sh tournament tactical positional minimax --games=2 --pgn=parties.pgn --csv=classement.csv
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
losses
forfeits
technical_draws
points
average_decision_ms
```

Cela permet d'archiver un tournoi complet ou d'analyser facilement les résultats dans un tableur.


---

## 14. Forfait sur erreur d'un bot

Un tournoi étudiant ne doit jamais être interrompu parce qu'un bot contient un bug.

`MatchRunner` intercepte donc les `RuntimeException` qui s'échappent de :

```java
ChessBot.decide(...)
```

Le comportement est alors :

```text
exception du bot
    ↓
arrêt propre de la partie
    ↓
forfait du bot fautif
    ↓
victoire de l'adversaire
    ↓
MatchIncident enregistré
    ↓
tournoi poursuivi
```

Un incident contient :

```java
incident.type();
incident.offenderColor();
incident.offender();
incident.exceptionClass();
incident.message();
incident.ply();
```

Les incidents de bot actuellement distingués sont :

```java
BOT_EXCEPTION
BOT_TIMEOUT
BOT_PROCESS_FAILURE
BOT_PROTOCOL_ERROR
BOT_ILLEGAL_MOVE
```

Le classement distingue les défaites par forfait des autres défaites.

Le PGN contient également des tags supplémentaires :

```text
[Termination "forfeit"]
[ForfeitBy "Nom du bot"]
[ForfeitReason "BOT_EXCEPTION"]
```

Le CSV du classement possède une colonne `forfeits`.

---

## 15. Isolation JVM et timeout dur

Le framework protège désormais le tournoi contre une boucle infinie ou un bot bloqué.

Activez l'isolation :

```bash
bash scripts/chess.sh console minimax random --isolated
```

ou pour un tournoi complet :

```bash
bash scripts/chess.sh tournament positional lookahead minimax --games=2 --isolated
```

Chaque bot d'une partie tourne alors dans une **JVM enfant persistante**, distincte de la JVM du tournoi. Une nouvelle JVM est créée à chaque nouvelle partie : un éventuel état interne ne fuit donc pas d'un match au suivant.

Les limites sont configurables :

```text
--timeout-ms=2000
--startup-timeout-ms=5000
--heap-mb=256
```

Exemple :

```bash
bash scripts/chess.sh tournament tactical positional minimax --isolated --timeout-ms=3000 --heap-mb=256
```

Si `ChessBot.decide(...)` dépasse le délai :

```text
timeout
  ↓
fermeture du canal
  ↓
Process.destroy()
  ↓
Process.destroyForcibly() si nécessaire
  ↓
forfait BOT_TIMEOUT
  ↓
tournoi poursuivi
```

Cette protection a un test d'intégration avec un bot volontairement bloqué dans une boucle infinie.

---

## 16. Protocole d'isolation

Le processus enfant ne communique pas via stdout. Le framework ouvre un socket **loopback** dédié et protégé par un jeton de session aléatoire.

Le protocole transporte uniquement les informations nécessaires :

- FEN de la position ;
- couleur ;
- coups légaux ;
- historique ;
- décision ;
- trace des règles et candidats évalués.

Les chaînes et listes reçues sont bornées avant allocation. stdout/stderr est drainé séparément et peut donc être utilisé par un étudiant pour du débogage sans corrompre le protocole.

Le heap de la JVM enfant est borné via `-Xmx`.

---

## 17. Limites de l'isolation actuelle

L'isolation de processus apporte un timeout dur et une limite mémoire JVM, mais ce n'est pas encore un sandbox système complet.

Le workflow de Pull Request contrôle déjà le périmètre des fichiers, bloque l'ajout de dépendances via les POM et refuse les usages directs évidents du réseau, du disque, des processus et de la réflexion.

Pour un environnement réellement hostile, des restrictions système supplémentaires — conteneurisation ou sandbox OS — resteraient nécessaires.

Pour un contexte pédagogique où les Pull Requests sont relues et passent la CI, le mécanisme actuel protège déjà le tournoi contre les erreurs, blocages et boucles infinies ordinaires.


---

## 18. Reproductibilité

Le moteur dérive une seed déterministe pour chaque partie du tournoi.

À configuration identique :

- mêmes participants dans le même ordre ;
- même nombre de parties ;
- même limite de demi-coups ;
- même seed ;

les séquences de coups pseudo-aléatoires sont identiques.

La CI vérifie cette propriété :

- en exécution locale ;
- avec des bots exécutés dans des JVM isolées ;
- sur un tournoi toutes rondes.

Les temps de décision ne participent pas aux départages afin de ne pas introduire de dépendance à la vitesse de la machine.
