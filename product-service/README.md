# Product Service

Ürünlerin oluşturulması, okunması, güncellenmesi, silinmesi ve barkodlarının yönetilmesinden sorumlu Spring Boot mikroservisidir. Ürün verileri PostgreSQL’de tutulur; kategori ve barkod bilgileri diğer servislerden alınır. Product Service, barkod üretim olaylarını Kafka üzerinden de tüketerek ilişkiyi idempotent biçimde uzlaştırır.

## Çalıştırma

Gereksinimler: Java `21`, Maven Wrapper ve PostgreSQL.

Yerel profil varsayılan olarak `localhost:5432/productdb` veritabanını kullanır. Veritabanı hazırlandıktan sonra:

```powershell
./mvnw.cmd spring-boot:run
```

Servis `8081` portunda başlar ve ürün işlemleri sırasında aşağıdaki servislerle iletişim kurar:

- Category Service: `http://localhost:8083`
- Barcode Service: `http://localhost:8082`

JAR üretip çalıştırmak için:

```powershell
./mvnw.cmd clean package
java -jar target/product-service-0.0.1-SNAPSHOT.jar
```

## API

Base URL: `http://localhost:8081`

Swagger UI: `http://localhost:8081/swagger-ui.html`  
OpenAPI JSON: `http://localhost:8081/v3/api-docs`

| Method | Endpoint | Açıklama |
|---|---|---|
| `POST` | `/api/v1/products` | Ürün oluşturur ve kategori/birime göre varsayılan barkodu bağlar |
| `GET` | `/api/v1/products` | Tüm ürünleri listeler |
| `GET` | `/api/v1/products/{id}` | Ürünü ID ile getirir |
| `PUT` | `/api/v1/products/{id}` | Ürün bilgilerini günceller |
| `DELETE` | `/api/v1/products/{id}` | Ürünü siler |
| `POST` | `/api/v1/products/{id}/barcodes` | Ürüne uygun ek barkod ekler |

### Ürün oluşturma ve güncelleme

`POST` ve `PUT` isteklerinde kullanılan gövde:

```json
{
  "name": "Elma",
  "code": "ME001",
  "brand": "Migros",
  "unit": "KILOGRAM",
  "categoryCode": "ME"
}
```

```powershell
curl -X POST http://localhost:8081/api/v1/products `
  -H "Content-Type: application/json" `
  -d '{"name":"Elma","code":"ME001","brand":"Migros","unit":"KILOGRAM","categoryCode":"ME"}'
```

Ürün kodu 5 karakter olmalı ve ilk 2 karakteri kategori koduyla eşleşmelidir. Ürün adı ve kodu tekil, marka/birim/kategori ise zorunludur.

### Barkod ekleme

```powershell
curl -X POST http://localhost:8081/api/v1/products/1/barcodes `
  -H "Content-Type: application/json" `
  -d '{"type":"CASE"}'
```

Bir ürün aynı tipte birden fazla barkoda sahip olamaz. Barkod tipi kategori ve birim kurallarına uymuyorsa istek reddedilir.

## Barkod uygunluk matrisi

| Kategori ve birim | İzin verilen barkodlar |
|---|---|
| Meyve + `KILOGRAM` | `PRODUCT`, `CASE` |
| Balık + `KILOGRAM` | `PRODUCT`, `SCALE` |
| Balık + `ADET` | `CASE` |
| Et | `SCALE` |
| Diğer kategoriler | `PRODUCT` |

Ürün oluşturulduğunda Product Service, Barcode Service’e ürün bağlamını gönderir ve dönen barkod kodunu ürün kaydına ekler. Varsayılan barkod çoğu ürün için `PRODUCT`, Et için `SCALE`, Balık + `ADET` için `CASE` tipindedir. Barcode Service’e doğrudan çağrı yapmak yerine ürün-barkod ilişkisinin tutarlı kalması için bu endpoint tercih edilmelidir.

## Konfigürasyon

Varsayılan `dev` profilinde:

- Port: `8081`
- PostgreSQL: `jdbc:postgresql://localhost:5432/productdb`
- Category Service: `http://localhost:8083`
- Barcode Service: `http://localhost:8082`

Docker profilinde adresleri değiştirmek için şu ortam değişkenleri kullanılabilir:

- `PRODUCT_DB_URL`, `PRODUCT_DB_USERNAME`, `PRODUCT_DB_PASSWORD`
- `CATEGORY_SERVICE_URL`
- `BARCODE_SERVICE_URL`
- `KAFKA_BOOTSTRAP_SERVERS` — yerel varsayılan `localhost:9092`, Docker varsayılan `kafka:29092`
- `KAFKA_CONSUMER_GROUP` — varsayılan `product-service`
- `BARCODE_EVENTS_TOPIC` — varsayılan `barcode-events`

```powershell
$env:SPRING_PROFILES_ACTIVE="docker"
./mvnw.cmd spring-boot:run
```

## Test

```powershell
./mvnw.cmd test
```

Testler validator, service ve controller katmanlarını kapsar. PostgreSQL/Testcontainers gerektiren testler için Docker çalışma ortamının erişilebilir olduğundan emin olun.

## Altyapı ve tasarım

Servis; HTTP için `controller`, iş kuralları için `service`, kalıcılık için `repository`, veri taşıma için `dto` ve dış servis erişimi için `client` katmanlarından oluşur. PostgreSQL ürün ve ürün-barkod ilişkilerini saklar; `CategoryClient` kategori doğrulaması, `BarcodeClient` barkod üretimi için kullanılır. Kafka barkod olayları `BarcodeEventListener` tarafından tüketilerek ilişki idempotent biçimde tamamlanır.

Uygulanan OOP ve tasarım ilkeleri:

- **Encapsulation:** Entity alanları ve değişiklikleri servis/metot sınırları içinde tutulur.
- **Abstraction:** `ProductService`, repository ve client arayüzleri altyapı ayrıntılarını gizler.
- **Dependency Inversion:** Constructor injection ile katmanlar somut bağımlılıklara doğrudan bağlanmaz.
- **Single Responsibility:** Validator, mapper, client, event listener ve exception handler ayrı sorumluluklara sahiptir.
- **Layered Architecture:** HTTP, iş kuralı, veri erişimi ve entegrasyon sınırları ayrılmıştır.

## Test stratejisi

Unit testlerde validator ve servis iş kuralları izole edilir; controller testlerinde HTTP sözleşmesi ve hata yanıtları, repository testlerinde PostgreSQL/Testcontainers davranışı ve event testlerinde Kafka olayının idempotent bağlanması doğrulanır. Ayrıntılı senaryolar için [`TEST-SCENARIOS.md`](TEST-SCENARIOS.md) dosyasına bakın.

## Docker Compose

Kök dizinden tüm sistemi başlatmak için `docker-compose.yml`, product ve barcode servislerini kategori servisi host üzerinde çalışırken başlatmak için `docker-compose.product.yml` kullanılabilir. Her iki dosya Kafka’yı KRaft modunda başlatır ve servisleri `kafka:29092` adresine bağlar. Ayrıntılı kurulum için kök `README.md` dosyasına bakın.