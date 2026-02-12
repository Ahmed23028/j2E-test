# أمثلة عملية للكود - شرح مفصل

## 🎯 مثال 1: كيف يعمل @PrePersist

### الكود:
```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}
```

### الشرح:
- **`@PrePersist`**: يتم تنفيذه **قبل** حفظ الكائن في قاعدة البيانات
- **متى يتم استدعاؤه:** عند `bookRepository.save(book)` لأول مرة
- **الغرض:** ملء `createdAt` و `updatedAt` تلقائياً
- **العلاقة مع الهجرات:** هذه الأعمدة أضيفت في V5__add_audit_columns.sql

### سؤال محتمل: "متى يتم استدعاء @PrePersist؟"
**الإجابة:**
- عند إنشاء سجل جديد (INSERT)
- **ليس** عند التحديث (UPDATE)
- للتحديث: نستخدم `@PreUpdate`

---

## 🔄 مثال 2: دورة حياة Book Entity

### السيناريو: إنشاء كتاب جديد

```java
// 1. إنشاء كائن Book
Book book = new Book();
book.setTitle("New Book");
book.setAuthor("Author Name");

// 2. حفظ في قاعدة البيانات
bookRepository.save(book);

// ما يحدث:
// - @PrePersist.onCreate() → يملأ createdAt و updatedAt
// - INSERT INTO books (...) VALUES (...)
// - يتم توليد id تلقائياً
```

### الشرح:
1. **إنشاء الكائن** → في الذاكرة فقط
2. **save()** → يبدأ Transaction
3. **@PrePersist** → يملأ التواريخ
4. **INSERT** → حفظ في قاعدة البيانات
5. **Commit** → تأكيد العملية

---

## 🔗 مثال 3: العلاقات JPA

### الكود:
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
- **`@ManyToOne`**: عدة كتب → فئة واحدة
- **`@OneToMany`**: فئة واحدة → عدة كتب
- **`mappedBy = "category"`**: يحدد أن Book هو المالك للعلاقة
- **`LAZY`**: Category لا يُحمّل إلا عند الطلب

### مثال عملي:
```java
Book book = bookRepository.findById(1L).get();
// book.category = null (لم يُحمّل بعد)

String categoryName = book.getCategory().getName();
// الآن يتم تحميل Category من قاعدة البيانات
```

### سؤال محتمل: "ما الفرق بين LAZY و EAGER؟"
**الإجابة:**
- **LAZY**: تحميل عند الطلب (أفضل للأداء)
- **EAGER**: تحميل فوري (قد يسبب N+1 problem)

---

## 📊 مثال 4: Query Methods في Repository

### الكود:
```java
List<Book> findByTitleContainingIgnoreCase(String title);
```

### كيف يعمل:
```java
// الاستدعاء
bookRepository.findByTitleContainingIgnoreCase("spring");

// SQL المولد تلقائياً
SELECT * FROM books 
WHERE LOWER(title) LIKE LOWER('%spring%');
```

### الشرح:
- **`findBy`**: يبدأ استعلام SELECT
- **`Title`**: العمود `title`
- **`Containing`**: LIKE '%...%'
- **`IgnoreCase`**: LOWER() للبحث غير حساس لحالة الأحرف

### أمثلة أخرى:
```java
findByAuthor(String author)           → WHERE author = ?
findByPriceGreaterThan(BigDecimal p) → WHERE price > ?
findByCategoryId(Long id)            → WHERE category_id = ?
```

---

## 🎨 مثال 5: Lombok في العمل

### بدون Lombok:
```java
public class Book {
    private String title;
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Override
    public String toString() {
        return "Book{title='" + title + "'}";
    }
    
    // ... equals, hashCode, etc.
}
```

### مع Lombok:
```java
@Data
public class Book {
    private String title;
    // Lombok يولد كل شيء تلقائياً!
}
```

### الشرح:
- **`@Data`** يولد:
  - `getTitle()`, `setTitle()`
  - `toString()`, `equals()`, `hashCode()`
- **التوفير:** ~40 سطر → 3 أسطر

---

## 🔧 مثال 6: @Transactional في العمل

