# Checklist V1 — prêt pour les étudiants

Cette checklist définit ce que signifie **« Chess Framework est prêt à être remis à une promotion »**.

Elle est volontairement distincte de la roadmap : les extensions futures — UCI, interface web, Elo, tournoi inter-langages — ne bloquent pas la V1 pédagogique.

---

## 1. Distribution et environnement

- [x] dépôt GitHub public et clonable ;
- [x] build Maven multi-module ;
- [x] Java 25 documenté ;
- [x] Maven et Git documentés comme prérequis ;
- [x] `.editorconfig` multi-OS ;
- [x] runner de tournoi autonome produit par `mvn verify` ;
- [x] lanceur Bash indépendant de la version du JAR ;
- [x] lanceur PowerShell indépendant de la version du JAR.

---

## 2. Démarrage étudiant

- [x] Quick start dans le README ;
- [x] guide `GETTING_STARTED.md` ;
- [x] tutoriel complet `STUDENT_TOURNAMENT_BOT.md` ;
- [x] générateur Bash de classe de bot + test ;
- [x] générateur PowerShell de classe de bot + test ;
- [x] package étudiant unique documenté ;
- [x] métadonnées nom / auteur / description ;
- [x] exemple de bot minimal immédiatement compilable ;
- [x] guide de dépannage.

---

## 3. SDK pédagogique

- [x] `ChessBot` comme classe de base ;
- [x] `Situation`, `Detection`, `Action`, `Rule` ;
- [x] décisions évaluées de 0 à 10 ;
- [x] explications de décision ;
- [x] fallback légal ;
- [x] analyse de position centralisée ;
- [x] projection d'un coup ;
- [x] profils stratégiques ;
- [x] ouvertures ;
- [x] plans multi-coups ;
- [x] recherche adversariale ;
- [x] Minimax / alpha-bêta ;
- [x] catalogue de situations V1 ;
- [x] tactiques avancées documentées.

---

## 4. Règles d'échecs

- [x] génération des coups légaux ;
- [x] FEN ;
- [x] SAN ;
- [x] PGN ;
- [x] échec et mat ;
- [x] pat ;
- [x] roque ;
- [x] prise en passant ;
- [x] quatre promotions ;
- [x] répétition triple ;
- [x] règle des cinquante coups ;
- [x] matériel insuffisant ;
- [x] perft 20 / 400 / 8902 sur la position initiale.

---

## 5. Test local d'un bot

- [x] `mvn verify` compile le bot et son test ;
- [x] `validate-students` vérifie réellement la classe compilée ;
- [x] validation du constructeur ;
- [x] validation des métadonnées ;
- [x] contrôle d'unicité ;
- [x] smoke-test avec les Blancs ;
- [x] smoke-test avec les Noirs ;
- [x] découverte automatique dans `list` ;
- [x] clé CLI `student-*` générée automatiquement ;
- [x] duel console ;
- [x] export PGN ;
- [x] viewer Swing ;
- [x] tournoi local.

---

## 6. Tournoi

- [x] round-robin ;
- [x] alternance des couleurs ;
- [x] scoring 1 / 0,5 / 0 ;
- [x] seed ;
- [x] limite de demi-coups ;
- [x] classement déterministe ;
- [x] temps de décision mesuré mais non utilisé comme départage ;
- [x] export PGN multi-parties ;
- [x] export CSV ;
- [x] rapport console ;
- [x] forfait structuré ;
- [x] incidents exploitables.

---

## 7. Isolation et robustesse

- [x] JVM enfant distincte par bot ;
- [x] JVM fraîche à chaque partie ;
- [x] timeout dur par décision ;
- [x] timeout de démarrage ;
- [x] plafond mémoire `-Xmx` ;
- [x] destruction forcée du processus bloqué ;
- [x] protocole binaire minimal ;
- [x] limites de taille avant allocation ;
- [x] canal loopback avec jeton de session ;
- [x] stdout/stderr étudiant séparé du protocole ;
- [x] le tournoi continue après un forfait ;
- [x] test avec bot en boucle infinie ;
- [x] reproductibilité testée en local et en JVM isolée.

---

## 8. Pull Requests étudiantes

- [x] template de Pull Request ;
- [x] règles de contribution ;
- [x] exactement un `ChessBot` par PR de bot ;
- [x] au moins un test obligatoire ;
- [x] package imposé ;
- [x] framework/POM/workflows interdits dans une PR de bot ;
- [x] ajout de dépendances interdit par le périmètre Git ;
- [x] réseau, disque, processus et réflexion bloqués par garde-fous statiques ;
- [x] build Maven en CI ;
- [x] validation exécutable du bot en CI.

---

## 9. Parcours automatisé de non-régression

La CI doit reproduire le parcours d'un étudiant et rester verte.

### Linux

- [x] générer un bot ;
- [x] compiler et tester ;
- [x] construire le runner ;
- [x] valider le bot ;
- [x] vérifier son apparition dans le catalogue ;
- [x] jouer un duel isolé ;
- [x] lancer un mini-tournoi isolé ;
- [x] vérifier les exports PGN/CSV.

### Windows

- [x] générer un bot PowerShell ;
- [x] compiler et tester ;
- [x] construire le runner ;
- [x] valider le bot ;
- [x] vérifier le catalogue dans le job Windows ;
- [x] jouer le duel isolé dans le job Windows.

---

## 10. Documentation

- [x] README / quick start ;
- [x] architecture ;
- [x] analyse de position ;
- [x] évaluation ;
- [x] stratégies ;
- [x] ouvertures ;
- [x] bots de référence ;
- [x] interfaces ;
- [x] tournoi ;
- [x] contribution ;
- [x] workflow étudiant complet ;
- [x] dépannage ;
- [x] roadmap ;
- [x] procédure de gel du framework.

---

## 11. Hors périmètre bloquant de la V1

Les éléments suivants sont des extensions. Leur absence ne bloque pas la remise du framework :

- interface web ;
- classement Elo ;
- protocole UCI ;
- bots externes ;
- tournoi inter-langages ;
- sandbox OS complète contre du code volontairement hostile ;
- optimisation d'un immense arbre d'ouvertures.

---

## 12. Critère de remise

La V1 est techniquement prête à être remise lorsque :

1. le HEAD de `main` a une CI verte ;
2. `Build and test` est vert ;
3. le smoke-test étudiant Linux est vert ;
4. le smoke-test étudiant Windows est vert ;
5. aucune modification fonctionnelle du SDK n'est encore prévue avant la promotion.

Le gel effectif est ensuite appliqué selon `docs/FRAMEWORK_FREEZE.md`.
