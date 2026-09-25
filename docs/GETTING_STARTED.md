# Bien démarrer — Guide étudiant

Ce guide accompagne progressivement la découverte du Chess Framework.

L'objectif n'est pas seulement de fabriquer un bot qui joue aux échecs. Le projet sert aussi à comprendre comment plusieurs concepts de programmation orientée objet coopèrent dans une application réelle.

---

## 1. Ce que vous allez construire

Votre travail final sera une classe Java représentant **votre propre joueur d'échecs**.

Vous choisirez :

- le **nom du bot** ;
- votre **nom** ou celui de votre équipe ;
- ses priorités ;
- les situations qu'il sait reconnaître ;
- les actions qu'il tente ;
- éventuellement ses propres outils d'analyse.

Exemple d'identité :

```java
@Override
public BotMetadata metadata() {
    return new BotMetadata(
        "Deep Rabbit",
        "Alice Dupont",
        "Bot prudent qui privilégie les tactiques et la sécurité du roi."
    );
}
```

Le nom du bot n'est pas le nom de la classe imposé par le framework. Vous pouvez donner une véritable identité à votre joueur.

Ces métadonnées seront ensuite utilisées dans les traces et dans le tournoi.

---

## 2. Prérequis

Le projet utilise :

- Java 25 ;
- Maven ;
- Git ;
- GitHub ;
- JUnit.

Vérifiez Java :

```bash
java --version
```

Vérifiez Maven :

```bash
mvn --version
```

Puis clonez le dépôt et exécutez :

```bash
mvn verify
```

Cette commande :

1. compile tous les modules ;
2. compile les tests ;
3. exécute les tests ;
4. génère les données de couverture JaCoCo ;
5. vérifie que l'ensemble du projet est cohérent.

Avant toute Pull Request, cette commande doit fonctionner.

---

## 3. Comprendre les quatre modules

Le framework est un projet Maven multi-module.

```text
chess-framework
├── chess-core
├── chess-bot-sdk
├── chess-bots
└── chess-tournament
```

### chess-core

Contient les concepts fondamentaux du jeu :

- couleur ;
- pièce ;
- case ;
- coup ;
- position ;
- plus tard : moteur et état de partie.

Un bot ne devrait normalement pas y ajouter de logique stratégique.

### chess-bot-sdk

C'est la boîte à outils du développeur de bot :

- `ChessBot` ;
- `Situation` ;
- `Detection` ;
- `Action` ;
- `Rule` ;
- `BotContext`.

La majorité des concepts intéressants pour votre stratégie se trouvent ici.

### chess-bots

Contient les bots.

Vous y trouverez les adversaires de référence et, plus tard, les soumissions étudiantes.

### chess-tournament

Contiendra le moteur permettant d'organiser les matchs et le tournoi final.

---

## 4. Première notion POO : les objets valeur

Regardez par exemple :

```java
public record Piece(Color color, PieceType type) {
}
```

Une pièce est avant tout une **valeur** composée :

- d'une couleur ;
- d'un type.

Nous utilisons un `record` parce que Java peut alors générer automatiquement plusieurs comportements classiques :

- constructeur ;
- accesseurs ;
- `equals` ;
- `hashCode` ;
- `toString`.

Cela rend explicite une idée importante :

> deux objets `Piece(WHITE, QUEEN)` décrivent la même valeur.

Même principe pour `Square`, `Move` et `PlacedPiece`.

---

## 5. Pourquoi utiliser des enum ?

`Color`, `PieceType`, `BoardFile` et `BoardRank` sont des `enum`.

Exemple :

```java
public enum Color {
    WHITE,
    BLACK
}
```

On préfère cela à une chaîne comme :

```java
String color = "white";
```

car une chaîne permet également :

```java
String color = "bleu";
```

Un `Color`, lui, ne peut contenir qu'une valeur prévue par le modèle.

C'est le principe de **rendre les états invalides difficiles à représenter**.

---

## 6. Votre classe étend ChessBot

Tous les bots partagent le même comportement général.

Ils doivent :

1. avoir une identité ;
2. exposer des règles ;
3. analyser ces règles dans l'ordre ;
4. produire un coup légal.

Cette mécanique commune est placée dans la classe abstraite `ChessBot`.

Votre bot l'étend :

```java
public final class DeepRabbitBot extends ChessBot {
}
```

### Pourquoi l'héritage est pertinent ici ?

Parce qu'il existe réellement une relation :

> DeepRabbitBot **est un** ChessBot.

La classe mère possède l'algorithme commun ; la classe fille personnalise certaines parties.

C'est le pattern **Template Method**.

---

## 7. Donner une identité à votre bot

Vous devez redéfinir :

```java
@Override
public BotMetadata metadata() {
    return new BotMetadata(
        "Deep Rabbit",
        "Alice Dupont",
        "Description libre."
    );
}
```

### Règles

- `botName` : nom libre du bot ;
- `authorName` : votre nom ou le nom de votre équipe ;
- `description` : quelques mots sur sa personnalité.

Le framework refuse un nom de bot ou un auteur vide.

