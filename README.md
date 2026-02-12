# Spring Boot + Flyway - Gestion de Migrations de Base de Données

## 📋 Description du Projet

Ce projet démontre l'utilisation de **Flyway** avec **Spring Boot** pour gérer les migrations de schémas de base de données de manière versionnée et automatisée.

### Thème
**Gestion de version et migration de schémas de base de données**

### Objectifs d'apprentissage
- Comprendre le problème des divergences de schémas
- Créer des scripts de migration Flyway (V1__init.sql, V2__add_table.sql, etc.)
- Automatiser les migrations au démarrage
- Suivre l'historique des migrations

---

## 🚀 Technologies Utilisées

- **Spring Boot 3.2.0**
- **Flyway 10.6.0** - Gestion de migrations de base de données
- **Spring Data JPA** - Accès aux données
- **H2 Database** - Base de données en mémoire (développement)
- **PostgreSQL** - Support pour production (optionnel)
- **Lombok** - Réduction du code boilerplate
- **Maven** - Gestion des dépendances

---

## 📁 Structure du Projet

```
spring-boot-flyway-demo/
├── src/
│   ├── main/
│   │   ├── java/com/example/flywaydemo/
│   │   │   ├── entity/          # Entités JPA
│   │   │   ├── repository/      # Repositories Spring Data
│   │   │   ├── service/         # Services métier
│   │   │   ├── controller/      # Contrôleurs REST
│   │   │   └── FlywayDemoApplication.java
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/   # Scripts de migration Flyway
│   │       │       ├── V1__init.sql
│   │       │       ├── V2__add_books_table.sql
│   │       │       ├── V3__add_author_column.sql
│   │       │       ├── V4__add_indexes.sql
│   │       │       ├── V5__add_audit_columns.sql
│   │       │       └── V6__add_borrowings_table.sql
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

---

## 🔧 Configuration

### 1. Dépendances Maven

Les dépendances principales dans `pom.xml` :

```xml
<!-- Spring Boot Starter Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Flyway Core -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

### 2. Configuration application.properties

```properties
# Configuration Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.sql-migration-prefix=V
spring.flyway.sql-migration-separator=__
spring.flyway.sql-migration-suffixes=.sql
spring.flyway.encoding=UTF-8
spring.flyway.baseline-on-migrate=true
spring.flyway.validate-on-migrate=true

# Configuration JPA
spring.jpa.hibernate.ddl-auto=validate
```

**Important** : `spring.jpa.hibernate.ddl-auto=validate` empêche Hibernate de créer/modifier automatiquement les tables. Flyway gère entièrement le schéma.

---

## 📝 Migrations Flyway

### Convention de Nommage

Les fichiers de migration doivent suivre le format :
```
V{version}__{description}.sql
```

Exemples :
- `V1__init.sql`
- `V2__add_books_table.sql`
- `V3__add_author_column.sql`

### Historique des Migrations

#### V1__init.sql
Création des tables initiales :
- `users` - Table des utilisateurs
- `categories` - Table des catégories de livres
- Insertion de données initiales

#### V2__add_books_table.sql
Ajout de la table principale :
- `books` - Table des livres avec relations
- Insertion de livres d'exemple

#### V3__add_author_column.sql
Modification de schéma :
- Ajout de la colonne `author` à la table `books`
- Mise à jour des données existantes
- Ajout de contrainte NOT NULL

#### V4__add_indexes.sql
Optimisation des performances :
- Création d'index sur les colonnes fréquemment utilisées
- Index simples et composites

#### V5__add_audit_columns.sql
Ajout de fonctionnalités d'audit :
- Colonnes `created_at` et `updated_at` sur toutes les tables
- Mise à jour des enregistrements existants

#### V6__add_borrowings_table.sql
Nouvelle fonctionnalité :
- Table `borrowings` pour gérer les emprunts
- Relations avec `users` et `books`
- Index pour optimiser les requêtes

---

## 🎯 Fonctionnalités de l'Application

### API REST Endpoints

#### Livres
- `GET /api/books` - Liste tous les livres
- `GET /api/books/{id}` - Détails d'un livre
- `GET /api/books/search?keyword=...` - Recherche de livres
- `GET /api/books/category/{categoryId}` - Livres par catégorie
- `GET /api/books/available` - Livres disponibles
- `POST /api/books` - Créer un livre
- `PUT /api/books/{id}` - Modifier un livre
- `DELETE /api/books/{id}` - Supprimer un livre

#### Catégories
- `GET /api/categories` - Liste toutes les catégories
- `GET /api/categories/{id}` - Détails d'une catégorie
- `POST /api/categories` - Créer une catégorie

