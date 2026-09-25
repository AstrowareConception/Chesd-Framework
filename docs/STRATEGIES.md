# Construire une vraie stratégie de bot

Le Chess Framework ne cherche pas seulement à faire jouer des coups légaux. Son objectif pédagogique est de permettre à chaque étudiant de construire une **personnalité échiquéenne cohérente**.

Trois mécanismes complémentaires sont disponibles :

1. l'ordre des règles ;
2. le profil stratégique ;
3. les plans multi-coups.

Ils ne remplissent pas le même rôle.

---

## 1. L'ordre des règles : ce qui est prioritaire

Le principe historique du framework reste :

```text
Situation -> Action
```

et les règles sont parcourues dans l'ordre.

Cela signifie que l'ordre constitue déjà une véritable stratégie.

Exemple prudent :

```text
1. éviter le mat
2. sortir d'échec
3. mettre le roi à l'abri
4. sauver une pièce attaquée
5. développer
6. prendre le centre
7. chercher une tactique
```

Exemple offensif :

```text
1. mater
2. sortir d'échec
3. exploiter une tactique
4. prendre le centre
5. développer activement
6. roquer
```

Les mêmes outils peuvent donc produire des bots très différents.

---

## 2. StrategyProfile : la personnalité générale

Le profil stratégique agit lorsque **plusieurs coups sont valables pour une même règle**.

Trois axes sont proposés dans la première version :

- agressivité ;
- sécurité ;
- tolérance au risque.

Chaque valeur va de 0 à 10.

Exemple :

```java
@Override
protected StrategyProfile strategyProfile() {
    return StrategyProfiles.defensive();
}
```

Profils fournis :

```java
StrategyProfiles.balanced();
StrategyProfiles.defensive();
StrategyProfiles.solid();
StrategyProfiles.aggressive();
StrategyProfiles.adventurous();
```

---

## 3. Un profil ne remplace pas la note d'un coup

Chaque `EvaluatedMove` possède d'abord un score de qualité général.

Exemple :

```text
Coup A : 7,5/10
Coup B : 7,2/10
```

Il possède aussi trois caractéristiques :

```text
agressivité
sécurité
risque
```

Le profil calcule ensuite une **préférence effective**.

Un bot défensif peut donc préférer :

```text
Coup A
score de base : 7,2
sécurité : 9
risque : 2
```

à :

```text
Coup B
score de base : 7,5
agressivité : 9
sécurité : 3
risque : 9
```

Le score général reste important : le style n'est pas censé transformer un très mauvais coup en excellent coup.

Il sert à départager des options raisonnablement proches.

---

## 4. Définir son propre profil

Un étudiant peut fabriquer sa propre personnalité :

```java
@Override
protected StrategyProfile strategyProfile() {
    return StrategyProfiles.profile(
        "Forteresse",
        2.0,  // agressivité
        10.0, // sécurité
        1.0   // tolérance au risque
    );
}
```

ou :

```java
@Override
protected StrategyProfile strategyProfile() {
    return StrategyProfiles.profile(
        "Pirate",
        10.0,
        2.0,
        10.0
    );
}
```

Deux bots utilisant exactement les mêmes règles peuvent ainsi faire des choix différents.

---

# 5. StrategicPlan : poursuivre un objectif

Une règle tactique répond souvent à un événement immédiat :

> « une pièce est pendue, je peux la prendre ».

Un plan stratégique répond à une autre question :

> « qu'est-ce que j'essaie d'obtenir dans les prochains coups ? »

Exemples :

- prendre le centre ;
- développer les pièces mineures ;
- mettre le roi à l'abri ;
- plus tard : créer un pion passé ;
- plus tard : attaquer le roi adverse ;
- plus tard : simplifier lorsque l'on a un avantage matériel.

---

## 6. Un plan n'est pas une suite figée

C'est un point essentiel.

Le plan :

```text
Mettre le roi à l'abri
```

ne signifie pas :

```text
1. Nf3
2. Be2
3. O-O
```

quoi qu'il arrive.

À chaque tour, le plan regarde de nouveau la position et les coups légaux.

Il peut proposer :

```text
Nf3
Be2
Bd3
g3
e3
e4
```

avec différentes notes.

Le profil du bot choisit ensuite une route.

Si le petit roque devient directement légal :

```text
O-O -> 9,5/10
```

le plan le privilégie immédiatement.

Cette approche est plus robuste qu'une séquence codée en dur.

---

## 7. Plans fournis actuellement

### Prendre le centre

```java
Plans.takeCenter()
```

La première version cherche à occuper les quatre cases centrales :

```text
d4 e4 d5 e5
```

Cette approximation sera enrichie lorsque l'AttackMap permettra de mesurer également le **contrôle** du centre.

### Développer les pièces mineures

```java
Plans.developMinorPieces()
```

Le plan cherche à faire quitter leurs cases initiales aux cavaliers et aux fous.

### Mettre le roi à l'abri

```java
Plans.castleKingside()
```

Le plan cherche le petit roque.

S'il n'est pas encore possible, il propose plusieurs coups susceptibles de le préparer.

---

## 8. Insérer un plan dans le comportement

Tout plan peut devenir une règle :

```java
Plans.takeCenter().asRule()
```

Exemple :

```java
@Override
protected List<Rule<?>> rules() {
    return List.of(
        Openings.londonSystem().asRule(),
        Plans.takeCenter().asRule(),
        Plans.developMinorPieces().asRule(),
        Plans.castleKingside().asRule(),
        rule(
            "Secours",
            Situations.always(),
            Actions.randomLegalMove()
        )
    );
}
```

L'ordre est intentionnel.

Ici le bot tente :

1. son ouverture ;
2. de prendre le centre ;
3. de développer ;
4. de roquer ;
5. puis seulement un coup de secours.

