# إعداد PostgreSQL للمشروع

## الخطوة 1: تثبيت PostgreSQL

إذا لم يكن مثبتاً:
1. تحميل من: https://www.postgresql.org/download/windows/
2. تثبيت PostgreSQL
3. تذكر كلمة المرور التي تحددها (افتراضي: postgres)

## الخطوة 2: إنشاء قاعدة البيانات

### الطريقة 1: باستخدام pgAdmin
1. افتح pgAdmin
2. انقر بزر الماوس الأيمن على "Databases"
3. اختر "Create" → "Database"
4. الاسم: `flywaydb`
5. اضغط "Save"

### الطريقة 2: باستخدام psql (Command Line)
```sql
-- فتح psql
psql -U postgres

-- إنشاء قاعدة البيانات
CREATE DATABASE flywaydb;

-- الخروج
\q
```

### الطريقة 3: باستخدام PowerShell
```powershell
# إذا كان PostgreSQL في PATH
psql -U postgres -c "CREATE DATABASE flywaydb;"
```

## الخطوة 3: تعديل كلمة المرور (إذا لزم الأمر)

في `application.properties`:
```properties
spring.datasource.password=your_password
```

## الخطوة 4: التحقق من الاتصال

```powershell
# اختبار الاتصال
psql -U postgres -d flywaydb -c "SELECT version();"
```

## الخطوة 5: تشغيل التطبيق

```bash
mvn spring-boot:run
```

Flyway سيطبق جميع الهجرات تلقائياً على PostgreSQL.

## ملاحظات مهمة

1. **المنفذ الافتراضي**: PostgreSQL يعمل على المنفذ 5432
2. **المستخدم الافتراضي**: postgres
3. **قاعدة البيانات**: flywaydb (يجب إنشاؤها أولاً)
4. **كلمة المرور**: يجب أن تطابق ما في application.properties

## استكشاف الأخطاء

### خطأ: "Connection refused"
- تأكد أن PostgreSQL يعمل
- تحقق من المنفذ 5432

### خطأ: "Database does not exist"
- أنشئ قاعدة البيانات `flywaydb` أولاً

### خطأ: "Password authentication failed"
- تحقق من كلمة المرور في application.properties