#### Informations Flyway
- `GET /api/flyway/info` - État des migrations Flyway

---

## 🚀 Exécution du Projet

### Prérequis
- Java 17 ou supérieur
- Maven 3.6+

### Lancer l'application

```bash
# Compiler et exécuter
mvn spring-boot:run
```

### Accéder à la console H2

1. L'application démarre sur `http://localhost:8080`
2. Accéder à `http://localhost:8080/h2-console`
3. Paramètres de connexion :
   - JDBC URL: `jdbc:h2:mem:flywaydb`
   - Username: `sa`
   - Password: (vide)

### Vérifier les migrations

```bash
# Voir les migrations appliquées
curl http://localhost:8080/api/flyway/info
```

---

## 🔍 Comment Flyway Fonctionne

### 1. Au Démarrage de l'Application

1. Flyway scanne le répertoire `db/migration`
2. Détecte tous les fichiers de migration (V1, V2, V3, ...)
3. Vérifie la table `flyway_schema_history` dans la base de données
4. Exécute uniquement les migrations non encore appliquées
5. Enregistre chaque migration dans `flyway_schema_history`

### 2. Table flyway_schema_history

Flyway crée automatiquement cette table pour suivre les migrations :

| installed_rank | version | description | type | script | installed_on | success |
|----------------|---------|-------------|------|--------|--------------|---------|
| 1 | 1 | init | SQL | V1__init.sql | 2024-01-10 | true |
| 2 | 2 | add books table | SQL | V2__add_books_table.sql | 2024-01-10 | true |

### 3. Gestion des Versions

- Les migrations sont exécutées **dans l'ordre** des numéros de version
- Une fois appliquée, une migration **ne peut pas être modifiée**
- Pour modifier un schéma, créer une **nouvelle migration** (V7, V8, etc.)

---

## 💡 Avantages de Flyway

### ✅ Versioning du Schéma
- Historique complet des changements de schéma
- Traçabilité de chaque modification

### ✅ Automatisation
- Migrations automatiques au démarrage
- Pas besoin d'exécuter manuellement les scripts SQL

### ✅ Sécurité
- Validation des migrations avant exécution
- Détection des migrations corrompues ou modifiées

### ✅ Collaboration
- Synchronisation du schéma entre développeurs
- Déploiement cohérent en production

### ✅ Rollback (avec Flyway Enterprise)
- Possibilité de revenir en arrière sur les migrations

---

## 📊 Exemples de Code

### Créer une Nouvelle Migration

1. Créer un fichier `V7__add_new_feature.sql` dans `src/main/resources/db/migration/`

```sql
-- Migration V7: Ajout d'une nouvelle fonctionnalité
ALTER TABLE books ADD COLUMN IF NOT EXISTS rating DECIMAL(3,2);
CREATE INDEX IF NOT EXISTS idx_books_rating ON books(rating);
```

2. Au prochain démarrage, Flyway exécutera automatiquement cette migration

### Entité JPA Correspondante

```java
@Entity
@Table(name = "books")
public class Book {
    // ... autres champs
    
    @Column(precision = 3, scale = 2)
    private BigDecimal rating;
}
```

---

## 🎓 Points Clés pour la Présentation

### 1. Problème Résolu
- **Avant Flyway** : Schémas divergents entre environnements, scripts SQL manuels, risque d'erreurs
- **Avec Flyway** : Schémas versionnés, migrations automatisées, cohérence garantie

### 2. Fonctionnement
- Convention de nommage stricte
- Exécution automatique au démarrage
- Table de suivi `flyway_schema_history`

### 3. Bonnes Pratiques
- Ne jamais modifier une migration existante
- Créer une nouvelle migration pour chaque changement
- Tester les migrations avant le déploiement
- Utiliser des noms descriptifs

### 4. Cas d'Usage
- Développement d'applications avec évolution du schéma
- Déploiements en production
- Collaboration en équipe
- CI/CD pipelines

---

## 🧪 Tests

### Tester les Migrations

```bash
# Lancer les tests
mvn test
```

### Vérifier Manuellement

1. Démarrer l'application
2. Accéder à `/h2-console`
3. Exécuter : `SELECT * FROM flyway_schema_history;`
4. Vérifier que toutes les migrations sont listées

---

## 📚 Ressources

- [Documentation Flyway](https://flywaydb.org/documentation/)
- [Spring Boot + Flyway](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
- [Flyway Best Practices](https://flywaydb.org/documentation/learnmore/best-practices)

---

## 👥 Auteurs

Projet réalisé dans le cadre du cours J2E - Groupe 10

---

## 📄 Licence

Ce projet est un exemple éducatif.
