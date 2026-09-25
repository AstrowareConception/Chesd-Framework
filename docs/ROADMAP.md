# Roadmap

## Phase 0 — Cadrage et choix techniques

- [x] Vision du projet
- [x] Spécifications fonctionnelles
- [x] Architecture cible
- [ ] ADR sur le choix Java/Maven
- [ ] spike comparatif des bibliothèques de règles d'échecs
- [ ] batterie de positions FEN et tests perft de référence
- [ ] choix du backend de règles

**Livrable :** architecture figée suffisamment pour démarrer le code sans coupler l'API étudiante au moteur choisi.

---

## Phase 1 — Squelette Maven multi-module

Créer :

- `chess-core`
- `chess-bot-sdk`
- `chess-bots`
- `chess-tournament`

Ajouter :

- compilation Java 25 ;
- JUnit ;
- JaCoCo ;
- Checkstyle ou équivalent léger ;
- GitHub Actions ;
- règles de compilation communes.

**Critère d'acceptation :** `mvn verify` passe sur une machine vierge.

---

## Phase 2 — Modèle de domaine et moteur

Implémenter :

- `Color`
- `PieceType`
- `Piece`
- `Square`
- `Move`
- `PlacedPiece`
- `PositionView`
- `GameState`
- `GameResult`
- `ChessRulesEngine`

Puis l'adapter vers le backend sélectionné.

Ajouter les tests :

- mouvements légaux ;
- roque ;
- promotion ;
- prise en passant ;
- échec ;
- mat ;
- pat ;
- répétitions ;
- matériel insuffisant ;
- perft.

**Critère d'acceptation :** le framework peut jouer une partie complète sans bot.

---

## Phase 3 — API ChessBot

Implémenter :

- `ChessBot`
- `BotContext`
- `Detection`
- `Situation<D>`
- `Action<D>`
- `Rule<D>`
- `BotDecision`
- politique de fallback.

Ajouter la trace de décision.

**Critère d'acceptation :** un bot de moins de 30 lignes peut jouer une partie complète.

---

## Phase 4 — Analyse de position

Implémenter en priorité :

- `AttackMap`
- attaquants ;
- défenseurs ;
- matériel ;
- captures ;
- projection après un coup ;
- sécurité simple d'une pièce ;
- mobilité ;
- contrôle du centre.

**Critère d'acceptation :** les détecteurs tactiques n'ont pas à recalculer chacun les mêmes informations élémentaires.

---

## Phase 5 — Premier catalogue de situations

Lot V1 :

1. toujours vraie ;
2. en échec ;
3. mat en un ;
4. capture disponible ;
5. pièce adverse pendue ;
6. pièce alliée pendue ;
7. pièce attaquée ;
8. défense insuffisante ;
9. promotion disponible ;
10. roque disponible ;
11. fourchette disponible ;
12. clouage ;
13. enfilade ;
14. développement possible ;
15. contrôle du centre.

Chaque situation reçoit des tests positifs et négatifs.

---

## Phase 6 — Premier catalogue d'actions

Lot V1 :

1. coup détecté ;
2. coup légal aléatoire ;
3. meilleure capture matérielle ;
4. sortie d'échec ;
5. mat ;
6. sauver la pièce la plus chère ;
7. meilleure fourchette ;
8. promotion ;
9. roque ;
10. développement ;
11. meilleur coup selon un évaluateur.

---

## Phase 7 — Bots de référence

### RandomBot

Objectif : adversaire minimal et test de robustesse.

### GreedyBot

Objectif : montrer une stratégie lisible fondée sur le matériel.

### TacticalBot

Objectif : démontrer l'intérêt de l'ordre des règles et de la composition des situations.

**Critère d'acceptation :** les trois bots ont des comportements clairement distincts et leurs décisions peuvent être expliquées par la trace.

---

## Phase 8 — TournamentRunner

Implémenter :

- match bot contre bot ;
- alternance des couleurs ;
- toutes rondes ;
- score ;
- seed ;
- temps par coup ;
- capture des erreurs ;
- export PGN ;
- classement ;
- rapport console.

---

## Phase 9 — Workflow étudiant

Ajouter :

- tutoriel « Créer son premier bot » ;
- emplacement des bots étudiants ;
- template de Pull Request ;
- tests automatiques d'une soumission ;
- règles de contribution ;
- exemple de PR ;
- checklist de rendu.

La CI doit refuser au minimum :

- code ne compilant pas ;
- tests cassés ;
- bot ne respectant pas le contrat ;
- modification interdite du moteur dans une PR de soumission.

---

## Phase 10 — Catalogue tactique avancé

Ajouter progressivement :

- attaque à la découverte ;
- double échec ;
- surcharge ;
- déviation ;
- attraction ;
- élimination du défenseur ;
- batterie ;
- rayon X ;
- pion passé ;
- pion isolé ;
- pion doublé ;
- roi exposé ;
- échanges favorables ;
- structure de pions ;
- espace ;
- initiative simple.

---

## Phase 11 — Robustesse du tournoi

- exécution des bots dans des JVM séparées ;
- timeout dur ;
- protocole de communication minimal ;
- limitation des dépendances ;
- contrôle de l'environnement ;
- reproductibilité complète ;
- rapport d'incident par bot.

---

## Phase 12 — Extensions possibles

Non prioritaires :

- interface graphique ;
- visualisation web ;
- replay pas à pas ;
- tableau de tournoi en direct ;
- Elo interne ;
- Minimax fourni comme extension ;
- alpha-beta ;
- bibliothèque d'ouvertures ;
- compatibilité UCI ;
- bots Python via protocole externe ;
- tournoi inter-langages.
