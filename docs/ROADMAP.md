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

La priorité suivante est la **couche d'analyse de position**, indispensable pour rendre les situations tactiques réellement puissantes.

---

## Phase 0 — Cadrage

- [x] Vision du projet
- [x] Spécifications fonctionnelles
- [x] Architecture cible
- [x] ADR Java 25 / Maven
- [x] ADR profils / plans / ouvertures
- [ ] spike comparatif des bibliothèques de règles d'échecs
- [ ] batterie de positions FEN et tests perft
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
- [ ] PGN
- [ ] perft
- [ ] roque
- [ ] en passant
- [ ] promotion
- [ ] répétitions
- [ ] règle des cinquante coups
- [ ] matériel insuffisant

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
- [ ] plan de création d'un pion passé
- [ ] plan d'attaque sur une aile
- [ ] davantage d'ouvertures
- [ ] arbre d'ouvertures optimisé si le catalogue devient volumineux

---

## Phase 4 — Analyse de position — PROCHAINE PRIORITÉ

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
- [ ] mobilité
- [ ] contrôle du centre
- [x] projection après un coup
- [x] sécurité simple d'une pièce
- [x] détection de pièce pendue

**Critère :** une situation tactique ne doit pas recalculer elle-même les informations élémentaires du plateau.

---

## Phase 5 — Catalogue de situations

Lot V1 :

1. toujours vraie — [x]
2. en échec — [ ]
3. mat en un — [x]
4. capture disponible — [ ]
5. pièce adverse pendue — [ ]
6. pièce alliée pendue — [ ]
7. pièce attaquée — [ ]
8. défense insuffisante — [ ]
9. promotion disponible — [ ]
10. roque disponible — [ ]
11. fourchette — [x]
12. clouage — [ ]
13. enfilade — [ ]
14. développement possible — [ ]
15. contrôle du centre — [ ]

Puis :

- attaque à la découverte
- double échec
- surcharge
- déviation
- attraction
- élimination du défenseur
- batterie
- rayon X
- pion passé
- pion isolé
- pion doublé
- roi exposé

---

## Phase 6 — Catalogue d'actions

- [x] coup légal aléatoire
- [ ] jouer le coup détecté
- [x] meilleure capture matérielle
- [ ] sortie d'échec
- [ ] mat
- [ ] sauver la pièce la plus chère
- [x] meilleure fourchette
- [ ] promotion
- [ ] roque
- [ ] meilleur coup selon un évaluateur
- [ ] minimisation du risque
- [ ] maximisation de l'activité

---

## Phase 7 — Bots de référence

- [x] `RandomBot`
- [x] `GreedyBot`
- [x] `CautiousBot`
- [x] `BerserkerBot`
- [x] `GuardianBot`
- [x] `TacticalBot`
- [x] `SolidPlannerBot` comme exemple pédagogique supplémentaire

---

## Phase 8 — Moteur de partie et tournoi

- [x] match bot contre bot
- [ ] alternance des couleurs
- [ ] toutes rondes
- [ ] scoring
- [x] seed
- [ ] temps par coup
- [ ] capture des erreurs
- [ ] PGN
- [ ] classement
- [ ] rapport console

---

## Phase 9 — Workflow étudiant

- [x] guide de démarrage
- [x] identité bot + auteur
- [x] template de Pull Request
- [x] CI
- [x] règles de contribution
- [ ] tutoriel complet « créer son bot de tournoi »
- [ ] validation automatique de la structure d'une soumission
- [ ] gel du framework avant tournoi

---

## Phase 10 — Robustesse du tournoi

- [ ] JVM séparée par bot
- [ ] timeout dur
- [ ] protocole minimal
- [ ] contrôle des dépendances
- [ ] limitation réseau/disque
- [ ] reproductibilité complète
- [ ] rapport d'incident

---

## Extensions possibles

- interface graphique
- replay pas à pas
- visualisation web
- tableau de tournoi en direct
- Elo interne
- Minimax
- alpha-beta
- compatibilité UCI
- bots externes via protocole
- tournoi inter-langages
