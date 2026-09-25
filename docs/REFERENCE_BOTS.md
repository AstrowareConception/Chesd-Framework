# Bots de référence et bots de test

Les bots fournis servent à la fois d'adversaires et d'exemples pédagogiques contrastés.

## RandomBot

Choisit un coup légal au hasard. C'est le bot minimal permettant de comprendre comment étendre `ChessBot`.

## GreedyBot

Priorité : capturer la pièce adverse de plus forte valeur.

```text
pion = 1
tour = 5
dame = 9
```

Il peut donc prendre une dame même si l'échange est mauvais ensuite. Ce défaut est volontaire.

## CautiousBot

Profil :

```java
StrategyProfiles.defensive();
```

Priorités :

1. prendre une pièce pendue ;
2. évaluer les autres captures avec une pénalité de risque ;
3. préparer le petit roque ;
4. développer ;
5. prendre le centre ;
6. fallback.

Une tour gratuite peut donc être préférée à une dame fortement défendue.

## BerserkerBot

Profil :

```java
StrategyProfiles.adventurous();
```

Il privilégie les prises agressives, le centre et l'initiative avant la sécurité du roi.

Il constitue l'opposé pédagogique de `CautiousBot`.

## SolidPlannerBot — The Architect

Bot d'exemple combinant plusieurs couches :

- Système de Londres avec les Blancs ;
- Défense Scandinave avec les Noirs ;
- petit roque ;
- développement ;
- centre ;
- fallback.

## Comparaison

| Bot | Matériel | Sécurité | Risque | Plans | Ouverture |
|---|---|---|---|---|---|
| RandomBot | aucun raisonnement | aucun | aléatoire | non | non |
| GreedyBot | très important | faible | peu considéré | non | non |
| CautiousBot | important | forte | faible | oui | non |
| BerserkerBot | important | secondaire | élevé | oui | non |
| SolidPlannerBot | secondaire pour l'instant | forte | faible à moyen | oui | Londres / Scandinave |

## Prochaine cible

`TacticalBot` exploitera ensuite :

- pièce pendue ;
- fourchette ;
- clouage ;
- enfilade ;
- menace de mat.

Une activité pédagogique utile consiste à donner la même position à plusieurs bots puis comparer la règle déclenchée, les candidats, leurs scores et le coup final.