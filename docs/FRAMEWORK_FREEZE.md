# Gel du framework avant le tournoi

Le gel sépare deux périodes :

1. **construction du framework** ;
2. **développement concurrentiel des bots étudiants**.

Une fois le gel annoncé, tous les étudiants doivent travailler sur exactement le même contrat technique.

---

## 1. Quand geler

Ne gelez pas le framework tant que :

- la CI du HEAD n'est pas entièrement verte ;
- le workflow étudiant Linux n'est pas vert ;
- le workflow étudiant Windows n'est pas vert ;
- la documentation V1 n'est pas à jour ;
- une modification cassante du SDK est encore prévue.

La checklist de référence est `docs/STUDENT_READY_CHECKLIST.md`.

---

## 2. Créer le point de référence

Au moment du gel, créez un tag explicite, par exemple :

```bash
git switch main
git pull
git tag -a student-v1 -m "Framework gelé pour le tournoi étudiant"
git push origin student-v1
```

Pour une nouvelle promotion ou une évolution majeure : `student-v2`, `student-v3`, etc.

Le tag permet de retrouver sans ambiguïté la version de référence utilisée pendant le tournoi.

---

## 3. Règle après le gel

Après le gel, une Pull Request de bot ne doit modifier que :

```text
chess-bots/src/main/java/fr/astroware/chess/bots/students/
chess-bots/src/test/java/fr/astroware/chess/bots/students/
```

Cette règle est déjà contrôlée automatiquement par `scripts/validate-student-submission.sh`.

Une modification de `chess-core`, `chess-bot-sdk`, `chess-tournament`, des bots de référence, des POM ou de la CI doit être traitée séparément et ne doit pas être mélangée à une soumission de tournoi.

---

## 4. Correctif framework exceptionnel

Si un bug du framework est découvert après le gel :

1. reproduire le bug par un test ;
2. corriger uniquement le framework ;
3. faire passer la CI complète ;
4. vérifier l'absence d'avantage spécifique à un bot ;
5. documenter le changement ;
6. publier un nouveau tag de référence.

Exemple : `student-v1.1`.

Tous les étudiants doivent alors repartir de la même version.

---

## 5. Paramètres officiels du tournoi

Avant le tournoi final, publier les paramètres qui seront réellement utilisés :

- seed ou règle de génération des seeds ;
- nombre de parties par paire ;
- limite de demi-coups ;
- timeout par décision ;
- timeout de démarrage ;
- mémoire maximale par JVM enfant ;
- mode `--isolated` obligatoire ;
- règle de classement.

Exemple :

```text
--games=4
--max-plies=400
--isolated
--timeout-ms=2000
--startup-timeout-ms=5000
--heap-mb=256
```

Ne changez pas ces paramètres au milieu du tournoi.

---

## 6. Reproductibilité

Le classement officiel n'utilise pas le temps de calcul comme critère de départage.

Le temps reste une métrique informative.

À seed et configuration identiques, le framework teste automatiquement la reproductibilité des parties locales, des parties isolées et du classement stable.

---

## 7. Protection GitHub recommandée

Pour la période de tournoi, la branche `main` devrait être protégée avec au minimum :

- Pull Request obligatoire ;
- CI obligatoire ;
- interdiction de push direct étudiant ;
- pas de force-push ;
- revue enseignant si souhaité.

La configuration exacte dépend de l'organisation du cours et reste une décision administrative.

---

## 8. Test de répétition générale

Avant d'ouvrir officiellement le dépôt aux étudiants :

1. partir d'un clone propre ;
2. utiliser le générateur de bot ;
3. lancer `mvn verify` ;
4. lancer `validate-students` ;
5. vérifier `list` ;
6. jouer un duel isolé ;
7. lancer un mini-tournoi avec exports ;
8. ouvrir une Pull Request test ;
9. vérifier les contrôles CI.

Les étapes 1 à 7 sont reproduites automatiquement dans GitHub Actions sur Linux, et le parcours Windows est également testé.

---

## 9. Début officiel

Lorsque la checklist V1 est entièrement verte et que le tag est créé, communiquez aux étudiants :

- l'URL du dépôt ;
- le tag de référence ;
- les prérequis ;
- le tutoriel `docs/STUDENT_TOURNAMENT_BOT.md` ;
- les paramètres officiels ;
- la date limite de Pull Request.

À partir de ce moment, le framework est considéré comme gelé pour la promotion concernée.