---

## 8. Principe fondamental : Situation → Action

Une règle répond à cette question :

> « Si je reconnais cette situation, qu'est-ce que j'essaie de faire ? »

Exemple humain :

```text
SI je peux mater en un coup
ALORS jouer le mat
```

ou :

```text
SI une pièce adverse n'est pas défendue
ALORS essayer de capturer la plus intéressante
```

Dans le framework :

```java
rule(
    "Nom lisible de la règle",
    uneSituation,
    uneAction
)
```

Les règles sont évaluées **dans l'ordre**.

La première produisant un coup légal arrête la recherche.

L'ordre constitue donc déjà une stratégie.

---

## 9. Une Situation n'est pas seulement un boolean

Une première idée serait :

```java
boolean isForkAvailable();
```

Mais savoir qu'une fourchette existe n'est pas suffisant.

Le bot doit aussi savoir :

- quel coup la crée ;
- quelle pièce la crée ;
- quelles pièces sont attaquées.

C'est le rôle de `Detection`.

Une future détection pourra ressembler à :

```java
public record ForkDetection(
    Move move,
    PlacedPiece attacker,
    List<PlacedPiece> targets
) implements Detection {
}
```

Ainsi :

```text
Situation
    ↓
Detection
    ↓
Action
```

L'analyse n'est pas perdue entre les deux composants.

---

## 10. Pourquoi Situation et Action sont des interfaces ?

Une interface décrit ici un **contrat**.

```java
public interface Situation<D extends Detection> {
    List<D> detect(BotContext context);
}
```

Cela signifie :

> « Toute situation sait analyser un contexte et produire des détections. »

Peu importe comment elle le fait.

On pourra donc avoir :

- `ForkSituation` ;
- `CheckSituation` ;
- `HangingPieceSituation` ;
- votre propre situation.

Et `ChessBot` n'aura pas besoin d'être modifié.

C'est une application du principe **Open/Closed**.

---

## 11. Pourquoi le type générique D ?

Observez :

```java
Situation<D>
Action<D>
Rule<D>
```

Le même type `D` relie la situation et l'action.

Ainsi une action destinée à exploiter une `ForkDetection` reçoit bien des informations de fourchette.

L'objectif est d'obtenir davantage de sécurité à la compilation et d'éviter des conversions manuelles dangereuses.

Vous n'aurez pas besoin de créer vos propres types génériques pour commencer.

---

## 12. Le premier bot de référence

Ouvrez :

```text
chess-bots/src/main/java/
fr/astroware/chess/bots/baseline/RandomBot.java
```

Son comportement complet est :

```java
@Override
protected List<Rule<?>> rules() {
    return List.of(
        rule(
            "Jouer au hasard",
            Situations.always(),
            Actions.randomLegalMove()
        )
    );
}
```

On peut presque lire le programme comme une phrase :

> Toujours → jouer un coup légal au hasard.

C'est volontaire.

La classe d'un bot doit rester la plus lisible possible.

---

## 13. Le moteur protège le bot

Votre action peut proposer **plusieurs coups candidats**.

Chaque candidat possède une note de 0 à 10 et peut expliquer les critères ayant conduit à cette note.

Elle ne modifie jamais directement l'échiquier.

Le framework élimine ensuite les candidats illégaux et choisit le candidat légal ayant la meilleure note.

Une règle peut donc connaître quatre situations principales :

1. situation non reconnue ;
2. situation reconnue mais aucune action disponible ;
3. coup proposé mais illégal ;
4. coup légal sélectionné.

Ces informations sont stockées dans `RuleAttempt`.

Cela permettra d'expliquer les décisions d'un bot.

---

## 14. Le fallback

Imaginez que toutes vos règles échouent.

Le framework ne veut pas qu'un oubli de programmation suffise à faire perdre immédiatement la partie.

`ChessBot` possède donc un dernier filet de sécurité :

> si aucune règle ne produit un coup, choisir aléatoirement un coup légal.

Vous devriez néanmoins prévoir votre propre dernière règle afin que le comportement soit explicite.

---

## 15. Premier squelette de bot étudiant

Voici la forme que vous pourrez utiliser :

```java
package fr.astroware.chess.bots.students;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.situation.Situations;

import java.util.List;

public final class DeepRabbitBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Deep Rabbit",
            "Alice Dupont",
            "Mon premier bot d'échecs."
        );
    }

    @Override
    protected List<Rule<?>> rules() {
        return List.of(
            rule(
                "Secours",
                Situations.always(),
                Actions.randomLegalMove()
            )
        );
    }
}
```

À ce stade, ce bot joue comme RandomBot.

Ce sera votre point de départ.

---

## 16. Progression prévue

Au fur et à mesure du cours, le framework ajoutera notamment :

- analyse du matériel ;
- attaquants et défenseurs ;
- pièces pendues ;
- prises rentables ;
- échec ;
- mat en un ;
- fourchettes ;
- clouages ;
- enfilades ;
- roque ;
- développement ;
- contrôle du centre ;
- évaluation d'une position.

