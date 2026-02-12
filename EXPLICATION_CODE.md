# شرح الكود - Spring Boot + Flyway

## 📁 هيكل المشروع

```
src/main/java/com/example/flywaydemo/
├── FlywayDemoApplication.java    # نقطة البداية
├── entity/                       # الكيانات JPA
│   ├── User.java
│   ├── Category.java
│   ├── Book.java
│   └── Borrowing.java
├── repository/                   # واجهات Spring Data
│   ├── UserRepository.java
│   ├── CategoryRepository.java
│   ├── BookRepository.java
│   └── BorrowingRepository.java
├── service/                      # منطق الأعمال
│   └── BookService.java
└── controller/                   # REST API
    ├── BookController.java
    ├── CategoryController.java
    └── FlywayInfoController.java
```

---

## 🚀 1. FlywayDemoApplication.java

### الكود:
```java
@SpringBootApplication
public class FlywayDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlywayDemoApplication.class, args);
    }
}
```

### الشرح:
- **`@SpringBootApplication`**: تجمع `@Configuration`, `@EnableAutoConfiguration`, `@ComponentScan`
- عند بدء التطبيق، Spring Boot يكتشف Flyway تلقائياً
- Flyway يطبق الهجرات **قبل** تهيئة JPA/Hibernate
- هذا يضمن أن الجداول موجودة قبل استخدامها

### سؤال محتمل: "متى تُطبق الهجرات؟"
**الإجابة:** عند بدء التطبيق، قبل تهيئة JPA/Hibernate

---

## 📊 2. Entity: Book.java

### الكود المهم:
```java
@Entity
@Table(name = "books", indexes = {...})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(nullable = false, length = 255)
    private String author;  // أضيف في V3
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "books"})
    private Category category;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;  // أضيف في V5
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
}
```

### الشرح:
- **`@Entity`**: يحدد أن هذه الكلاس تمثل جدول في قاعدة البيانات
- **`@Table(name = "books")`**: يحدد اسم الجدول (مطابق للجدول في الهجرات)
- **`@Data`**: Lombok يولد getters/setters تلقائياً
- **`@Column(name = "author")`**: يربط الحقل بالعمود في قاعدة البيانات
- **`@ManyToOne`**: علاقة مع Category (جدول categories)
- **`@JsonIgnoreProperties`**: يمنع مشاكل JSON مع LAZY loading

### سؤال محتمل: "كيف تربط الكود بالهجرات؟"
**الإجابة:** 
- الهجرات تنشئ الجداول (V1, V2...)
- الكود Java (Entities) يطابق هذه الجداول
- `@Table(name = "books")` يربط الكلاس بالجدول الموجود

---

## 🔍 3. Repository: BookRepository.java

### الكود:
```java
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    List<Book> findByCategoryId(Long categoryId);
    
    @Query("SELECT b FROM Book b WHERE b.stockQuantity > 0")
    List<Book> findAvailableBooks();
    
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword%")
    List<Book> searchBooks(@Param("keyword") String keyword);
}
```

### الشرح:
- **`extends JpaRepository<Book, Long>`**: يوفر عمليات CRUD الأساسية
- **Query Methods**: Spring Data يولد SQL تلقائياً من اسم الدالة
  - `findByTitleContainingIgnoreCase` → `WHERE title LIKE '%...%'`
- **`@Query`**: لاستعلامات مخصصة (JPQL)
- **`@Param`**: لربط المعاملات في الاستعلام

### سؤال محتمل: "كيف تعمل Query Methods؟"
**الإجابة:**
- Spring Data يقرأ اسم الدالة
- يولد SQL تلقائياً
- `findByTitleContainingIgnoreCase` → `WHERE LOWER(title) LIKE LOWER('%...%')`

---

## 🎯 4. Service: BookService.java

### الكود المهم:
```java
@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    
    @Transactional
    public Book createBook(Book book) {
        if (book.getCategory() != null && book.getCategory().getId() != null) {
            Optional<Category> category = categoryRepository.findById(book.getCategory().getId());
            category.ifPresent(book::setCategory);
        }
        return bookRepository.save(book);
    }
}
```

