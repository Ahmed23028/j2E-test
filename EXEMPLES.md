# Exemples de Code - Spring Boot + Flyway

## Exemple 1 : Créer une Nouvelle Migration

### Scénario
Vous voulez ajouter une colonne `rating` (note) à la table `books`.

### Étape 1 : Créer le fichier de migration

Créez le fichier : `src/main/resources/db/migration/V7__add_rating_column.sql`

```sql
-- Migration V7: Ajout de la colonne rating pour noter les livres
-- Cette migration illustre l'ajout d'une nouvelle fonctionnalité

-- Ajout de la colonne rating
ALTER TABLE books 
ADD COLUMN IF NOT EXISTS rating DECIMAL(3,2) CHECK (rating >= 0 AND rating <= 5);

-- Mise à jour des livres existants avec une note par défaut (optionnel)
UPDATE books 
SET rating = 4.0 
WHERE rating IS NULL;

-- Création d'un index pour les recherches par note
CREATE INDEX IF NOT EXISTS idx_books_rating ON books(rating);

-- Commentaire sur la colonne (PostgreSQL)
COMMENT ON COLUMN books.rating IS 'Note du livre sur 5 étoiles';
```

### Étape 2 : Mettre à jour l'entité JPA

```java
@Entity
@Table(name = "books")
public class Book {
    // ... autres champs existants
    
    // Nouvelle colonne ajoutée dans V7
    @Column(precision = 3, scale = 2)
    private BigDecimal rating;
    
    // ... reste du code
}
```

### Étape 3 : Redémarrer l'application

Au prochain démarrage, Flyway détectera automatiquement `V7__add_rating_column.sql` et l'exécutera.

```bash
mvn spring-boot:run
```

**Résultat :**
- La colonne `rating` est ajoutée à la table `books`
- Les données existantes sont mises à jour
- L'index est créé
- La migration est enregistrée dans `flyway_schema_history`

---

## Exemple 2 : Migration avec Données Complexes

### Scénario
Ajouter une table de commentaires avec des relations.

### Fichier : `V8__add_comments_table.sql`

```sql
-- Migration V8: Ajout du système de commentaires

-- Table des commentaires
CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_comments_book_id ON comments(book_id);
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at);

-- Insertion de commentaires d'exemple
INSERT INTO comments (book_id, user_id, content, rating) 
SELECT 
    b.id,
    u.id,
    'Excellent livre, je le recommande !',
    5
FROM books b, users u
WHERE b.title = 'Spring Boot in Action' 
  AND u.username = 'admin'
LIMIT 1;
```

---

## Exemple 3 : Migration de Modification de Données

### Scénario
Corriger ou transformer des données existantes.

### Fichier : `V9__normalize_isbn_format.sql`

```sql
-- Migration V9: Normalisation du format ISBN

-- Supprimer les tirets des ISBN existants
UPDATE books 
SET isbn = REPLACE(REPLACE(REPLACE(isbn, '-', ''), ' ', ''), '.', '')
WHERE isbn IS NOT NULL;

-- Ajouter une contrainte pour valider le format ISBN-13
ALTER TABLE books 
ADD CONSTRAINT check_isbn_format 
CHECK (isbn IS NULL OR LENGTH(isbn) = 13 OR LENGTH(isbn) = 10);
```

---

## Exemple 4 : Migration avec Rollback (Conceptuel)

### ⚠️ Important
Flyway Community ne supporte pas le rollback automatique. Pour annuler une migration, vous devez créer une nouvelle migration qui inverse les changements.

### Scénario
Supprimer une colonne ajoutée par erreur.

### Fichier : `V10__remove_unused_column.sql`

```sql
-- Migration V10: Suppression d'une colonne non utilisée
-- Cette migration illustre comment "annuler" un changement précédent

-- Supprimer la colonne (si elle existe)
ALTER TABLE books 
DROP COLUMN IF EXISTS unused_column;
```

**Note :** En production, il est préférable de ne jamais supprimer de colonnes. Utilisez plutôt une approche de dépréciation.

---

## Exemple 5 : Utilisation dans le Code Java

### Service avec la nouvelle colonne rating

```java
@Service
@RequiredArgsConstructor
public class BookService {
    
    private final BookRepository bookRepository;
    
    // Rechercher les livres les mieux notés
    public List<Book> getTopRatedBooks(int limit) {
        return bookRepository.findAll()
            .stream()
            .filter(book -> book.getRating() != null)
            .sorted((b1, b2) -> b2.getRating().compareTo(b1.getRating()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    // Mettre à jour la note d'un livre
    @Transactional
    public Book updateRating(Long bookId, BigDecimal rating) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Livre non trouvé"));
        
        if (rating.compareTo(BigDecimal.ZERO) < 0 || 
            rating.compareTo(new BigDecimal("5")) > 0) {
            throw new IllegalArgumentException("La note doit être entre 0 et 5");
        }
        
        book.setRating(rating);
        return bookRepository.save(book);
    }
}
```

