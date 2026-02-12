# كيفية التحقق من الجداول

## ✅ جدول users موجود في V1__init.sql

### في الهجرة V1:
```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    active BOOLEAN DEFAULT TRUE
);
```

## 🔍 كيفية التحقق

### 1. في H2 Console:

افتح: `http://localhost:8083/h2-console`

#### أ) عرض جميع الجداول:
```sql
SHOW TABLES;
```

يجب أن ترى:
- `USERS`
- `CATEGORIES`
- `BOOKS`
- `BORROWINGS`
- `FLYWAY_SCHEMA_HISTORY`

#### ب) عرض بنية جدول users:
```sql
SELECT * FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_NAME = 'USERS';
```

#### ج) عرض أعمدة جدول users:
```sql
SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'USERS';
```

#### د) عرض بيانات جدول users:
```sql
SELECT * FROM users;
```

### 2. عبر API (إذا كان لديك UserController):

يمكنك إضافة:
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
```

### 3. في السجلات (Logs):

عند بدء التطبيق، يجب أن ترى:
```
Migrating schema "PUBLIC" to version "1 - init"
```

---

## ❓ لماذا قد لا ترى الجدول؟

### السبب 1: التطبيق لم يبدأ بشكل صحيح
**الحل:** أعد تشغيل التطبيق وتحقق من السجلات

### السبب 2: الهجرات لم تُطبق
**الحل:** تحقق من `flyway_schema_history`:
```sql
SELECT * FROM flyway_schema_history;
```

### السبب 3: قاعدة البيانات فارغة (H2 في الذاكرة)
**الحل:** H2 في الذاكرة تُحذف عند إيقاف التطبيق. أعد تشغيله.

### السبب 4: البحث في قاعدة بيانات خاطئة
**الحل:** تأكد من الاتصال بـ `jdbc:h2:mem:flywaydb`

---

## ✅ التحقق السريع

### في H2 Console:
```sql
-- 1. عرض جميع الجداول
SHOW TABLES;

-- 2. عرض الهجرات المطبقة
SELECT version, description, success 
FROM flyway_schema_history 
ORDER BY installed_rank;

-- 3. عرض بيانات users (إذا كانت موجودة)
SELECT * FROM users;

-- 4. عدد السجلات في كل جدول
SELECT 'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'categories', COUNT(*) FROM categories
UNION ALL
SELECT 'books', COUNT(*) FROM books;
```

---

## 🎯 إذا كان الجدول غير موجود

### الحل 1: أعد تشغيل التطبيق
```bash
mvn spring-boot:run
```

### الحل 2: تحقق من السجلات
ابحث عن:
```
Migrating schema "PUBLIC" to version "1 - init"
Successfully applied 6 migrations
```

### الحل 3: تحقق من flyway_schema_history
```sql
SELECT * FROM flyway_schema_history;
```

إذا لم ترى V1، فهذا يعني أن الهجرة لم تُطبق.

---

## 📊 الجداول المتوقعة

بعد تطبيق جميع الهجرات، يجب أن تجد:

1. **USERS** (من V1)
2. **CATEGORIES** (من V1)
3. **BOOKS** (من V2)
4. **BORROWINGS** (من V6)
5. **FLYWAY_SCHEMA_HISTORY** (من Flyway)

---

## 🔧 إذا استمرت المشكلة

### تحقق من:
1. ✅ التطبيق يعمل على المنفذ 8083
2. ✅ H2 Console متاح
3. ✅ الاتصال بـ `jdbc:h2:mem:flywaydb`
4. ✅ السجلات تظهر تطبيق الهجرات

### أعد تشغيل التطبيق:
```bash
# إيقاف جميع عمليات Java
Get-Process java | Stop-Process -Force

# إعادة التشغيل
mvn clean spring-boot:run
```
