# Roadmap

## État synthétique

Le socle pédagogique est opérationnel et la CI est verte.

Déjà disponibles :

- [x] Java 25 + Maven multi-module
- [x] JUnit + JaCoCo + GitHub Actions
- [x] objets valeur du domaine
- [x] `ChessBot`, `Situation`, `Detection`, `Action`, `Rule`
- [x] candidats évalués de 0 à 10
- [x] personnalité stratégique
- [x] prise en compte de la sécurité, de l'agressivité et du risque
- [x] historique des coups
- [x] notation UCI simple
- [x] ouvertures multi-variantes
- [x] Système de Londres
- [x] Défense Scandinave
- [x] plans multi-coups
- [x] plan de prise du centre
- [x] plan de développement
- [x] plan de petit roque
- [x] bot exemple combinant ouvertures + plans + profil
- [x] documentation étudiante progressive

La priorité suivante est le **workflow étudiant de tournoi** : validation automatique des soumissions, documentation finale et gel du framework avant l'épreuve.

---

## Phase 0 — Cadrage

- [x] Vision du projet
- [x] Spécifications fonctionnelles
- [x] Architecture cible
- [x] ADR Java 25 / Maven
- [x] ADR profils / plans / ouvertures
- [ ] spike comparatif des bibliothèques de règles d'échecs
- [x] batterie de positions FEN ciblées
- [x] tests perft (position initiale, profondeurs 1 à 3)
- [x] choix initial du backend de règles (`chessgame` 2.3)

---

## Phase 1 — Build et CI

- [x] parent Maven
- [x] `chess-core`
- [x] `chess-bot-sdk`
- [x] `chess-bots`
- [x] `chess-tournament`
- [x] Java 25
- [x] JUnit
- [x] JaCoCo
- [x] GitHub Actions
- [ ] Checkstyle ou équivalent léger

**Critère :** `mvn verify` passe dans GitHub Actions.

---

## Phase 2 — Domaine et moteur d'échecs

### Domaine

- [x] `Color`
- [x] `PieceType`
- [x] `Piece`
- [x] `Square`
- [x] `Move`
- [x] notation UCI
- [x] `PlacedPiece`
- [x] `PositionView`

### Moteur

- [x] `GameStatus`
- [x] `GameResult`
- [x] `ChessRulesEngine`
- [x] adapter vers `chessgame` 2.3
- [x] FEN
- [x] PGN
- [x] perft
- [x] roque
- [x] en passant
- [x] promotion
- [x] répétitions
- [x] règle des cinquante coups
- [x] matériel insuffisant

---

## Phase 3 — SDK de bot

- [x] `ChessBot`
- [x] `BotMetadata`
- [x] nom du bot et auteur
- [x] `BotContext`
- [x] historique
- [x] `Detection`
- [x] `Situation<D>`
- [x] `Action<D>`
- [x] `Rule<D>`
- [x] `BotDecision`
- [x] trace de décision
- [x] fallback
- [ ] hooks de cycle de vie complets

---

## Phase 3 bis — Stratégie de haut niveau

- [x] `EvaluationScore`
- [x] `EvaluatedMove`
- [x] plusieurs candidats par règle
- [x] `StrategyProfile`
- [x] profils équilibré, défensif, solide, offensif, aventureux
- [x] tolérance au risque
- [x] `StrategicPlan`
- [x] progression d'un plan
- [x] `OpeningBook`
- [x] `OpeningLine`
- [x] plusieurs variantes d'une ouverture
- [x] abandon automatique du livre lorsqu'il ne correspond plus
- [x] `SolidPlannerBot` comme exemple pédagogique

Extensions prévues :

- [ ] plan d'attaque du roi
- [ ] plan de simplification avec avantage matériel
- [x] plan de création d'un pion passé
- [x] plan de sécurité du roi
- [x] plan d'occupation d'une colonne ouverte
- [ ] plan d'attaque sur une aile
- [ ] davantage d'ouvertures
- [ ] arbre d'ouvertures optimisé si le catalogue devient volumineux

---

## Phase 4 — Analyse de position

Implémenter :

