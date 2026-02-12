# ملخص سريع - شرح الكود

## 🎯 أهم النقاط للعرض

### 1. العلاقة بين الهجرات والكود

```
V2__add_books_table.sql
    ↓
CREATE TABLE books (...)
    ↓
@Entity @Table(name = "books")
public class Book { ... }
    ↓
@Repository
BookRepository extends JpaRepository<Book, Long>
    ↓
@Service
BookService uses BookRepository
    ↓
@RestController
BookController exposes REST API
```

**الشرح:** الهجرات تنشئ الجداول → Entities تطابقها → Repositories تصل إليها → Services تستخدمها → Controllers تعرضها

---

## 📋 2. شرح سريع لكل طبقة

### Entity (Book.java)
```java
@Entity                    // هذا كلاس يمثل جدول
@Table(name = "books")     // اسم الجدول في قاعدة البيانات
@Data                      // Lombok: getters/setters
public class Book {
    @Id                    // Primary Key
    @GeneratedValue         // Auto-increment
    private Long id;
    
    @Column(name = "title") // يربط بالعمود في الجدول
    private String title;
}
```

**الوظيفة:** تمثل جدول `books` في قاعدة البيانات

---

### Repository (BookRepository.java)
```java
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    // Spring Data يولد implementation تلقائياً
    List<Book> findByTitleContainingIgnoreCase(String title);
}
```

**الوظيفة:** واجهة للوصول إلى البيانات (CRUD operations)

---

### Service (BookService.java)
```java
@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    
    @Transactional  // يضمن Atomicity
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }
}
```

**الوظيفة:** منطق الأعمال (Business Logic)

---

### Controller (BookController.java)
```java
@RestController
@RequestMapping("/api/books")
public class BookController {
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }
}
```

**الوظيفة:** REST API endpoints

---

## 🔄 3. دورة حياة الكائن

### عند إنشاء كتاب جديد:

```java
// 1. إنشاء الكائن
Book book = new Book();
book.setTitle("New Book");

// 2. حفظ
bookRepository.save(book);

// 3. ما يحدث:
//    - @PrePersist.onCreate() → يملأ createdAt
//    - INSERT INTO books (...) VALUES (...)
//    - يتم توليد id تلقائياً
```

---

## 🎨 4. Lombok - كيف يعمل

### بدون Lombok:
```java
public class Book {
    private String title;
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    // ... 30+ سطر إضافي
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

**الفوائد:** كود أقل، أسهل في القراءة، أقل أخطاء

---

## 🔗 5. العلاقات JPA

### Book ↔ Category:
```java
// في Book
@ManyToOne
@JoinColumn(name = "category_id")
private Category category;

// في Category
@OneToMany(mappedBy = "category")
private List<Book> books;
```

**الشرح:**
- **ManyToOne**: عدة كتب → فئة واحدة
- **mappedBy**: يحدد المالك للعلاقة
- **LAZY**: تحميل عند الطلب (أفضل للأداء)

---

## ⚙️ 6. @Transactional

### بدون @Transactional:
```java
public void createBook(Book book) {
    categoryRepository.save(category);  // ✅
    bookRepository.save(book);          // ❌ خطأ
    // المشكلة: category تم حفظه لكن book لم يُحفظ
}
```

### مع @Transactional:
```java
@Transactional
public void createBook(Book book) {
    categoryRepository.save(category);  // ✅
    bookRepository.save(book);          // ✅
    // كل شيء أو لا شيء (Atomicity)
}
```

---

## 🔍 7. Query Methods

### كيف يعمل Spring Data:
```java
// الكود
List<Book> findByTitleContainingIgnoreCase(String title);

// SQL المولد تلقائياً
SELECT * FROM books 
WHERE LOWER(title) LIKE LOWER('%?%');
```

**الشرح:**
- Spring Data يقرأ اسم الدالة
- يولد SQL تلقائياً
- `findBy` → SELECT
- `Containing` → LIKE '%...%'
- `IgnoreCase` → LOWER()

---

## 📊 8. العلاقة مع Flyway

### الترتيب عند البدء:
```
1. Spring Boot يبدأ
2. Flyway يطبق الهجرات (V1, V2, V3...)
3. JPA يتحقق من الجداول (validate)
4. Entities تطابق الجداول
5. التطبيق يبدأ
```

### لماذا validate؟
```properties
spring.jpa.hibernate.ddl-auto=validate
```
- **validate**: يتحقق فقط (لا ينشئ جداول)
- **Flyway** هو المسؤول عن إنشاء الجداول
- هذا يمنع التعارض

---

## 💡 9. أمثلة عملية

### مثال: إنشاء كتاب جديد

```java
// 1. Controller يستقبل JSON
@PostMapping
public ResponseEntity<Book> createBook(@RequestBody Book book) {
    // @RequestBody: JSON → Book object
    
    // 2. Service يعالج
    Book created = bookService.createBook(book);
    
    // 3. إرجاع Response
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}
```

### ما يحدث في Service:
```java
@Transactional
public Book createBook(Book book) {
    // 1. التحقق من Category
    if (book.getCategory() != null) {
        Category cat = categoryRepository.findById(...).get();
        book.setCategory(cat);
    }
    
    // 2. @PrePersist.onCreate() → يملأ createdAt
    // 3. INSERT INTO books
    return bookRepository.save(book);
}
```

---

## 🎯 10. أسئلة محتملة عن الكود

### س: "كيف تطابق Entity الجدول؟"
**ج:** 
- `@Table(name = "books")` يحدد اسم الجدول
- `@Column(name = "title")` يربط الحقل بالعمود
- يجب أن تطابق أسماء الجداول والأعمدة في الهجرات

### س: "ما هو دور @PrePersist؟"
**ج:**
- يتم تنفيذه قبل حفظ الكائن
- في مشروعنا: يملأ `createdAt` و `updatedAt` تلقائياً
- يطابق الأعمدة المضافة في V5

### س: "لماذا @JsonIgnoreProperties؟"
**ج:**
- LAZY loading يسبب مشاكل عند تحويل إلى JSON
- يمنع تحميل العلاقات تلقائياً
- يمنع أخطاء "LazyInitializationException"

### س: "كيف يعمل Spring Data JPA؟"
**ج:**
- Spring Data يولد implementation تلقائياً
- من اسم الدالة: `findByTitle` → `WHERE title = ?`
- لا نحتاج كتابة implementation يدوياً

---

## ✅ نقاط مهمة للتذكر

1. **الهجرات** تنشئ الجداول
2. **Entities** تطابق الجداول
3. **Repositories** تصل إلى البيانات
4. **Services** تحتوي منطق الأعمال
5. **Controllers** تعرض REST API
6. **Flyway** يطبق الهجرات قبل JPA
7. **Lombok** يقلل الكود
8. **@Transactional** يضمن Atomicity

---

## 🚀 للعرض

**كن مستعداً لإظهار:**
- ✅ كيف تطابق Entities الجداول
- ✅ كيف تعمل العلاقات
- ✅ كيف Flyway يطبق الهجرات قبل JPA
- ✅ كيف Lombok يقلل الكود
- ✅ كيف Query Methods تعمل
