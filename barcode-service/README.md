# Barcode Service

Ürünler için `PRODUCT`, `SCALE` ve `CASE` barkodları üreten Spring Boot mikroservisidir. Barkod üretimi Strategy yapısı ile tip bazında ayrılmış, uygunluk kontrolleri ise merkezi policy üzerinden uygulanmıştır. Başarılı üretimlerden sonra `barcode-events` Kafka topic’ine olay yayınlar.

## Çalıştırma

Gereksinimler: Java `21`, Maven Wrapper ve PostgreSQL.

Yerel profil varsayılan olarak `localhost:5433/barcodedb` veritabanını kullanır:

```powershell
./mvnw.cmd spring-boot:run
```

Servis `8082` portunda başlar. JAR üretip çalıştırmak için:

```powershell
./mvnw.cmd clean package
java -jar target/barcode-service-0.0.1-SNAPSHOT.jar
```

## API

Base URL: `http://localhost:8082`

Swagger UI: `http://localhost:8082/swagger-ui.html`  
OpenAPI JSON: `http://localhost:8082/v3/api-docs`

| Method | Endpoint | Başarı yanıtı |
|---|---|---|
| `POST` | `/api/v1/barcodes` | `201 Created`, üretilen barkod |

İstek gövdesi:

```json
{
  "productId": 1,
  "productCode": "ME001",
  "categoryCode": "ME",
  "categoryName": "Meyve",
  "unit": "KILOGRAM",
  "type": "PRODUCT"
}
```

Yanıt örneği:

```json
{
  "id": 1,
  "code": "000000001",
  "type": "PRODUCT",
  "productId": 1
}
```

Bu endpoint ürün bağlamını kendisi sorgulamaz; gönderilen ürün/kategori alanlarını doğrular. Normal akışta çağrı Product Service tarafından yapılır.

## Barkod kuralları

| Tip | Üretim kuralı |
|---|---|
| `PRODUCT` | 9 haneli sequence değeri |
| `SCALE` | 5 karakterlik ürün kodu + 3 haneli sequence |
| `CASE` | 4 haneli sequence değeri |

İzin verilen kombinasyonlar:

| Kategori ve birim | Tipler |
|---|---|
| Meyve + `KILOGRAM` | `PRODUCT`, `CASE` |
| Balık + `KILOGRAM` | `PRODUCT`, `SCALE` |
| Balık + `ADET` | `CASE` |
| Et | `SCALE` |
| Diğer kategoriler | `PRODUCT` |

Kategori kodu 2, ürün kodu 5 alfanümerik karakter olmalı ve ürün kodu kategori koduyla başlamalıdır. Aynı ürün ve barkod tipi için ikinci üretim engellenir. Sequence değerleri barkod tipine göre veritabanından devam ettirilir.

## Konfigürasyon

Varsayılan `dev` profilinde:

- Port: `8082`
- PostgreSQL: `jdbc:postgresql://localhost:5433/barcodedb`
- Hibernate `ddl-auto`: `update`

Ortam değişkenleri:

- `BARCODE_DB_URL`
- `BARCODE_DB_USERNAME`
- `BARCODE_DB_PASSWORD`
- `SPRING_PROFILES_ACTIVE`
- `KAFKA_BOOTSTRAP_SERVERS` — yerel varsayılan `localhost:9092`, Docker varsayılan `kafka:29092`
- `BARCODE_EVENTS_TOPIC` — varsayılan `barcode-events`

Docker profilinde veritabanı adresi varsayılan olarak `jdbc:postgresql://barcode-db:5432/barcodedb` olur.

## Test

```powershell
./mvnw.cmd test
```

Test kapsamı eligibility policy, barkod servisi ve üretim stratejilerini içerir.

## Altyapı ve tasarım

İstek `BarcodeController` üzerinden alınır, `BarcodeService` ürün bağlamını doğrular, `BarcodeEligibilityPolicy` kategori-birim-barkod uyumluluğunu kontrol eder ve `BarcodeStrategyFactory` uygun üretim stratejisini seçer. Üretilen değer PostgreSQL’e yazılır ve `BarcodeEventPublisher` ile Kafka’daki `barcode-events` topic’ine yayınlanır.

Uygulanan OOP ve tasarım ilkeleri:

- **Strategy Pattern:** `ProductBarcodeGenerationStrategy`, `ScaleBarcodeGenerationStrategy` ve `CaseBarcodeGenerationStrategy` aynı `BarcodeGenerationStrategy` sözleşmesini uygular.
- **Factory Pattern:** `BarcodeStrategyFactory`, barkod tipine göre doğru Strategy nesnesini seçer; servis `switch`/tip ayrıntılarına bağlanmaz.
- **Policy Object:** `BarcodeEligibilityPolicy`, değişken kategori-birim kurallarını üretim akışından ayırır.
- **Abstraction ve Polymorphism:** Yeni bir barkod tipi, mevcut sözleşmeyi uygulayan yeni bir Strategy eklenerek genişletilebilir.
- **Dependency Inversion ve SRP:** Constructor injection kullanılır; controller, servis, policy, repository ve event publisher tek sorumluluk taşır.

## Test stratejisi

Strategy testleri her barkod biçimini ve sequence sınırlarını, policy testleri izin verilen ve reddedilen kombinasyonları, servis testleri kayıt/tekrar üretim davranışını doğrular. Ayrıntılı senaryolar için [`TEST-SCENARIOS.md`](TEST-SCENARIOS.md) dosyasına bakın.

## Docker

Barcode Service Dockerfile’ı host üzerinde üretilen hazır JAR dosyasını kullanır. Image oluşturmadan önce paketleme yapılmalıdır:

```powershell
docker build -t b2b-barcode-service .
docker run --rm -p 8082:8082 `
  -e SPRING_PROFILES_ACTIVE=dev `
  -e BARCODE_DB_URL=jdbc:postgresql://host.docker.internal:5433/barcodedb `
  b2b-barcode-service
```

Kafka ve üç servisi iki PostgreSQL veritabanıyla birlikte çalıştırmak için kök dizindeki `docker-compose.yml` dosyasını kullanın. Barkod üretim olayının örnek payload’ı şöyledir:

```json
{
  "barcodeId": 1,
  "code": "000000001",
  "type": "PRODUCT",
  "productId": 1
}
```