Vous pourrez remplacer progressivement votre règle aléatoire par de vraies décisions.

---

## 17. Tests

Les tests ne servent pas uniquement à vérifier que le programme « marche aujourd'hui ».

Ils documentent également le comportement attendu.

Par exemple :

```java
@Test
void rejectsBlankAuthorName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> BotMetadata.of("Deep Rabbit", " ")
    );
}
```

Ce test exprime clairement une règle métier :

> un bot de tournoi possède toujours un auteur identifié.

---

## 18. Git et Pull Request

Votre bot final sera soumis par Pull Request.

Le workflow attendu sera :

```text
main
  ↓
votre branche
  ↓
votre bot + vos tests
  ↓
mvn verify
  ↓
push
  ↓
Pull Request
  ↓
CI
  ↓
revue
  ↓
merge
  ↓
tournoi
```

Le fichier `.github/pull_request_template.md` vous guidera lors du rendu.

Pour la procédure complète de tournoi — emplacement imposé, validation, isolation JVM, clé `student-*`, mini-tournoi local et Pull Request — consultez :

```text
docs/STUDENT_TOURNAMENT_BOT.md
```

Avant le rendu final :

```bash
mvn verify
mvn install
cd chess-tournament
mvn exec:java -Dexec.args="validate-students"
```

---

## 19. Ce qu'il faut retenir

À ce stade :

- le domaine utilise des objets immuables ;
- `ChessBot` fournit le comportement commun ;
- votre classe hérite de `ChessBot` ;
- votre bot possède une identité explicite ;
- une règle associe une situation et une action ;
- une situation produit des informations de détection ;
- une action transforme ces informations en coups candidats évalués ;
- chaque candidat peut recevoir une note de 0 à 10 ;
- le framework vérifie la légalité puis retient le meilleur candidat ;
- les règles sont testées dans l'ordre ;
- le tournoi sera reproductible et automatisable.

La suite du projet consistera à enrichir le vocabulaire disponible pour que vous puissiez écrire des comportements de plus en plus intéressants.


---

## 20. Évaluer plusieurs coups

Une règle ne se limite pas à répondre :

> « j'ai trouvé un coup ».

Elle peut répondre :

> « j'ai trouvé trois coups possibles, je les ai évalués, voici celui que je préfère ».

Exemple :

```text
Bxc6  -> 6,2 / 10
Nxe5  -> 5,7 / 10
Qxa8  -> 9,1 / 10
```

Le framework représente cela avec `EvaluatedMove`.

```java
EvaluatedMove.of(
    Move.of("d2", "d4"),
    8.5,
    "Bon contrôle du centre"
);
```

La note n'est pas une vérité absolue. Elle exprime la préférence de votre stratégie.

Un bot agressif et un bot prudent pourront donc attribuer des notes différentes au même coup.

Pour approfondir ce mécanisme, lisez :

```text
docs/EVALUATION.md
```


---

## 21. Donner une personnalité à votre bot

Le framework fournit maintenant des profils stratégiques.

```java
@Override
protected StrategyProfile strategyProfile() {
    return StrategyProfiles.defensive();
}
```

Profils disponibles :

```java
StrategyProfiles.balanced();
StrategyProfiles.defensive();
StrategyProfiles.solid();
StrategyProfiles.aggressive();
StrategyProfiles.adventurous();
```

Le profil intervient lorsqu'une règle dispose de plusieurs coups candidats.

Chaque candidat peut indiquer :

- son score général ;
- son agressivité ;
- sa sécurité ;
- son niveau de risque.

Le même ensemble de coups peut donc être interprété différemment par deux bots.

---

## 22. Suivre une stratégie sur plusieurs coups

Un `StrategicPlan` représente un objectif.

Exemple :

```java
Plans.castleKingside().asRule()
```

Le plan ne dit pas :

> « joue exactement Nf3, puis Be2, puis O-O ».

Il réévalue la partie à chaque tour.

Il peut donc choisir une autre route si nécessaire.

Plans actuellement fournis :

```java
Plans.takeCenter();
Plans.developMinorPieces();
Plans.castleKingside();
```

Consultez `docs/STRATEGIES.md` pour comprendre leur fonctionnement.

---

## 23. Utiliser une ouverture

Une ouverture peut elle aussi être insérée comme une règle :

```java
Openings.londonSystem().asRule()
```

ou :

```java
Openings.scandinavianDefense().asRule()
```

Le livre suit l'historique complet des coups.

S'il ne connaît plus la position, il ne propose simplement aucun coup et le bot passe à la règle suivante.

Consultez `docs/OPENINGS.md`.

---

## 24. Exemple complet à lire

Le projet contient désormais :

```text
chess-bots/src/main/java/
fr/astroware/chess/bots/examples/SolidPlannerBot.java
```

Ce bot montre dans une seule classe :

- des métadonnées ;
- un profil stratégique ;
- une ouverture avec les Blancs ;
- une ouverture avec les Noirs ;
- plusieurs plans ;
- un fallback.

Il constitue le meilleur exemple actuel avant l'arrivée des situations tactiques avancées.