### الكود:
```java
@Transactional
public Book createBook(Book book) {
    // 1. التحقق من Category
    if (book.getCategory() != null) {
        Category cat = categoryRepository.findById(...).get();
        book.setCategory(cat);
    }
    // 2. حفظ Book
    return bookRepository.save(book);
}
```

### ماذا يحدث بدون @Transactional:
```
1. تحميل Category ✅
2. حفظ Book ❌ (خطأ)
→ Category تم تحميله لكن Book لم يُحفظ
→ بيانات غير متسقة
```

### مع @Transactional:
```
1. بدء Transaction
2. تحميل Category ✅
3. حفظ Book ✅
4. Commit (كل شيء أو لا شيء)
```

### سؤال محتمل: "متى تستخدم @Transactional؟"
**الإجابة:**
- عند عمليات متعددة على قاعدة البيانات
- عند الحاجة لضمان Atomicity
- في Services (ليس في Controllers)

---

## 🌐 مثال 7: REST API في العمل

### Request:
```
POST http://localhost:8083/api/books
Content-Type: application/json

{
  "title": "New Book",
  "author": "Author Name",
  "category": {"id": 4}
}
```

### ما يحدث:
```java
// 1. Controller يستقبل Request
@PostMapping
public ResponseEntity<Book> createBook(@RequestBody Book book) {
    // @RequestBody يحول JSON → Book object
    
    // 2. استدعاء Service
    Book created = bookService.createBook(book);
    
    // 3. إرجاع Response
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}
```

### الشرح:
- **`@RequestBody`**: يحول JSON إلى كائن Java
- **`@PostMapping`**: HTTP POST
- **`ResponseEntity`**: للتحكم في Status Code (201 Created)

---

## 🔍 مثال 8: البحث المعقد

### الكود:
```java
@Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword%")
List<Book> searchBooks(@Param("keyword") String keyword);
```

### كيف يعمل:
```java
// الاستدعاء
bookService.searchBooks("Spring");

// SQL المولد
SELECT b.* FROM books b 
WHERE b.title LIKE '%Spring%' 
   OR b.author LIKE '%Spring%';
```

### الشرح:
- **JPQL**: Java Persistence Query Language
- **`%:keyword%`**: LIKE مع wildcards
- **`@Param`**: ربط المعامل في الاستعلام

---

## 📝 مثال 9: العلاقة بين الهجرات والكود

### الهجرة V2:
```sql
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    category_id BIGINT,
    ...
);
```

### الكود Java:
```java
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // → id BIGSERIAL
    
    @Column(nullable = false, length = 255)
    private String title;  // → title VARCHAR(255) NOT NULL
    
    @Column(nullable = false, length = 255)
    private String author;  // → author VARCHAR(255) NOT NULL
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;  // → category_id BIGINT
}
```

### الشرح:
- **الهجرة** تنشئ الجدول
- **الكود** يطابق الجدول
- **JPA** يتحقق من التطابق عند البدء

---

## 🎯 مثال 10: Method Reference

### الكود:
```java
category.ifPresent(book::setCategory);
```

### بدون Method Reference:
```java
category.ifPresent(cat -> book.setCategory(cat));
```

### الشرح:
- **`book::setCategory`**: Method Reference (Java 8+)
- **أقصر وأوضح** من Lambda
- **نفس الوظيفة**

---

## 💡 نقاط مهمة للعرض

### 1. العلاقة بين الهجرات والكود:
```
SQL Migration → Database Table → JPA Entity → Repository → Service → Controller
```

### 2. دورة حياة الكائن:
```
new Book() → @PrePersist → save() → INSERT → @PostPersist
```

### 3. Flyway قبل JPA:
```
Application Start → Flyway Migrations → JPA Validation → Beans Creation
```

---

## ✅ الخلاصة

**كن مستعداً لشرح:**
1. ✅ كيف تطابق Entities الجداول
2. ✅ كيف تعمل العلاقات JPA
3. ✅ كيف Lombok يقلل الكود
4. ✅ كيف @Transactional يضمن Atomicity
5. ✅ كيف Query Methods تعمل
6. ✅ كيف Flyway يتكامل مع Spring Boot