---

## 9. Changer l'ordre change la personnalité

Bot plutôt prudent :

```java
return List.of(
    opening.asRule(),
    Plans.castleKingside().asRule(),
    Plans.developMinorPieces().asRule(),
    Plans.takeCenter().asRule()
);
```

Bot plus dynamique :

```java
return List.of(
    opening.asRule(),
    Plans.takeCenter().asRule(),
    Plans.developMinorPieces().asRule(),
    Plans.castleKingside().asRule()
);
```

C'est une propriété importante du framework :

> la stratégie doit rester lisible dans le code de la classe du bot.

---

## 10. Mesurer l'avancement

Un plan expose également :

```java
PlanProgress progress(BotContext context);
```

La progression est notée de 0 à 10.

Exemple :

```text
Plan : mettre le roi à l'abri
Progression : 4/10
État : ACTIVE
Explication :
"Le roque n'est pas encore disponible : développement nécessaire."
```

États possibles :

- `ACTIVE`
- `COMPLETED`
- `BLOCKED`

Cette information sera utile plus tard pour les traces détaillées et les interfaces de visualisation.

---

## 11. Limite volontaire de la première version

Les plans actuels utilisent encore une analyse simple.

Par exemple, « prendre le centre » mesure essentiellement l'occupation des cases centrales.

Lorsque la couche `Analysis` sera développée, le même plan pourra intégrer :

- contrôle des cases ;
- nombre d'attaquants ;
- nombre de défenseurs ;
- sécurité de la pièce installée au centre ;
- conséquences après le coup.

L'API du bot n'aura pas besoin de changer.

C'est précisément l'intérêt de séparer le **plan** de l'implémentation de l'analyse.


---

## 12. Adapter le profil à la phase

Le framework distingue maintenant :

```java
GamePhase.OPENING
GamePhase.MIDDLEGAME
GamePhase.ENDGAME
```

La phase est estimée à partir du numéro de coup et du matériel restant.

Un bot peut adapter son tempérament :

```java
@Override
protected StrategyProfile strategyProfile(BotContext context) {
    return switch (context.analysis().gamePhase()) {
        case OPENING -> StrategyProfiles.solid();
        case MIDDLEGAME -> StrategyProfiles.aggressive();
        case ENDGAME -> StrategyProfiles.defensive();
    };
}
```

C'est le principe utilisé par `ChameleonBot`.

---

## 13. Limiter une règle à une phase

Une situation peut être gardée :

```java
Situations.onlyInPhase(
    GamePhase.MIDDLEGAME,
    Situations.forkOpportunity()
)
```

ou directement :

```java
Situations.forkOpportunity()
    .when(context ->
        context.analysis().gamePhase()
            == GamePhase.MIDDLEGAME
    );
```

L'intérêt est de conserver le type précis de la détection : une `ForkDetection` reste une `ForkDetection`.

Cela permet d'écrire des comportements comme :

- ouvrir solidement ;
- rechercher les complications tactiques au milieu de jeu ;
- simplifier et réduire le risque en finale.


---

## 14. Plans positionnels

Trois nouveaux plans utilisent directement les métriques d'analyse :

```java
Plans.improveKingSafety();
Plans.useOpenFile();
Plans.createPassedPawn();
```

Ils peuvent être conditionnés par phase :

```java
Plans.createPassedPawn()
    .asRule(
        Situations.inPhase(GamePhase.ENDGAME)
    );
```

### Créer un pion passé

Le plan simule les coups et conserve ceux qui augmentent réellement le nombre de pions passés ou de pions passés protégés.

### Occuper une colonne ouverte

Le plan cherche les déplacements de tour vers une colonne ouverte ou semi-ouverte adaptée à sa couleur.

### Améliorer la sécurité du roi

Le plan conserve les coups qui améliorent effectivement la note de sécurité du roi.

---

## 15. Stratégie purement positionnelle

Le framework fournit aussi :

```java
Actions.bestPosition();
```

Cette action simule tous les coups légaux puis transforme la note de `PositionEvaluation` en `EvaluatedMove`.

C'est le principe utilisé par `PositionalBot`.


---

## 16. Regarder une réponse adverse

Deux actions positionnelles sont maintenant disponibles.

### Profondeur 1

```java
Actions.bestPosition();
```

Chaque coup est évalué immédiatement.

### Profondeur 2

```java
Actions.bestPositionAfterBestReply();
```

Chaque coup est évalué après la meilleure réponse adverse.

Une variante permet de contrôler le coût :

```java
Actions.bestPositionAfterBestReply(8);
```

Ici, les 8 meilleurs coups selon l'évaluation immédiate sont conservés avant la recherche des réponses adverses.

Ce compromis est volontairement explicite :

```text
plus de candidats
= meilleure couverture
= plus de calcul
```

Le bot de référence `LookaheadBot` utilise une shortlist de 8 coups.


---

## 17. Minimax et alpha-bêta

Pour aller au-delà d'une seule réponse adverse :

```java
Actions.minimax(
    SearchSettings.bounded(3, 6)
);
```

La profondeur est exprimée en demi-coups.

```text
profondeur 1 : mon coup
profondeur 2 : mon coup + réponse adverse
profondeur 3 : mon coup + réponse adverse + ma réplique
```

Une recherche exhaustive peut être demandée :

```java
SearchSettings.exact(3);
```

mais son coût augmente très rapidement.

Le réglage borné :

```java
SearchSettings.bounded(3, 6);
```

limite à six coups par nœud après move ordering.

L'alpha-bêta est activé par défaut et peut être désactivé pour comparer :

```java
settings.withoutAlphaBeta();
```

Les traces affichent la variante principale, les nœuds visités et les coupures.
