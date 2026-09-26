# Contribuer au Chess Framework

## Principe

Le dépôt doit servir à la fois de framework pédagogique et de support au tournoi final.

Les modifications du framework et les soumissions de bots sont donc traitées séparément.

---

## Soumettre un bot étudiant

Le workflow cible est une Pull Request.

Le tutoriel complet est disponible dans :

```text
docs/STUDENT_TOURNAMENT_BOT.md
```

### 1. Créer une branche

Exemple :

```bash
git switch -c bot/ada-lovelace
```

### 2. Ajouter le bot

Vous pouvez créer automatiquement le squelette et son test :

```bash
bash scripts/new-student-bot.sh AdaLovelaceBot "Ada Lovelace" "Analytical Engine"
```

Sous Windows PowerShell :

```powershell
powershell -ExecutionPolicy Bypass -File scripts/new-student-bot.ps1 AdaLovelaceBot "Ada Lovelace" "Analytical Engine"
```

Emplacement cible :

```text
chess-bots/
└── src/
    └── main/
        └── java/
            └── fr/
                └── astroware/
                    └── chess/
                        └── bots/
                            └── students/
                                └── AdaLovelaceBot.java
```

### 3. Respecter le contrat

Le bot doit :

- être placé dans `fr.astroware.chess.bots.students` ;
- étendre `ChessBot` ;
- être `public`, non abstrait et posséder un constructeur public sans argument ;
- avoir un nom unique, un auteur et une description ;
- retourner uniquement des coups via l'API prévue ;
- ne pas modifier le moteur, le SDK, le tournoi ou les `pom.xml` ;
- ne pas accéder au réseau ou au disque ;
- ne pas lancer de processus, utiliser la réflexion ou terminer la JVM ;
- ne pas inspecter le code ou l'état privé d'un adversaire ;
- respecter le budget de calcul ;
- conserver un code lisible.

Une Pull Request étudiante doit contenir **exactement un bot** et au moins un test sous le package `students`.

### 4. Tester

Avant une Pull Request :

```bash
mvn verify
bash scripts/chess.sh validate-students
```

Sous Windows, utilisez `scripts/chess.ps1` à la place de `scripts/chess.sh`.

Les situations ou actions personnalisées significatives doivent être accompagnées de tests.

Le validateur exécute également le bot en JVM isolée comme Blanc puis comme Noir.

### 5. Validation automatique de la Pull Request

Dès qu'un bot étudiant est détecté, la CI vérifie automatiquement :

- que seuls les répertoires `students` de production et de test sont modifiés ;
- qu'un test est présent ;
- qu'une seule classe étend `ChessBot` ;
- que le package est correct ;
- qu'aucune API réseau, fichier, processus ou réflexion interdite n'est utilisée ;
- que `mvn verify` passe ;
- que le bot peut réellement être instancié et jouer dans une JVM isolée.

Après merge, le catalogue découvre automatiquement le bot. Il n'est pas nécessaire de modifier `BotCatalog`.

### 6. Ouvrir une Pull Request

La description doit préciser :

- nom du bot ;
- auteur ou équipe ;
- stratégie générale ;
- ordre des règles principales ;
- situations personnalisées ;
- actions personnalisées ;
- limites connues.

---

## Revue

Une Pull Request de bot sera notamment relue sur :

- compilation ;
- respect du contrat ;
- lisibilité ;
- encapsulation ;
- qualité objet ;
- absence de contournement ;
- tests ;
- reproductibilité.

La force du bot n'est pas le seul critère de qualité du code.

---

## Modifications du framework

Une modification du framework doit rester compatible avec les principes suivants :

1. le code étudiant dépend d'une API stable ;
2. les positions visibles des bots sont en lecture seule ;
3. les règles fondamentales des échecs appartiennent au moteur ;
4. une tactique est ajoutée par extension, pas par ajout de conditions dans `ChessBot` ;
5. une dépendance externe ne doit pas fuiter dans l'API étudiant ;
6. les comportements complexes sont composés à partir de briques simples ;
7. toute correction du moteur est accompagnée d'un test de non-régression.

---

## Style de commits

Exemples :

```text
feat: add fork detection
feat: add greedy baseline bot
fix: handle en passant attack map
test: add castling regression cases
docs: document student bot workflow
refactor: extract position evaluator
```

---

## Pull Requests de tournoi

À l'approche du tournoi, une branche ou une convention dédiée pourra être imposée afin de geler le framework et de n'accepter que les nouvelles classes de bots.

L'objectif est que tous les concurrents soient compilés contre exactement la même version du framework.