### الشرح:
- **`@Service`**: يحدد أن هذه الكلاس هي Service (منطق الأعمال)
- **`@RequiredArgsConstructor`**: Lombok يولد constructor للـ final fields
- **`@Transactional`**: يضمن أن العملية كلها أو لا شيء (Atomicity)
- **`category.ifPresent(book::setCategory)`**: Method reference (Java 8)

### سؤال محتمل: "ما هو دور @Transactional؟"
**الإجابة:**
- يضمن أن العملية كلها تُنفذ أو لا شيء
- في حالة الخطأ، يتم Rollback تلقائياً
- مهم عند عمليات متعددة على قاعدة البيانات

---

## 🌐 5. Controller: BookController.java

### الكود:
```java
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

### الشرح:
- **`@RestController`**: يجمع `@Controller` + `@ResponseBody`
- **`@RequestMapping("/api/books")`**: المسار الأساسي لجميع endpoints
- **`@GetMapping`**: HTTP GET request
- **`@PathVariable`**: لاستخراج المعاملات من URL
- **`ResponseEntity`**: للتحكم في HTTP status code

### سؤال محتمل: "ما الفرق بين @Controller و @RestController؟"
**الإجابة:**
- **`@Controller`**: للـ Views (HTML)
- **`@RestController`**: للـ REST API (JSON)
- `@RestController` = `@Controller` + `@ResponseBody`

---

## 🔧 6. FlywayInfoController.java

### الكود:
```java
@RestController
@RequestMapping("/api/flyway")
public class FlywayInfoController {
    @Autowired(required = false)
    private Flyway flyway;
    
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getFlywayInfo() {
        Map<String, Object> info = new HashMap<>();
        
        if (flyway != null) {
            var migrations = flyway.info().all();
            var current = flyway.info().current();
            
            info.put("totalMigrations", migrations.length);
            info.put("currentVersion", current != null ? 
                current.getVersion().toString() : "baseline");
            
            // ... معلومات الهجرات
        }
        
        return ResponseEntity.ok(info);
    }
}
```

### الشرح:
- **`@Autowired(required = false)`**: Flyway قد لا يكون متاحاً (اختياري)
- **`flyway.info().all()`**: جميع الهجرات (مطبقة وغير مطبقة)
- **`flyway.info().current()`**: آخر هجرة مطبقة
- هذا endpoint يظهر حالة Flyway عبر API

### سؤال محتمل: "كيف تحصل على معلومات Flyway من الكود؟"
**الإجابة:**
- حقن `Flyway` bean في الكلاس
- استخدام `flyway.info()` للحصول على المعلومات
- عرضها عبر REST API

---

## ⚙️ 7. application.properties

### الكود المهم:
```properties
# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.sql-migration-prefix=V
spring.flyway.sql-migration-separator=__
spring.flyway.sql-migration-suffixes=.sql

# JPA
spring.jpa.hibernate.ddl-auto=validate
```

### الشرح:
- **`spring.flyway.enabled=true`**: تفعيل Flyway
- **`spring.flyway.locations`**: مكان ملفات الهجرة
- **`spring.flyway.sql-migration-prefix=V`**: يحدد أن الملفات تبدأ بـ V
- **`spring.jpa.hibernate.ddl-auto=validate`**: Hibernate يتحقق فقط (لا ينشئ جداول)
  - **مهم:** Flyway هو المسؤول عن إنشاء الجداول

### سؤال محتمل: "لماذا validate وليس create؟"
**الإجابة:**
- `create`: Hibernate ينشئ الجداول (تعارض مع Flyway)
- `validate`: Hibernate يتحقق فقط من وجود الجداول
- Flyway هو المسؤول الوحيد عن إدارة بنية قاعدة البيانات

---

## 🔗 8. العلاقات بين الكيانات

### Book ↔ Category:
```java
// في Book.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "category_id")
private Category category;

