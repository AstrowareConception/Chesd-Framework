# Spécifications fonctionnelles et pédagogiques

## 1. Objectif du projet

Chess Framework est un framework pédagogique permettant à des étudiants de programmer un bot d'échecs en Java sans avoir à réimplémenter eux-mêmes l'ensemble des règles du jeu.

Le framework doit fournir :

- un modèle objet du jeu d'échecs ;
- un moteur garantissant la légalité des coups ;
- une API publique de création de bots ;
- une bibliothèque riche de situations tactiques et stratégiques ;
- une bibliothèque d'actions réutilisables ;
- un mécanisme de règles ordonnées `Situation -> Action` ;
- des outils d'analyse d'une position ;
- des bots de référence ;
- un moteur de matchs et de tournois ;
- des traces explicables des décisions ;
- un cadre de contribution adapté à des Pull Requests étudiantes.

Le projet n'a pas pour objectif de construire le moteur d'échecs le plus performant possible. Il doit surtout fournir un terrain d'apprentissage propre, testable et extensible.

---

## 2. Objectifs pédagogiques

Le projet doit permettre de pratiquer concrètement :

- classes abstraites ;
- héritage ;
- interfaces ;
- polymorphisme ;
- encapsulation ;
- composition ;
- records et objets valeur ;
- collections ;
- génériques ;
- exceptions ;
- lambdas lorsque cela apporte de la lisibilité ;
- tests unitaires ;
- Maven ;
- Git ;
- branches ;
- Pull Requests ;
- revue de code ;
- principes SOLID ;
- design patterns.

Patterns particulièrement adaptés au projet :

- **Template Method** : cycle de décision commun fourni par `ChessBot` ;
- **Chain of Responsibility** : évaluation ordonnée des règles ;
- **Strategy** : actions, sélecteurs de coups et politiques de décision ;
- **Specification** : composition de situations ;
- **Adapter** : isolation d'une éventuelle bibliothèque tierce de règles d'échecs ;
- **Factory** : création de bots et de matchs ;
- **Observer** : événements de partie et journalisation ;
- **Builder** : définition lisible d'une règle ou d'un tournoi.

Le framework doit cependant rester utilisable sans exiger que les étudiants connaissent tous ces patterns au préalable.

---

## 3. Expérience développeur cible

Un étudiant doit pouvoir créer un bot en ajoutant principalement une classe :

```java
public final class AdaBot extends ChessBot {

    @Override
    public String name() {
        return "AdaBot";
    }

    @Override
    protected List<Rule<?>> rules() {
        return List.of(
            rule("Mat immédiat", Situations.mateInOne(), Actions.playDetectedMove()),
            rule("Défense du roi", Situations.inCheck(), Actions.bestEscape()),
            rule("Prise gratuite", Situations.hangingEnemyPiece(), Actions.captureHighestValue()),
            rule("Fourchette", Situations.forkOpportunity(), Actions.playDetectedMove()),
            rule("Roque", Situations.castlingAvailable(), Actions.castle()),
            rule("Développement", Situations.canDevelopPiece(), Actions.developBestPiece()),
            rule("Coup de secours", Situations.always(), Actions.randomLegalMove())
        );
    }
}
```

L'étudiant peut commencer uniquement avec les briques fournies puis, à mesure qu'il progresse :

1. combiner des situations ;
2. écrire ses propres situations ;
3. écrire ses propres actions ;
4. écrire ses propres fonctions d'évaluation ;
5. remplacer certaines heuristiques ;
6. éventuellement construire un bot plus avancé tout en respectant le contrat du framework.

---

## 4. Principe de décision

### 4.1 Règles ordonnées

Un bot expose une liste ordonnée de règles.

Chaque règle associe :

- un nom ;
- une priorité implicite par sa position dans la liste ;
- une situation à détecter ;
- une action à tenter lorsque cette situation est reconnue.

Pseudo-code :

```text
pour chaque règle du bot, dans l'ordre :
    analyser la situation
    si elle n'est pas reconnue :
        continuer
    tenter l'action associée
    si l'action produit un coup légal :
        jouer ce coup
        arrêter l'analyse
    sinon :
        continuer

si aucune règle ne produit de coup :
    appliquer la politique de secours
```

