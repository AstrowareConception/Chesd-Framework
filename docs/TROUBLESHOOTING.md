# Dépannage étudiant

Ce document regroupe les problèmes les plus fréquents lors de l'installation, de la création d'un bot, de son exécution locale et de sa soumission.

---

## 1. Vérifier l'environnement

Le framework demande :

- Java 25 ;
- Maven 3.9.x ;
- Git.

Vérifiez :

```bash
java --version
mvn --version
git --version
```

La version Java active doit être **25** et Maven doit être une version **3.9.x**.

Si Maven utilise un autre JDK que votre terminal, regardez la ligne `Java version` affichée par :

```bash
mvn --version
```

---

## 2. Compiler une première fois

À la racine du dépôt :

```bash
mvn verify
```

Cette commande doit terminer par :

```text
BUILD SUCCESS
```

Elle :

- compile les modules ;
- compile et exécute les tests ;
- construit les JAR ;
- produit le runner autonome du tournoi.

Ne lancez pas le tournoi avant cette première compilation.

---

## 3. « Runner introuvable »

Si :

```bash
bash scripts/chess.sh list
```

ou :

```powershell
powershell -ExecutionPolicy Bypass -File scripts/chess.ps1 list
```

indique que le runner est introuvable, exécutez :

```bash
mvn verify
```

Le lanceur cherche automatiquement :

```text
chess-tournament/target/chess-tournament-*-runner.jar
```

Vous n'avez pas besoin de connaître le numéro de version du JAR.

---

## 4. PowerShell refuse d'exécuter le script

Sur certaines machines Windows, la politique d'exécution PowerShell bloque les scripts locaux.

Utilisez :

```powershell
powershell -ExecutionPolicy Bypass -File scripts/new-student-bot.ps1 DeepRabbitBot "Alice Dupont" "Deep Rabbit"
```

et pour le CLI :

```powershell
powershell -ExecutionPolicy Bypass -File scripts/chess.ps1 list
```

Le contournement `Bypass` ne s'applique qu'au processus PowerShell lancé par cette commande.

---

## 5. Le générateur refuse le nom de classe

Le nom de classe doit :

- commencer par une majuscule ;
- ne contenir que lettres et chiffres ;
- se terminer par `Bot`.

Valide :

```text
DeepRabbitBot
Alice2026Bot
DefensiveDragonBot
```

Invalide :

```text
deepRabbitBot
Deep-Rabbit-Bot
DeepRabbit
```

La clé CLI est générée automatiquement :

```text
DeepRabbitBot
    ↓
student-deep-rabbit
```

---

## 6. Mon bot n'apparaît pas dans `list`

Vérifiez d'abord son emplacement :

```text
chess-bots/src/main/java/fr/astroware/chess/bots/students/
```

et son package :

```java
package fr.astroware.chess.bots.students;
```

Puis recompilez :

```bash
mvn verify
```

et relancez :

```bash
bash scripts/chess.sh validate-students
bash scripts/chess.sh list
```

Sous Windows, utilisez `scripts/chess.ps1`.

Le catalogue découvre les classes **compilées** : modifier un fichier Java sans relancer Maven ne met pas à jour le runner.

---

## 7. `validate-students` refuse mon bot

Le validateur exige notamment :

- une classe `public` ;
- non `abstract` ;
- qui étend `ChessBot` ;
- un constructeur public sans argument ;
- un nom de bot non vide ;
- un auteur non vide ;
- une description non vide ;
- un nom de bot unique.

Il exécute aussi un smoke-test réel en JVM isolée avec les Blancs et avec les Noirs.

Lisez le message d'incident : il distingue notamment exception, timeout, erreur de protocole, échec du processus et coup illégal.

---

## 8. Mon bot perd par `BOT_TIMEOUT`

Votre méthode de décision a dépassé le budget autorisé.

Pour tester avec les limites du tournoi :

```bash
bash scripts/chess.sh console student-deep-rabbit random --isolated --timeout-ms=2000 --heap-mb=256
```

Causes fréquentes :

- boucle infinie ;
- recherche récursive trop profonde ;
- trop de projections de positions ;
- exploration exhaustive d'un trop grand nombre de coups.

