# Présentation Test

Support de code pour une présentation sur les tests.  
Consistes-en une API implémentée via KTor, utilisant une base de données MariaDB via KTORM.  
Les tests sont réalisés en utilisant JUnit, Mockk et AssertJ ainsi que le framework de test intégré à KTor.

## API

L'API permet la lecture et l'écriture dans une base de données des utilisateurs possédant un nom et un scope.

### Règle métier

La création, la mise à jour et la suppression des utilisateurs nécessite un scope au moins égal à la cible.  
Dans le cas d'une création, le scope de l'utilisateur souhaitant créer le nouvel utilisateur doit être au moins égal au scope de ce dernier.
Dans le cas d'une mise à jour le scope de l'appelant doit être au moins égal au scope de l'utilisateur cible et de la mise à jour voulue.
Dans le cas d'une suppression le scope de l'appellant doit être au moins égal au scope de l'utilisateur cible.

On ne peut pas se supprimer soi-même.

### Authentification

Pour s'authentifier, il est nécessaire d'adjoindre à la requête un bearer token contenant l'id de l'appelant.

## Test

### Unitaire

Les tests unitaires sont les plus simples et les plus courts des tests automatisés.

Il s'agit de tester uniquement des morceaux de code correspondant à **une seule** règle métier.

Afin de ne tester que le code métier et non les différents détails techniques, les dépendances présentes dans les services métier seront le plus possible mocker pour contrôler efficacement l'environnement dans lequel les tests unitaires s'exécutent.

## Intégration

Les tests d'intégration se diviseront en 2 parties.

La première dans laquelle nous allons tester uniquement le métier, mais dans un état variable (mais toujours contrôlé). On utilisera par exemple des InMemory afin de simuler la base de données.  
Ces tests ont pour but de mettre à l'épreuve l'évolution de l'état dans le service ainsi que la cohérence de cette évolution.

La seconde partie consiste à tester, tant que faire ce peut évidemment, les dépendances de l'application. Par exemple tester que le comportement attendu de la base de données soit bien le comportement réel. Ceci permet de s'assurer que les différentes dépendances ont un fonctionnement réel identique à celui attendu.
Il ne s'agit pas là de tester du métier ni même l'implémentation d'une dépendance, mais de tester que les interactions que nous demanderons à des dépendances satisferons nos attentes, et ce, de manière continue (en cas de mise à jour par exemple).

## End 2 End

Les tests end 2 end ont pour vocation à tester un scénario réel.

Ceci permettra de s'assurer que toutes les briques constituant l'application, que nous avons testé séparément dans les tests unitaires et les tests d'intégrations, fonctionne de concert comme cela est attendu. Le but ici est de tester en condition presque réelle le parcours exact que nous emprunterons en production.