Ce comportement constitue la mécanique de base du framework.

### 4.2 Une situation ne doit pas seulement répondre vrai/faux

Certaines situations doivent transporter de l'information.

Exemples :

- une pièce pendue identifie la pièce concernée ;
- une fourchette identifie le coup créant la fourchette et les cibles ;
- une enfilade identifie l'attaquant et les pièces alignées ;
- une promotion identifie le pion et les cases possibles.

Le contrat interne doit donc permettre à une situation de retourner un résultat de détection exploitable par l'action associée.

La forme cible est générique :

```java
public interface Situation<T extends Detection> {
    Optional<T> detect(BotContext context);
}

public interface Action<T extends Detection> {
    Optional<Move> choose(BotContext context, T detection);
}
```

Les étudiants utilisant uniquement les situations et actions fournies n'auront pas besoin de manipuler directement toute la complexité des génériques.

### 4.3 Légalité

Un bot ne décide jamais lui-même si un coup est légal au sens complet des règles des échecs.

Le framework :

- fournit la liste des coups légaux ;
- valide tout coup proposé ;
- refuse un coup illégal ;
- poursuit éventuellement la chaîne de règles ;
- empêche qu'un bot modifie directement l'état interne de l'échiquier.

Le contexte remis au bot doit être en lecture seule.

---

### 4.4 Mémoire de partie et plans facultatifs

Le modèle `Situation -> Action` est volontairement réactif et doit rester suffisant pour un premier bot.

Cependant, un bot plus évolué doit pouvoir conserver une intention entre plusieurs coups. Exemples :

- préparer le roque sur plusieurs tours ;
- poursuivre une pièce cible ;
- conserver un objectif d'attaque sur une aile ;
- se souvenir qu'un plan vient d'échouer ;
- adapter son comportement à la phase de jeu.

Une instance de `ChessBot` est donc **limitée à une partie** et peut conserver un état interne typé.

Le framework doit fournir des hooks de cycle de vie sans obliger les étudiants à les utiliser :

```java
protected void onGameStart(GameContext context) {}

protected void onMovePlayed(GameEvent event) {}

protected void onGameEnd(GameResult result) {}
```

Une abstraction `Plan` pourra être ajoutée après la V1 pour les stratégies multi-coups. Elle ne doit pas remplacer les règles ordonnées : elle constitue une couche facultative destinée aux bots avancés.

Ce choix permet de conserver une API extrêmement simple pour un débutant tout en évitant de condamner le framework à des bots purement opportunistes et sans continuité stratégique.

---

## 5. Modèle de domaine minimal

Le domaine public doit au minimum exposer les concepts suivants :

- `Color`
- `PieceType`
- `Piece`
- `Square`
- `Move`
- `Position`
- `GameState`
- `GameResult`
- `BotContext`
- `ChessBot`
- `Rule`
- `Situation`
- `Action`

Les objets de valeur doivent être immuables autant que possible.

### 5.1 Valeur des pièces

Le framework fournit une politique par défaut configurable :

| Pièce | Valeur par défaut |
|---|---:|
| Pion | 1 |
| Cavalier | 3 |
| Fou | 3 |
| Tour | 5 |
| Dame | 9 |
| Roi | non quantifié / valeur spéciale |

Une API doit permettre à un bot de remplacer cette politique.

Exemple :

```java
PieceValues values = PieceValues.standard();
```

ou :

```java
PieceValues custom = PieceValues.builder()
    .pawn(100)
    .knight(320)
    .bishop(330)
    .rook(500)
    .queen(900)
    .build();
```

---

## 6. Bibliothèque d'analyse

Le framework doit fournir des services d'analyse sans imposer une stratégie.

### 6.1 Analyse élémentaire

- pièces présentes ;
- pièces d'une couleur ;
- coups légaux ;
- coups de capture ;
- cases contrôlées ;
- cases attaquées ;
- cases défendues ;
- attaquants d'une case ;
- défenseurs d'une case ;
- nombre d'attaquants ;
- nombre de défenseurs ;
- mobilité d'une pièce ;
- mobilité globale ;
- matériel total ;
- avantage matériel ;
- roi en échec ;
- échec et mat ;
- pat ;
- possibilité de roque ;
- possibilité de promotion.

