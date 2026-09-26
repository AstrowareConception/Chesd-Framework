# Interfaces de partie

Le framework propose trois façons complémentaires d'observer un duel entre bots :

1. console en temps réel ;
2. export PGN ;
3. viewer graphique Swing.

Les trois modes utilisent le même `MatchRunner`.

---

## 1. Préparer le projet

À la racine du dépôt :

```bash
mvn verify
```

Cette commande compile, teste et produit le runner autonome.

Les exemples ci-dessous utilisent le lanceur Bash :

```bash
bash scripts/chess.sh ...
```

Sous Windows PowerShell, utilisez la même commande avec :

```powershell
powershell -ExecutionPolicy Bypass -File scripts/chess.ps1 ...
```

---

## 2. Afficher les bots disponibles

```bash
bash scripts/chess.sh list
```

La commande affiche tous les bots de référence ainsi que les bots étudiants découverts automatiquement dans le package `students`.

---

## 3. Mode console

Exemple :

```bash
bash scripts/chess.sh console tactical cautious
```

Le terminal affiche pour chaque demi-coup :

- numéro du coup ;
- bot qui joue ;
- notation SAN ;
- règle sélectionnée ;
- score ;
- sécurité ;
- risque ;
- candidats concurrents en mode détaillé.

Exemple conceptuel :

```text
1.     Tactical Bot       e4        [Plan : Prendre le centre] score=8.00/10
1...   Cautious Bot       Nf6       [Plan : Développer les pièces mineures] score=7.00/10
```

À la fin, la console affiche :

- vainqueur ou nulle ;
- résultat PGN ;
- nombre de coups ;
- nombre de demi-coups ;
- type de terminaison ;
- FEN finale.

---

## 4. Reproductibilité

Une seed peut être imposée :

```bash
bash scripts/chess.sh console tactical random --seed=123
```

Les choix aléatoires seront alors reproductibles.

Une limite technique peut également être fixée :

```bash
bash scripts/chess.sh console random random --max-plies=100
```

---

## 5. Export PGN

Vers la console :

```bash
bash scripts/chess.sh pgn tactical guardian
```

Vers un fichier :

```bash
bash scripts/chess.sh pgn tactical guardian partie.pgn
```

Le PGN contient notamment :

- White ;
- Black ;
- WhiteAuthor ;
- BlackAuthor ;
- Result ;
- PlyCount ;
- FEN et SetUp si la partie ne part pas de la position standard.

Les coups sont exportés en notation SAN :

```text
1. e4 e5 2. Nf3 Nc6 3. Bb5 ...
```

Le fichier peut ensuite être ouvert dans un viewer PGN compatible.

---

## 6. Viewer graphique

```bash
bash scripts/chess.sh gui architect tactical
```

Le viewer affiche :

- un échiquier 8x8 ;
- les pièces Unicode ;
- le dernier coup surligné ;
- la liste des coups ;
- les deux bots et leurs auteurs ;
- le résultat ;
- la règle ayant choisi le coup ;
- le score du candidat ;
- agressivité, sécurité et risque ;
- l'explication de l'action.

Commandes :

- `|<` : début ;
- `<` : coup précédent ;
- `Lecture` : lecture automatique ;
- `>` : coup suivant ;
- `>|` : fin ;
- `Retourner` : inverser l'échiquier.

On peut également cliquer directement sur un coup dans la liste.

---

## 7. Architecture Observer

`MatchRunner` ne connaît ni la console ni l'interface graphique.

Il émet des événements via :

```java
MatchListener
```

avec :

```java
onMatchStarted(...)
onMovePlayed(...)
onMatchEnded(...)
```

Cette architecture permet d'ajouter plus tard :

- un écran de tournoi en direct ;
- une interface web ;
- un logger JSON ;
- un WebSocket ;
- un export de statistiques ;
- un replay vidéo.

sans modifier le moteur de partie.

---

## 8. PlayedMove

Chaque coup joué conserve :

- son numéro de demi-coup ;
- sa couleur ;
- le bot ;
- la `BotDecision` complète ;
- la notation SAN ;
- la FEN avant le coup ;
- la FEN après le coup.

Le replay graphique ne recalcule donc pas approximativement la partie : chaque état affiché est un état réel produit par le moteur.

---

## 9. MatchResult

`MatchResult` fournit :

```java
result.pgnResult();
result.pliesPlayed();
result.fullMovesPlayed();
result.initialFen();
result.finalFen();
result.playedMoves();
```

Le même résultat sert à la console, au PGN et au viewer graphique.

---

## 10. Mode tournoi

Le CLI permet aussi un tournoi toutes rondes :

```bash
bash scripts/chess.sh tournament random greedy tactical
```

Tous les bots :

```bash
bash scripts/chess.sh tournament --all
```

Options principales :

```text
--games=N
--seed=N
--max-plies=N
--pgn=parties.pgn
--csv=classement.csv
```

Le classement affiche également le temps moyen de décision de chaque bot.

Voir `docs/TOURNAMENT.md`.


Chaque coup conserve également son temps de décision en millisecondes. La console et le viewer Swing affichent cette information pour comparer le coût des stratégies.