// في Category.java
@OneToMany(mappedBy = "category")
private List<Book> books;
```

### الشرح:
- **`@ManyToOne`**: كتاب واحد ينتمي لفئة واحدة
- **`@OneToMany`**: فئة واحدة لها عدة كتب
- **`mappedBy = "category"`**: يحدد أن Category هو المالك للعلاقة
- **`LAZY`**: تحميل Category فقط عند الحاجة

### سؤال محتمل: "ما الفرق بين LAZY و EAGER؟"
**الإجابة:**
- **LAZY**: تحميل عند الطلب (أفضل للأداء)
- **EAGER**: تحميل فوري (قد يسبب مشاكل أداء)
- في مشروعنا: استخدمنا LAZY لتجنب تحميل بيانات غير ضرورية

---

## 📝 9. الهجرات SQL

### مثال: V3__add_author_column.sql
```sql
-- Ajout de la colonne author
ALTER TABLE books ADD COLUMN IF NOT EXISTS author VARCHAR(255);

-- Mise à jour des données existantes
UPDATE books SET author = 'Craig Walls' WHERE title = 'Spring Boot in Action';

-- Ajout de contrainte
ALTER TABLE books ALTER COLUMN author SET NOT NULL;
```

### الشرح:
- **`IF NOT EXISTS`**: لتجنب الأخطاء إذا كان العمود موجوداً
- **UPDATE**: تحديث البيانات الموجودة
- **NOT NULL**: إضافة قيد بعد ملء البيانات

### سؤال محتمل: "لماذا UPDATE قبل NOT NULL؟"
**الإجابة:**
- لا يمكن إضافة NOT NULL على عمود يحتوي NULL
- أولاً: إضافة العمود (قابل للـ NULL)
- ثانياً: ملء البيانات
- ثالثاً: إضافة قيد NOT NULL

---

## 🎨 10. Lombok Annotations

### الكود:
```java
@Data                    // getters, setters, toString, equals, hashCode
@NoArgsConstructor       // constructor بدون معاملات
@AllArgsConstructor      // constructor بجميع المعاملات
@RequiredArgsConstructor // constructor للـ final fields فقط
```

### الشرح:
- **`@Data`**: يولد جميع الـ getters/setters تلقائياً
- **`@NoArgsConstructor`**: مهم لـ JPA (يحتاج constructor فارغ)
- **`@AllArgsConstructor`**: لإنشاء كائنات بسهولة
- **`@RequiredArgsConstructor`**: للـ dependency injection

### سؤال محتمل: "كيف يعمل Lombok؟"
**الإجابة:**
- Lombok هو **Annotation Processor**
- عند التجميع، يولد الكود تلقائياً
- في `pom.xml`: أضفنا annotation processor path
- الكود المولد موجود في `.class` files

---

## 🔄 11. دورة حياة الهجرة

### الخطوات:
1. **بدء التطبيق** → Spring Boot يبدأ
2. **اكتشاف Flyway** → Auto-configuration
3. **فحص الهجرات** → قراءة `db/migration/`
4. **التحقق** → مقارنة مع `flyway_schema_history`
5. **التطبيق** → تنفيذ الهجرات الجديدة
6. **التسجيل** → حفظ في `flyway_schema_history`
7. **تهيئة JPA** → بعد تطبيق جميع الهجرات

### سؤال محتمل: "ما هو ترتيب التنفيذ؟"
**الإجابة:**
1. Flyway يطبق الهجرات
2. ثم JPA/Hibernate يتحقق من الجداول
3. ثم التطبيق يبدأ

---

## 🛠️ 12. Dependency Injection

### الكود:
```java
@Service
@RequiredArgsConstructor  // يولد constructor
public class BookService {
    private final BookRepository bookRepository;  // final = required
    private final CategoryRepository categoryRepository;
}
```

### الشرح:
- **`@RequiredArgsConstructor`**: يولد constructor للـ `final` fields
- **`private final`**: Spring يقوم بالـ injection تلقائياً
- **بدون `@Autowired`**: Spring Boot 3+ لا يحتاجه

### سؤال محتمل: "كيف يعمل Dependency Injection؟"
**الإجابة:**
- Spring يبحث عن beans مناسبة
- يحقنها في constructor تلقائياً
- `@RequiredArgsConstructor` يولد constructor
- Spring يستخدم هذا constructor للحقن

---

## 📋 13. أمثلة على الكود

### مثال 1: إنشاء كتاب جديد
```java
// في BookController
@PostMapping
public ResponseEntity<Book> createBook(@RequestBody Book book) {
    Book createdBook = bookService.createBook(book);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
}

