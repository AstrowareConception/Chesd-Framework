# ADR 0002 — Séparer profils, plans et ouvertures

- **Statut :** accepté
- **Date :** 2026-09-25

## Contexte

Le modèle initial du framework repose sur des règles ordonnées :

```text
Situation -> Action
```

Ce modèle est excellent pour les tactiques locales, mais il doit également permettre :

- des personnalités défensives ou offensives ;
- une acceptation variable du risque ;
- des objectifs poursuivis sur plusieurs coups ;
- des ouvertures théoriques ;
- un retour naturel au comportement normal lorsque l'ouverture échoue.

Fusionner tous ces concepts dans une seule classe `Strategy` rendrait l'API ambiguë et difficile à enseigner.

## Décision

Trois abstractions distinctes sont retenues.

### StrategyProfile

Décrit les préférences générales du bot :

- agressivité ;
- sécurité ;
- tolérance au risque.

Il influence le classement de plusieurs coups candidats.

### StrategicPlan

Décrit un objectif durable et adaptatif.

Exemples :

- prendre le centre ;
- développer ;
- roquer.

Le plan réévalue la position à chaque tour.

Il ne mémorise pas obligatoirement une séquence exacte.

### OpeningBook

Décrit une connaissance théorique fondée sur l'historique précis des coups.

Tant qu'une ligne correspond à la partie et que le prochain coup reste légal, le livre propose ce coup.

Lorsque la partie sort du livre, l'ouverture ne propose plus rien et la chaîne de règles continue.

## Conséquences positives

- concepts pédagogiques clairement distincts ;
- ouverture extensible sans toucher au moteur ;
- plans adaptatifs aux réponses adverses ;
- personnalité réutilisable sur plusieurs règles ;
- maintien du modèle simple de règles ordonnées ;
- bascule automatique de l'ouverture vers le jeu normal.

## Limites

- le profil n'altère pour l'instant que le choix entre candidats d'une même règle ;
- les plans V1 utilisent une analyse positionnelle encore simple ;
- le livre d'ouvertures initial est volontairement petit.

Ces limites pourront être enrichies sans remettre en cause les trois abstractions.