Réduisez la profondeur ou le nombre de candidats explorés. Les API `SearchSettings.bounded(...)` sont prévues pour cela.

---

## 9. Mon bot joue, mais pas le coup que j'attendais

Consultez les traces en mode console.

Le framework peut afficher :

- la règle retenue ;
- les candidats évalués ;
- le score ;
- la sécurité ;
- le risque ;
- l'explication de l'action.

Gardez en tête que :

1. les règles sont évaluées dans l'ordre ;
2. une règle peut produire plusieurs candidats ;
3. le profil stratégique peut modifier leur préférence ;
4. un candidat illégal est éliminé ;
5. un fallback légal existe si aucune règle ne produit de coup.

Pour déboguer une situation précise, écrivez un test JUnit sur une position FEN minimale.

---

## 10. Une position FEN fait planter mon test

Une FEN doit décrire une position échiquéenne cohérente.

Vérifiez notamment :

- présence des deux rois ;
- camp au trait correct ;
- roi du camp qui vient de jouer non laissé illégalement en échec ;
- droits de roque cohérents ;
- case en passant cohérente si elle est renseignée.

Pour un test pédagogique, préférez une position minimale contenant uniquement les pièces nécessaires au motif étudié, mais gardez toujours les deux rois dans une configuration légale.

---

## 11. La CI refuse ma Pull Request alors que `mvn verify` passe

Une Pull Request de bot possède des règles supplémentaires.

Elle doit modifier uniquement :

```text
chess-bots/src/main/java/fr/astroware/chess/bots/students/
chess-bots/src/test/java/fr/astroware/chess/bots/students/
```

Elle doit contenir :

- exactement une classe étendant `ChessBot` ;
- au moins un test étudiant.

Elle ne doit pas modifier :

- les POM ;
- le moteur ;
- le SDK ;
- le tournoi ;
- les workflows GitHub.

Si vous avez besoin d'une évolution du framework, faites-en une contribution séparée.

---

## 12. La CI refuse une API interdite

Une soumission de tournoi n'a pas besoin d'utiliser directement :

- le réseau ;
- le système de fichiers ;
- la réflexion ;
- les processus externes ;
- `System.exit`.

Ces usages sont bloqués pour préserver l'équité et la stabilité du tournoi.

Votre stratégie doit travailler avec les informations fournies par `BotContext`, `PositionView` et `Analysis`.

---

## 13. Tester contre plusieurs adversaires

Commencez par un bot simple :

```bash
bash scripts/chess.sh console student-deep-rabbit random --isolated
```

Puis testez plusieurs styles :

```bash
bash scripts/chess.sh console student-deep-rabbit greedy --isolated
bash scripts/chess.sh console student-deep-rabbit tactical --isolated
bash scripts/chess.sh console student-deep-rabbit positional --isolated
```

Enfin lancez un mini-tournoi :

```bash
bash scripts/chess.sh tournament student-deep-rabbit random greedy tactical --games=2 --isolated
```

---

## 14. Exporter pour analyser une partie

PGN d'un duel :

```bash
bash scripts/chess.sh pgn student-deep-rabbit tactical partie.pgn --isolated
```

Tournoi avec PGN et CSV :

```bash
bash scripts/chess.sh tournament student-deep-rabbit random tactical --games=2 --isolated --pgn=parties.pgn --csv=classement.csv
```

Le PGN peut être chargé dans un viewer d'échecs compatible.

Le CSV peut être ouvert dans un tableur.

---

## 15. Repartir proprement

Pour supprimer les artefacts Maven :

```bash
mvn clean
```

Puis reconstruire :

```bash
mvn verify
```

N'effacez pas votre classe étudiant ni ses tests.

---

## 16. Avant de demander de l'aide

Préparez :

1. la commande exacte lancée ;
2. le message d'erreur complet ;
3. votre version de Java ;
4. votre version de Maven ;
5. le résultat de `git status` ;
6. si possible, un test minimal qui reproduit le problème.

Cela permet de distinguer rapidement un problème d'environnement, de framework ou de stratégie.