### 6.2 Sécurité des pièces

Le framework doit pouvoir déterminer notamment :

- pièce non défendue ;
- pièce insuffisamment défendue ;
- pièce attaquée ;
- pièce attaquée par une pièce de valeur inférieure ;
- pièce pouvant être capturée immédiatement ;
- échange matériel potentiellement favorable ;
- case dangereuse ;
- déplacement qui laisse une pièce pendue.

La notion d'échange favorable reste une heuristique et doit être configurable.

### 6.3 Situations tactiques initialement visées

Le catalogue est extensible. La première version doit viser progressivement :

- échec ;
- mat en un ;
- menace de mat ;
- prise disponible ;
- pièce pendue ;
- pièce attaquée ;
- défense insuffisante ;
- défense multiple ;
- fourchette ;
- clouage ;
- enfilade ;
- attaque à la découverte ;
- double échec ;
- surcharge d'un défenseur ;
- déviation ;
- attraction ;
- élimination du défenseur ;
- batterie ;
- rayon X ;
- pion passé ;
- pion isolé ;
- pion doublé ;
- promotion imminente ;
- possibilité de roque ;
- roi exposé ;
- pièce enfermée ;
- pièce sous-développée ;
- contrôle du centre ;
- gain d'espace simple ;
- répétition potentielle ;
- menace immédiate sur une pièce de forte valeur.

Toutes ces situations ne sont pas obligatoires pour la première livraison technique. Elles constituent le catalogue cible.

### 6.4 Situations composables

Une situation doit pouvoir être combinée avec d'autres :

```java
Situation<?> safeCapture =
    Situations.captureAvailable()
        .and(Situations.targetValueAtLeast(PieceType.ROOK))
        .and(Situations.moveDoesNotHangQueen());
```

Combinators cibles :

- `and`
- `or`
- `not`
- éventuellement `when`, `filter`, `map` pour les usages avancés.

---

## 7. Bibliothèque d'actions

Actions génériques envisagées :

- jouer le coup détecté ;
- jouer un coup légal aléatoire ;
- capturer la pièce de plus forte valeur ;
- capturer la pièce la moins défendue ;
- sauver la pièce attaquée de plus forte valeur ;
- sortir d'échec ;
- donner échec ;
- mater si possible ;
- créer une fourchette ;
- exploiter un clouage ;
- exploiter une enfilade ;
- roquer ;
- promouvoir ;
- développer une pièce ;
- occuper ou contrôler le centre ;
- échanger des pièces ;
- éviter les échanges ;
- choisir le coup maximisant un score ;
- choisir le coup minimisant le risque ;
- jouer un coup parmi un ensemble de candidats.

Les actions ne doivent jamais contourner la validation de légalité du moteur.

---

## 8. Évaluation d'une position

Une API de scoring doit être disponible sans rendre obligatoire la programmation d'un moteur Minimax.

Exemple de composants :

```text
score =
    matériel
  + mobilité
  + sécurité du roi
  + activité des pièces
  + structure de pions
  + contrôle du centre
```

Contrat cible :

```java
public interface PositionEvaluator {
    double evaluate(BotContext context, Color perspective);
}
```

Le framework fournit :

- `MaterialEvaluator`
- `MobilityEvaluator`
- `KingSafetyEvaluator`
- `CenterControlEvaluator`
- `CompositeEvaluator`

Cela permet à un étudiant plus avancé d'utiliser une logique de sélection par score sans modifier le moteur de partie.

---

## 9. Bots de référence

### 9.1 RandomBot

Comportement :

- récupère les coups légaux ;
- en choisit un aléatoirement.

Utilité :

- vérifier qu'un bot sait gagner contre un adversaire sans stratégie ;
- disposer d'un adversaire très simple ;
- tester le moteur.

### 9.2 GreedyBot

Ordre de décision indicatif :

1. mat en un ;
2. sortir d'échec ;
3. capturer la pièce disponible de plus forte valeur ;
4. promouvoir ;
5. jouer un coup légal aléatoire.

Il ignore volontairement beaucoup de conséquences tactiques.

### 9.3 TacticalBot