// في BookService
@Transactional
public Book createBook(Book book) {
    // التحقق من Category
    if (book.getCategory() != null && book.getCategory().getId() != null) {
        Optional<Category> category = categoryRepository.findById(book.getCategory().getId());
        category.ifPresent(book::setCategory);
    }
    return bookRepository.save(book);
}
```

### الشرح:
- **`@RequestBody`**: يحول JSON إلى كائن Java
- **`@Transactional`**: يضمن أن العملية كلها أو لا شيء
- **`category.ifPresent()`**: Method reference (Java 8)

---

### مثال 2: البحث عن كتب
```java
// في BookRepository
@Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword%")
List<Book> searchBooks(@Param("keyword") String keyword);

// في BookController
@GetMapping("/search")
public ResponseEntity<List<Book>> searchBooks(@RequestParam String keyword) {
    return ResponseEntity.ok(bookService.searchBooks(keyword));
}
```

### الشرح:
- **JPQL**: Java Persistence Query Language
- **`%:keyword%`**: LIKE مع wildcards
- **`@RequestParam`**: معامل من URL query string

---

## 🎯 نقاط مهمة للعرض

### 1. العلاقة بين الهجرات والكود:
```
V1__init.sql → Table "users" → Entity User.java
V2__add_books_table.sql → Table "books" → Entity Book.java
V3__add_author_column.sql → Column "author" → Field author in Book.java
```

### 2. Flyway يطبق الهجرات قبل JPA:
```
1. Flyway Migration
2. JPA Validation
3. Application Start
```

### 3. Lombok يقلل الكود:
```java
// بدون Lombok: ~50 سطر
// مع Lombok: ~10 أسطر
```

---

## 💡 أسئلة محتملة عن الكود

### س1: "كيف تربط Entity بالجدول؟"
**الإجابة:**
- `@Table(name = "books")` يحدد اسم الجدول
- `@Column(name = "author")` يربط الحقل بالعمود
- يجب أن تطابق أسماء الجداول والأعمدة في الهجرات

### س2: "ما هو دور @PrePersist؟"
**الإجابة:**
- يتم تنفيذه قبل حفظ الكائن في قاعدة البيانات
- في مشروعنا: يملأ `createdAt` و `updatedAt` تلقائياً
- هذا يطابق الأعمدة المضافة في V5

### س3: "لماذا @JsonIgnoreProperties؟"
**الإجابة:**
- LAZY loading يسبب مشاكل عند تحويل إلى JSON
- `@JsonIgnoreProperties` يمنع تحميل العلاقات تلقائياً
- يمنع أخطاء "LazyInitializationException"

### س4: "كيف يعمل Spring Data JPA؟"
**الإجابة:**
- Spring Data يولد implementation تلقائياً
- من اسم الدالة: `findByTitle` → `WHERE title = ?`
- لا نحتاج كتابة implementation يدوياً

---

## 📚 مصطلحات مهمة

| المصطلح | الشرح |
|---------|-------|
| **Entity** | كلاس Java يمثل جدول في قاعدة البيانات |
| **Repository** | واجهة للوصول إلى البيانات |
| **Service** | منطق الأعمال (Business Logic) |
| **Controller** | REST API endpoints |
| **@Transactional** | يضمن Atomicity |
| **LAZY Loading** | تحميل عند الطلب |
| **JPQL** | Java Persistence Query Language |

---

## ✅ الخلاصة

**نقاط مهمة:**
1. الهجرات تنشئ الجداول
2. Entities تطابق الجداول
3. Repositories تصل إلى البيانات
4. Services تحتوي منطق الأعمال
5. Controllers تعرض REST API

**كن مستعداً لإظهار:**
- ✅ كيف تطابق Entities الجداول
- ✅ كيف تعمل العلاقات
- ✅ كيف Flyway يطبق الهجرات قبل JPA
- ✅ كيف Lombok يقلل الكود
