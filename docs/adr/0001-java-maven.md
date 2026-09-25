# ADR 0001 — Java 25 et Maven

- **Statut :** accepté
- **Date :** 2026-09-25

## Contexte

Chess Framework est un framework pédagogique destiné à des étudiants qui doivent construire des bots d'échecs puis les confronter dans un tournoi.

Le choix du langage doit donc répondre à deux objectifs distincts :

1. permettre de développer le projet ;
2. rendre visibles et manipulables les concepts de conception logicielle étudiés en cours.

Python aurait permis un prototypage rapide et possède un écosystème échiquéen très riche.

Cependant le projet vise explicitement l'apprentissage de :

- la programmation orientée objet ;
- l'héritage ;
- les interfaces ;
- le polymorphisme ;
- l'encapsulation ;
- les génériques ;
- la composition ;
- les design patterns ;
- les tests ;
- la compilation et la gestion explicite des dépendances.

## Décision

Le framework sera développé en **Java 25 LTS** avec **Maven**.

Le projet sera organisé comme un build Maven multi-module.

## Raisons

### Typage statique

Les contrats entre :

```text
Situation<D>
Action<D>
Rule<D>
```

peuvent être vérifiés à la compilation.

### POO explicite

Java rend visibles dans le code plusieurs notions pédagogiques importantes :

- `abstract class` ;
- `interface` ;
- `extends` ;
- `implements` ;
- visibilité `private/protected/public`.

### Écosystème pédagogique

Les étudiants pourront pratiquer dans un même projet :

- Maven ;
- JUnit ;
- GitHub Actions ;
- Pull Requests ;
- documentation Javadoc.

### Durabilité

Java 25 est une version LTS et convient à un support de cours conservé sur plusieurs promotions.

## Conséquences

### Positives

- modèle objet fortement structuré ;
- erreurs de type détectées tôt ;
- excellente intégration IDE ;
- concepts POO directement visibles ;
- projet adapté à une évaluation de conception.

### Négatives

- davantage de code structurel qu'en Python ;
- courbe d'apprentissage plus importante ;
- prototype initial plus long.

Ces coûts sont acceptés car ils participent directement aux objectifs pédagogiques.

## Alternative Python

Python reste une piste d'extension future.

Une architecture de tournoi par processus séparés pourrait permettre un jour d'accepter des bots écrits dans plusieurs langages via un protocole commun.

Ce n'est pas un objectif de la V1.