- [x] `Analysis`
- [x] `AttackMap`
- [x] cases attaquées
- [x] cases défendues
- [x] attaquants d'une case
- [x] défenseurs d'une case
- [x] nombre d'attaquants / défenseurs
- [x] matériel
- [x] balance matérielle
- [x] captures disponibles
- [x] mobilité heuristique
- [x] contrôle du centre
- [x] projection après un coup
- [x] cache de projection par décision
- [x] phase de jeu
- [x] règles conditionnées par phase
- [x] profil stratégique dépendant du contexte
- [x] recherche adversariale profondeur 2
- [x] Minimax configurable
- [x] alpha-bêta
- [x] variante principale et statistiques de recherche
- [x] pré-sélection configurable des candidats
- [x] sécurité simple d'une pièce
- [x] détection de pièce pendue

**Critère :** une situation tactique ne doit pas recalculer elle-même les informations élémentaires du plateau.

---

## Phase 5 — Catalogue de situations

Lot V1 :

1. toujours vraie — [x]
2. en échec — [x]
3. mat en un — [x]
4. capture disponible — [x]
5. pièce adverse pendue — [x]
6. pièce alliée pendue — [x]
7. pièce attaquée — [x]
8. défense insuffisante — [x]
9. promotion disponible — [x]
10. roque disponible — [x]
11. fourchette — [x]
12. clouage — [x]
13. enfilade — [x]
14. développement possible — [x]
15. contrôle du centre — [x]

Puis :

- attaque à la découverte — [x]
- double échec — [x]
- surcharge — [x]
- déviation — [x]
- attraction — [x]
- élimination du défenseur surchargé — [x]
- batterie — [x]
- rayon X — [x]
- pion passé — [x]
- pion isolé — [x]
- pion doublé — [x]
- roi exposé / sécurité du roi — [x]

---

## Phase 6 — Catalogue d'actions

- [x] coup légal aléatoire
- [x] jouer le coup détecté
- [x] meilleure capture matérielle
- [x] sortie d'échec
- [x] mat en un
- [x] sauver une pièce pendue / menacée
- [x] meilleure fourchette
- [x] promotion
- [x] roque
- [x] meilleur coup selon un évaluateur
- [x] évitement du mat en un / risque de roi
- [x] activité via mobilité / centre

---

## Phase 7 — Bots de référence

- [x] `RandomBot`
- [x] `GreedyBot`
- [x] `CautiousBot`
- [x] `BerserkerBot`
- [x] `GuardianBot`
- [x] `TacticalBot`
- [x] `PressureBot`
- [x] `ChameleonBot`
- [x] `PositionalBot`
- [x] `LookaheadBot`
- [x] `MinimaxBot`
- [x] `SolidPlannerBot` comme exemple pédagogique supplémentaire

---

## Phase 8 — Moteur de partie et tournoi

- [x] match bot contre bot
- [x] alternance des couleurs
- [x] toutes rondes
- [x] scoring
- [x] seed
- [x] mesure du temps par coup
- [x] capture des exceptions de bot / forfait
- [x] incidents de match structurés
- [x] compteur de forfaits dans le classement
- [x] PGN
- [x] classement
- [x] rapport console
- [x] export PGN multi-parties
- [x] export CSV classement

---

## Phase 9 — Workflow étudiant

- [x] guide de démarrage
- [x] identité bot + auteur
- [x] template de Pull Request
- [x] CI
- [x] règles de contribution
- [x] tutoriel complet « créer son bot de tournoi »
- [x] validation automatique de la structure d'une soumission
- [x] validation exécutable en JVM isolée
- [x] découverte automatique des bots étudiants dans le catalogue
- [ ] gel du framework avant tournoi

---

## Phase 10 — Robustesse du tournoi

- [x] JVM séparée par bot
- [x] timeout dur par décision
- [x] protocole binaire minimal et borné
- [x] plafond mémoire de la JVM enfant (`-Xmx`)
- [x] canal loopback authentifié par jeton de session
- [x] stdout/stderr étudiant séparé du protocole
- [x] contrôle des dépendances de soumission (périmètre PR gelé)
- [x] garde-fous réseau/disque/processus/réflexion sur les soumissions
- [ ] sandbox OS réseau/disque pour environnement hostile (extension)
- [x] reproductibilité complète
- [x] rapport d'incident

---

## Extensions possibles

- [x] interface graphique Swing
- [x] replay pas à pas
- visualisation web
- tableau de tournoi en direct
- Elo interne
- [x] Minimax
- [x] alpha-beta
- compatibilité UCI
- bots externes via protocole
- tournoi inter-langages
