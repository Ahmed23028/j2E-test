# Commandes Utiles - Spring Boot + Flyway

## Compilation et Exécution

### Compiler le projet
```bash
mvn clean compile
```

### Exécuter l'application
```bash
mvn spring-boot:run
```

### Créer un JAR exécutable
```bash
mvn clean package
java -jar target/spring-boot-flyway-demo-1.0.0.jar
```

## Tests

### Lancer tous les tests
```bash
mvn test
```

### Lancer un test spécifique
```bash
mvn test -Dtest=FlywayMigrationTest
```

## Base de Données H2

### Accéder à la console H2
1. Démarrer l'application
2. Ouvrir : http://localhost:8080/h2-console
3. Paramètres de connexion :
   - **JDBC URL:** `jdbc:h2:mem:flywaydb`
   - **Username:** `sa`
   - **Password:** (laisser vide)

### Requêtes SQL Utiles

#### Voir toutes les migrations appliquées
```sql
SELECT * FROM flyway_schema_history 
ORDER BY installed_rank;
```

#### Voir la structure de la table books
```sql
SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'BOOKS';
```

#### Voir tous les livres
```sql
SELECT id, title, author, price, stock_quantity, rating 
FROM books;
```

#### Voir les catégories
```sql
SELECT * FROM categories;
```

#### Compter les migrations
```sql
SELECT COUNT(*) as total_migrations 
FROM flyway_schema_history;
```

## API REST - Exemples avec curl

### Livres

#### Lister tous les livres
```bash
curl http://localhost:8080/api/books
```

#### Obtenir un livre par ID
```bash
curl http://localhost:8080/api/books/1
```

#### Rechercher des livres
```bash
curl "http://localhost:8080/api/books/search?keyword=Spring"
```

#### Livres disponibles (en stock)
```bash
curl http://localhost:8080/api/books/available
```

#### Livres par catégorie
```bash
curl http://localhost:8080/api/books/category/1
```

#### Créer un nouveau livre
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Nouveau Livre",
    "author": "Auteur Test",
    "isbn": "978-1234567890",
    "price": 29.99,
    "stockQuantity": 15,
    "description": "Description du livre"
  }'
```

#### Mettre à jour un livre
```bash
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Titre Modifié",
    "author": "Auteur Modifié",
    "price": 39.99,
    "stockQuantity": 20
  }'
```

#### Supprimer un livre
```bash
curl -X DELETE http://localhost:8080/api/books/1
```

### Catégories

#### Lister toutes les catégories
```bash
curl http://localhost:8080/api/categories
```

#### Obtenir une catégorie par ID
```bash
curl http://localhost:8080/api/categories/1
```

#### Créer une nouvelle catégorie
```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Biographie",
    "description": "Livres biographiques"
  }'
```

### Informations Flyway

#### Voir l'état des migrations
```bash
curl http://localhost:8080/api/flyway/info
```

## Vérification des Migrations

### Voir les logs de migration au démarrage
Lors du démarrage, Flyway affiche dans les logs :
```
Flyway Community Edition 10.6.0
Database: jdbc:h2:mem:flywaydb (H2 2.x)
Successfully validated 6 migrations (execution time 00:00.012s)
Current version of schema "PUBLIC": 6
Schema "PUBLIC" is up to date. No migration necessary.
```

### Vérifier via l'API
```bash
curl http://localhost:8080/api/flyway/info | jq
```

## Créer une Nouvelle Migration

### Étape 1 : Créer le fichier
Créer un nouveau fichier dans `src/main/resources/db/migration/` :
```
V7__description_de_la_migration.sql
```

### Étape 2 : Écrire le SQL
```sql
-- Migration V7: Description
ALTER TABLE books ADD COLUMN new_column VARCHAR(100);
```

### Étape 3 : Redémarrer l'application
```bash
mvn spring-boot:run
```

Flyway détectera et exécutera automatiquement la nouvelle migration.

## Dépannage

### Réinitialiser la base de données (H2 en mémoire)
```bash
# Arrêter l'application
# Redémarrer (la base H2 en mémoire est recréée)
mvn spring-boot:run
```

### Vérifier les erreurs de migration
Consulter les logs au démarrage. Les erreurs apparaissent comme :
```
ERROR: Migration V7__add_column.sql failed
SQL State  : 42S21
Error Code : 42121
Message    : Column "column_name" already exists
```

### Nettoyer la base (ATTENTION : supprime tout)
Modifier `application.properties` :
```properties
spring.flyway.clean-disabled=false
```

Puis utiliser l'API Flyway ou redémarrer (non recommandé en production).

## PostgreSQL (Optionnel)

### Configuration pour PostgreSQL
Modifier `application.properties` :
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/flywaydb
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Créer la base de données
```bash
psql -U postgres
CREATE DATABASE flywaydb;
\q
```

## Maven - Commandes Utiles

### Nettoyer le projet
```bash
mvn clean
```

### Compiler sans tests
```bash
mvn clean compile -DskipTests
```

### Voir les dépendances
```bash
mvn dependency:tree
```

### Mettre à jour les dépendances
```bash
mvn versions:display-dependency-updates
```

## Vérification Rapide

### Checklist de démarrage
- [ ] Application démarre sans erreur
- [ ] Migrations appliquées (voir logs)
- [ ] Console H2 accessible
- [ ] API REST répond (`/api/books`)
- [ ] Table `flyway_schema_history` existe
- [ ] 6 migrations enregistrées

### Commandes de vérification rapide
```bash
# 1. Démarrer l'application
mvn spring-boot:run

# 2. Dans un autre terminal, tester l'API
curl http://localhost:8080/api/books

# 3. Vérifier Flyway
curl http://localhost:8080/api/flyway/info
```

## Formatage JSON (avec jq)

Si vous avez `jq` installé :
```bash
curl http://localhost:8080/api/books | jq
curl http://localhost:8080/api/flyway/info | jq
```

## Notes Importantes

⚠️ **Ne jamais modifier une migration existante** après qu'elle soit appliquée en production.

⚠️ **Toujours tester les migrations** sur un environnement de développement avant la production.

✅ **Créer une nouvelle migration** pour chaque changement de schéma.

✅ **Utiliser des noms descriptifs** pour les migrations.