### Contrôleur REST

```java
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    
    private final BookService bookService;
    
    @GetMapping("/top-rated")
    public ResponseEntity<List<Book>> getTopRatedBooks(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(bookService.getTopRatedBooks(limit));
    }
    
    @PutMapping("/{id}/rating")
    public ResponseEntity<Book> updateRating(
            @PathVariable Long id,
            @RequestParam BigDecimal rating) {
        try {
            Book updated = bookService.updateRating(id, rating);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
```

---

## Exemple 6 : Vérifier l'État des Migrations

### Endpoint REST pour voir les migrations

```java
@RestController
@RequestMapping("/api/flyway")
public class FlywayInfoController {
    
    @Autowired
    private Flyway flyway;
    
    @GetMapping("/migrations")
    public ResponseEntity<Map<String, Object>> getMigrations() {
        Map<String, Object> info = new HashMap<>();
        
        var migrations = flyway.info().all();
        
        info.put("totalMigrations", migrations.length);
        info.put("currentVersion", flyway.info().current() != null 
            ? flyway.info().current().getVersion().toString() 
            : "baseline");
        
        List<Map<String, Object>> migrationList = new ArrayList<>();
        for (var migration : migrations) {
            Map<String, Object> mig = new HashMap<>();
            mig.put("version", migration.getVersion() != null 
                ? migration.getVersion().toString() 
                : "baseline");
            mig.put("description", migration.getDescription());
            mig.put("type", migration.getType().toString());
            mig.put("state", migration.getState().toString());
            mig.put("installedOn", migration.getInstalledOn());
            migrationList.add(mig);
        }
        
        info.put("migrations", migrationList);
        
        return ResponseEntity.ok(info);
    }
}
```

---

## Exemple 7 : Test des Migrations

### Test JUnit pour vérifier les migrations

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FlywayMigrationTest {
    
    @Autowired
    private Flyway flyway;
    
    @Test
    void testAllMigrationsApplied() {
        // Vérifier que toutes les migrations sont appliquées
        var migrations = flyway.info().all();
        assertThat(migrations).isNotEmpty();
        
        // Vérifier qu'il n'y a pas de migrations en échec
        long failedMigrations = Arrays.stream(migrations)
            .filter(m -> m.getState() == MigrationState.FAILED)
            .count();
        assertThat(failedMigrations).isZero();
    }
    
    @Test
    void testBooksTableExists() {
        // Vérifier que la table books existe (créée dans V2)
        // Cette vérification dépend de votre configuration de test
    }
}
```

---

## Bonnes Pratiques Illustrées

### ✅ DO (À Faire)

1. **Nommer clairement les migrations**
   ```sql
   V7__add_rating_column.sql  ✅
   ```

2. **Utiliser IF NOT EXISTS pour éviter les erreurs**
   ```sql
   ALTER TABLE books ADD COLUMN IF NOT EXISTS rating DECIMAL(3,2);
   ```

3. **Mettre à jour les données existantes**
   ```sql
   UPDATE books SET rating = 4.0 WHERE rating IS NULL;
   ```

4. **Ajouter des index pour les nouvelles colonnes recherchables**
   ```sql
   CREATE INDEX idx_books_rating ON books(rating);
   ```

### ❌ DON'T (À Éviter)

1. **Ne jamais modifier une migration existante**
   ```sql
   -- ❌ MAUVAIS : Modifier V1__init.sql après qu'elle soit appliquée
   -- ✅ BON : Créer V7__add_new_feature.sql
   ```

2. **Ne pas supprimer des migrations appliquées**
   - Cela casserait la synchronisation entre environnements

3. **Éviter les migrations trop volumineuses**
   - Diviser en plusieurs migrations si nécessaire

4. **Ne pas utiliser de données de test en production**
   ```sql
   -- ❌ MAUVAIS en production
   INSERT INTO books VALUES (1, 'Test Book', ...);
   ```

---

## Résumé

Ces exemples illustrent :
- ✅ Création de nouvelles migrations
- ✅ Modification de schémas existants
- ✅ Ajout de colonnes, tables, index
- ✅ Transformation de données
- ✅ Utilisation dans le code Java
- ✅ Bonnes pratiques

**Rappel :** Chaque migration doit être idempotente et testée avant le déploiement en production.
