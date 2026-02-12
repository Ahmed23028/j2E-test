# Présentation PowerPoint - Spring Boot + Flyway

## Slide 1 : Titre
**Spring Boot + Flyway**
Gestion de version et migration de schémas de base de données

Groupe 10 - J2E
Date : 10/01/2025

---

## Slide 2 : Problématique
### Le Problème des Divergences de Schémas

**Sans gestion de migrations :**
- ❌ Schémas différents entre développeurs
- ❌ Scripts SQL exécutés manuellement
- ❌ Pas de traçabilité des changements
- ❌ Risque d'erreurs en production
- ❌ Difficulté à synchroniser les environnements

**Exemple :**
```
Développeur A : ajoute une colonne "email"
Développeur B : ajoute une colonne "phone"
Production : quelle version est la bonne ?
```

---

## Slide 3 : Solution - Flyway
### Qu'est-ce que Flyway ?

**Flyway** est un outil de migration de base de données qui :
- ✅ Versionne le schéma de la base de données
- ✅ Automatise l'exécution des migrations
- ✅ Garantit la cohérence entre environnements
- ✅ Suit l'historique des changements

**Principe :**
- Fichiers SQL versionnés (V1, V2, V3...)
- Exécution automatique au démarrage
- Table de suivi `flyway_schema_history`

---

## Slide 4 : Architecture du Projet
### Structure du Projet

```
spring-boot-flyway-demo/
├── src/main/resources/
│   └── db/migration/
│       ├── V1__init.sql
│       ├── V2__add_books_table.sql
│       ├── V3__add_author_column.sql
│       ├── V4__add_indexes.sql
│       ├── V5__add_audit_columns.sql
│       └── V6__add_borrowings_table.sql
└── src/main/java/
    ├── entity/
    ├── repository/
    ├── service/
    └── controller/
```

**Application de bibliothèque** pour illustrer les migrations

---

## Slide 5 : Convention de Nommage
### Format des Migrations

**Format obligatoire :**
```
V{version}__{description}.sql
```

**Exemples :**
- `V1__init.sql` ✅
- `V2__add_books_table.sql` ✅
- `V3__add_author_column.sql` ✅

**Règles :**
- Préfixe `V` obligatoire
- Numéro de version (1, 2, 3...)
- Séparateur `__` (double underscore)
- Description en minuscules avec underscores
- Extension `.sql`

---

## Slide 6 : Migration V1 - Initialisation
### Création des Tables Initiales

```sql
-- V1__init.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    ...
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    ...
);
```

**Résultat :**
- Structure de base créée
- Données initiales insérées
- Base prête pour l'évolution

---

## Slide 7 : Migration V2 - Nouvelle Table
### Ajout de la Table Books

```sql
-- V2__add_books_table.sql
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    category_id BIGINT,
    price DECIMAL(10, 2),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
```

**Illustration :**
- Ajout d'une nouvelle fonctionnalité
- Relation avec les catégories
- Données d'exemple insérées

---

## Slide 8 : Migration V3 - Modification
### Ajout d'une Colonne

```sql
-- V3__add_author_column.sql
ALTER TABLE books 
ADD COLUMN author VARCHAR(255);

UPDATE books 
SET author = 'Nom Auteur' 
WHERE title = 'Titre';

ALTER TABLE books 
ALTER COLUMN author SET NOT NULL;
```

**Points importants :**
- Modification d'une table existante
- Mise à jour des données existantes
- Ajout de contraintes

---

## Slide 9 : Migration V4 - Optimisation
### Ajout d'Index

```sql
-- V4__add_indexes.sql
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_category_stock 
ON books(category_id, stock_quantity);
```

**Bénéfices :**
- Amélioration des performances
- Optimisation des requêtes de recherche
- Index simples et composites

---

## Slide 10 : Migration V5 - Audit
### Colonnes d'Audit

```sql
-- V5__add_audit_columns.sql
ALTER TABLE books 
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE books 
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
```

**Fonctionnalité :**
- Suivi de création et modification
- Appliqué à toutes les tables
- Mise à jour des données existantes

---

## Slide 11 : Fonctionnement de Flyway
### Cycle de Vie d'une Migration

1. **Démarrage de l'application**
   - Flyway scanne `db/migration/`

2. **Vérification**
   - Lit la table `flyway_schema_history`
   - Compare avec les fichiers disponibles

3. **Exécution**
   - Exécute uniquement les nouvelles migrations
   - Dans l'ordre des versions (V1 → V2 → V3...)

4. **Enregistrement**
   - Enregistre chaque migration dans `flyway_schema_history`

---