Ordre indicatif :

1. mat en un ;
2. défense contre une menace immédiate ;
3. tactique gagnante simple ;
4. pièce adverse pendue ;
5. fourchette ;
6. clouage exploitable ;
7. mise en sécurité d'une pièce importante ;
8. développement ;
9. roque ;
10. coup de secours.

Ce bot doit rester battable par un travail étudiant raisonnable.

---

## 10. Explicabilité

Chaque décision doit pouvoir être tracée.

Exemple de trace :

```text
Tour 17 — AdaBot — Blancs

[1] Mat en un
    situation: non détectée

[2] Sortir d'échec
    situation: non détectée

[3] Prendre une pièce pendue
    situation: détectée
    cible: Tour noire en a8
    candidats: Bxa8, Qxa8
    action: captureHighestValue
    coup choisi: Bxa8
```

Le système de trace doit pouvoir être désactivé en tournoi pour éviter un coût inutile.

Usages :

- débogage ;
- explication pédagogique ;
- comparaison de stratégies ;
- visualisation future ;
- rapports de tournoi.

---

## 11. Moteur de partie

Le moteur doit gérer correctement :

- position initiale ;
- alternance des joueurs ;
- déplacements légaux ;
- captures ;
- roque ;
- prise en passant ;
- promotion ;
- échec ;
- échec et mat ;
- pat ;
- répétitions ;
- règle des cinquante coups ;
- matériel insuffisant ;
- abandon technique éventuel décidé par le runner ;
- historique des coups ;
- import/export FEN lorsque pertinent ;
- export PGN.

Les règles fondamentales du jeu ne doivent pas être confiées aux bots.

---

## 12. Abstraction du moteur d'échecs

Le framework ne doit pas exposer directement dans son API publique les classes d'une bibliothèque tierce.

Contrat interne cible :

```java
public interface ChessRulesEngine {
    Position initialPosition();
    List<Move> legalMoves(Position position);
    Position play(Position position, Move move);
    GameState state(Position position);
}
```

Une implémentation peut s'appuyer sur une bibliothèque existante.

Cette isolation permet :

- de changer de bibliothèque plus tard ;
- de tester indépendamment le framework ;
- de conserver un modèle public stable ;
- d'éviter que les étudiants deviennent dépendants d'une API externe.

Avant de figer le backend, une batterie de tests `perft` et de cas limites doit être exécutée.

---

## 13. Tournoi

### 13.1 Format initial

Format recommandé : toutes rondes.

Pour chaque paire de bots :

- une partie avec A blancs / B noirs ;
- une partie avec B blancs / A noirs.

Une configuration peut prévoir plusieurs confrontations et plusieurs graines.

### 13.2 Barème

Par défaut :

- victoire : 1 point ;
- nulle : 0,5 point ;
- défaite : 0 point.

Départages envisageables :

- Sonneborn-Berger ;
- nombre de victoires ;
- confrontation directe ;
- performance avec les noirs.

Le règlement définitif sera figé avant le tournoi pédagogique.

### 13.3 Reproductibilité

Toute source aléatoire doit recevoir une graine fournie par le runner.

Le même bot, dans la même position et avec la même graine, doit pouvoir reproduire son comportement lorsque sa stratégie est déterministe vis-à-vis de cette graine.

### 13.4 Temps de réflexion

Le runner doit pouvoir imposer :

- un temps maximal par coup ;
- un temps maximal par partie ;
- éventuellement un nombre maximal de demi-coups.

Un dépassement doit produire un résultat déterministe selon le règlement.

La première implémentation peut utiliser un contrôle simple, mais la version tournoi devra isoler suffisamment les bots pour qu'une boucle infinie ou une erreur étudiante ne bloque pas l'ensemble du tournoi.

### 13.5 Erreurs d'un bot

Le runner doit intercepter :

- exception non gérée ;
- coup illégal ;
- retour nul inattendu ;
- dépassement de temps.

La politique exacte peut être :

- tentative suivante lorsque l'erreur appartient à une règle interne récupérable ;
- défaite technique lorsque le contrat public du bot est violé.

---

## 14. Sécurité et isolation des soumissions

Les bots étudiants sont du code exécuté dans le contexte du tournoi.

Le runner final devra limiter autant que possible :

- accès réseau ;
- accès disque non nécessaire ;
- dépendances non autorisées ;
- création incontrôlée de threads ;
- blocages permanents ;
- modification d'état global partagé.

Pour un premier tournoi local encadré, cette contrainte peut être progressive. Une exécution en processus JVM séparé constitue la cible robuste.

---

## 15. Soumission par Pull Request

Workflow cible :

1. l'étudiant fork le dépôt ou crée une branche selon les droits choisis ;
2. il ajoute son bot dans l'espace prévu ;
3. il ajoute ses tests ;
4. il exécute `mvn test` ;
5. il ouvre une Pull Request ;
6. la CI compile le framework et le bot ;
7. la CI vérifie le respect du contrat ;
8. une revue de code est effectuée ;
9. la PR est fusionnée avant la date limite du tournoi.

Le template de PR devra demander :

- nom du bot ;
- auteur ou équipe ;
- résumé de la stratégie ;
- situations principales utilisées ;
- fonctionnalités personnalisées ajoutées ;
- limites connues ;
- confirmation du passage des tests.

---

## 16. Contraintes sur les bots étudiants

À figer avant le lancement du tournoi. Proposition initiale :

- une classe principale étendant `ChessBot` ;
- pas de modification du framework pour avantager son bot ;
- pas d'accès direct à l'implémentation interne du moteur ;
- pas de dépendance externe sans autorisation ;
- pas d'accès réseau ;
- pas de lecture du code ou de l'état privé d'un adversaire ;
- pas de comportement dépendant de l'identité de l'adversaire, sauf si explicitement autorisé ;
- respect du temps de calcul ;
- tests obligatoires pour les situations personnalisées importantes.

---

## 17. Tests

Le framework doit contenir plusieurs niveaux de tests.

### 17.1 Domaine

- coordonnées ;
- pièces ;
- valeurs ;
- mouvements ;
- sérialisation éventuelle.

### 17.2 Règles d'échecs

- cas unitaires connus ;
- positions FEN ;
- roque ;
- en passant ;
- promotions ;
- échecs ;
- mats ;
- pats ;
- répétitions ;
- tests `perft`.

### 17.3 Situations

Chaque situation fournie doit avoir :

- au moins un cas positif ;
- au moins un cas négatif ;
- des cas limites pertinents.

### 17.4 Actions

Vérifier notamment :

- légalité du coup retourné ;
- sélection correcte parmi plusieurs candidats ;
- respect des valeurs de pièces ;
- comportement lorsqu'aucun coup n'est possible.

### 17.5 Bots de référence

Tests de propriétés simples :

- RandomBot retourne toujours un coup légal ;
- GreedyBot prend la meilleure prise attendue dans une position contrôlée ;
- TacticalBot reconnaît les motifs explicitement couverts.

### 17.6 Tournoi

- alternance des couleurs ;
- scoring ;
- égalités ;
- erreurs ;
- timeout ;
- reproductibilité ;
- génération PGN.

---

## 18. Critères de réussite de la première version pédagogique

Une V1 est considérée exploitable lorsque :

- le projet se construit en une commande ;
- un étudiant peut créer un bot en une classe ;
- un bot peut déclarer une liste ordonnée de règles ;
- au moins 15 situations utiles sont fournies ;
- au moins 10 actions génériques sont fournies ;
- les règles peuvent être composées ;
- trois bots de référence fonctionnent ;
- deux bots peuvent jouer une partie complète ;
- les parties sont exportables ;
- un tournoi toutes rondes peut être lancé ;
- les décisions peuvent être tracées ;
- la CI valide les Pull Requests ;
- la documentation contient un tutoriel de création d'un bot.

---

## 19. Hors périmètre initial

Ne sont pas nécessaires pour la première version :

- interface graphique complète ;
- serveur web ;
- multijoueur réseau ;
- moteur de niveau grand maître ;
- réseau neuronal ;
- entraînement automatique ;
- compatibilité UCI complète ;
- base d'ouvertures massive ;
- tablebases de finales ;
- calcul distribué.

Ces éléments pourront faire l'objet d'extensions ultérieures.