## Slide 12 : Table flyway_schema_history
### Suivi des Migrations

| version | description | type | script | installed_on | success |
|---------|-------------|------|--------|--------------|---------|
| 1 | init | SQL | V1__init.sql | 2024-01-10 | ✅ |
| 2 | add books table | SQL | V2__add_books_table.sql | 2024-01-10 | ✅ |
| 3 | add author column | SQL | V3__add_author_column.sql | 2024-01-10 | ✅ |

**Avantages :**
- Historique complet
- Traçabilité
- Détection des migrations appliquées

---

## Slide 13 : Configuration Spring Boot
### application.properties

```properties
# Activation de Flyway
spring.flyway.enabled=true

# Emplacement des migrations
spring.flyway.locations=classpath:db/migration

# Convention de nommage
spring.flyway.sql-migration-prefix=V
spring.flyway.sql-migration-separator=__
spring.flyway.sql-migration-suffixes=.sql

# Validation
spring.flyway.validate-on-migrate=true

# JPA ne doit pas créer les tables
spring.jpa.hibernate.ddl-auto=validate
```

**Important :** Hibernate en mode `validate` pour laisser Flyway gérer le schéma

---

## Slide 14 : Code Java - Entités JPA
### Correspondance avec les Migrations

```java
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String isbn;
    
    // Colonne ajoutée dans V3
    private String author;
    
    // Colonnes ajoutées dans V5
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Synchronisation :**
- Entités JPA alignées avec le schéma
- Validation automatique au démarrage

---

## Slide 15 : API REST
### Endpoints Disponibles

**Livres :**
- `GET /api/books` - Liste tous les livres
- `GET /api/books/{id}` - Détails d'un livre
- `GET /api/books/search?keyword=...` - Recherche
- `POST /api/books` - Créer un livre

**Flyway Info :**
- `GET /api/flyway/info` - État des migrations

**Démonstration :**
- Application fonctionnelle
- Utilisation des entités créées par les migrations

---

## Slide 16 : Avantages de Flyway
### Pourquoi Utiliser Flyway ?

✅ **Versioning**
- Historique complet des changements
- Traçabilité de chaque modification

✅ **Automatisation**
- Migrations au démarrage
- Pas d'intervention manuelle

✅ **Sécurité**
- Validation des migrations
- Détection des erreurs

✅ **Collaboration**
- Synchronisation entre développeurs
- Déploiement cohérent

---

## Slide 17 : Bonnes Pratiques
### Règles à Suivre

1. **Ne jamais modifier une migration existante**
   - Créer une nouvelle migration (V7, V8...)

2. **Nommer clairement les migrations**
   - `V7__add_rating_column.sql` ✅
   - `V7__update.sql` ❌

3. **Tester avant de déployer**
   - Vérifier sur un environnement de test

4. **Une migration = un changement logique**
   - Éviter les migrations trop volumineuses

5. **Documenter les migrations complexes**
   - Commentaires dans les fichiers SQL

---

## Slide 18 : Cas d'Usage
### Quand Utiliser Flyway ?

**Développement :**
- Évolution progressive du schéma
- Collaboration en équipe
- Synchronisation des environnements

**Production :**
- Déploiements automatisés
- Migrations sans interruption
- Rollback possible (Enterprise)

**CI/CD :**
- Intégration dans les pipelines
- Tests automatiques des migrations
- Déploiement cohérent

---

## Slide 19 : Démonstration
### Exécution du Projet

1. **Démarrer l'application**
   ```bash
   mvn spring-boot:run
   ```

2. **Vérifier les migrations**
   - Console H2 : `http://localhost:8080/h2-console`
   - Table `flyway_schema_history`

3. **Tester l'API**
   - `GET /api/books`
   - `GET /api/flyway/info`

4. **Ajouter une nouvelle migration**
   - Créer `V7__add_new_feature.sql`
   - Redémarrer → Migration automatique

---

## Slide 20 : Conclusion
### Résumé

**Flyway résout :**
- ✅ Problème des schémas divergents
- ✅ Gestion manuelle des scripts SQL
- ✅ Manque de traçabilité

**Notre projet démontre :**
- ✅ 6 migrations progressives
- ✅ Application fonctionnelle
- ✅ API REST complète

**Points clés :**
- Convention de nommage stricte
- Exécution automatique
- Historique complet

---

## Slide 21 : Questions
### Merci pour votre attention !

**Questions ?**

**Ressources :**
- Documentation : https://flywaydb.org
- Code source : Disponible sur le projet
- Démonstration : Application en ligne

**Contact :**
Groupe 10 - J2E
