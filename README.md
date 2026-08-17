# B2B Ürün ve Barkod Mikroservisleri

Bu proje, B2B web portalı için ürün yönetimi ve ürünlere bağlı barkod üretimini gerçekleştiren Spring Boot mikroservislerinden oluşur. Proje gereksinimleri için [Case Study](<Case Study (1).md>) dokümanına bakabilirsiniz.

## Servisler

| Servis | Sorumluluk | Varsayılan port |
|---|---|---:|
| `category-service` | Sabit kategori lookup verisini sunar | `8083` |
| `barcode-service` | Ürün, terazi ve kasa barkodu üretir | `8082` |
| `product-service` | Ürün CRUD işlemlerini ve barkodların ürüne bağlanmasını yönetir | `8081` |

### Mimari akış

```text
İstemci
   |
   v
product-service -----> category-service
   |
   +------------------> barcode-service -----> barcode PostgreSQL
   |
   +-----------------------------------------> product PostgreSQL
                         |
                         +----> Kafka ----> product-service (barcode event)
```

Ürün oluşturulurken `product-service`, kategori bilgisini `category-service` üzerinden doğrular, ürünü kaydeder ve kategori/birime göre varsayılan barkodu `barcode-service` üzerinden üretir. Varsayılan tip çoğu durumda `PRODUCT`, Et için `SCALE`, Balık + `ADET` için `CASE` olur. Sonraki barkodlar `POST /api/v1/products/{id}/barcodes` endpoint’i ile eklenebilir.

## Teknoloji ve önkoşullar

- Java `21`
- Spring Boot `4.1.0`
- Maven Wrapper
- PostgreSQL `16` (product ve barcode servisleri için)
- Docker ve Docker Compose (opsiyonel)
- Apache Kafka `4.0` (Compose ile otomatik kurulur)

## Projeyi çalıştırma

### Yerel geliştirme

Önce PostgreSQL üzerinde aşağıdaki veritabanlarını oluşturun:

- `productdb` — port `5432`
- `barcodedb` — port `5433`

Ardından servisleri ayrı terminallerde başlatın. Maven Wrapper ilgili servis klasöründen çalıştırılmalıdır:

```powershell
cd category-service
./mvnw.cmd spring-boot:run
```

```powershell
cd barcode-service
./mvnw.cmd spring-boot:run
```

```powershell
cd product-service
./mvnw.cmd spring-boot:run
```

Uygulamalar varsayılan olarak `dev` profilini kullanır. Profil seçmek için `SPRING_PROFILES_ACTIVE` ortam değişkenini ayarlayabilirsiniz.

### Docker Compose ile tüm sistemi çalıştırma

Önce üç servisin JAR dosyalarını üretin. Compose içindeki servis Dockerfile’ları `target/*.jar` beklediği için bu adım gereklidir:

```powershell
cd category-service
./mvnw.cmd clean package -DskipTests
cd ..\barcode-service
./mvnw.cmd clean package -DskipTests
cd ..\product-service
./mvnw.cmd clean package -DskipTests
cd ..
docker compose up --build
```

Servisler hazır olduğunda:

- Product API: `http://localhost:8081`
- Barcode API: `http://localhost:8082`
- Category API: `http://localhost:8083`

Durdurmak için:

```powershell
docker compose down
```

Veritabanı volume’larını da silmek için yalnızca gerektiğinde `docker compose down -v` kullanın.

### Product ve Barcode servislerini ayrı Compose dosyasıyla çalıştırma

`docker-compose.product.yml`, `product-service` ve `barcode-service` ile birlikte PostgreSQL servislerini başlatır. Kategori servisi host üzerinde `8083` portunda çalışıyor olmalıdır; Compose içindeki `CATEGORY_SERVICE_URL` bunu `host.docker.internal:8083` adresine yönlendirir.

```powershell
docker compose -f docker-compose.product.yml up --build
```

Her iki Compose dosyası Kafka’yı tek düğümlü KRaft modunda `kafka` servisi olarak başlatır. Konteynerler `kafka:29092`, host üzerinde çalışan uygulamalar ise `localhost:9092` adresini kullanır. Barkod üretildiğinde `barcode-events` topic’ine olay yayınlanır; Product Service bu olayı tüketerek ürün-barkod ilişkisini idempotent biçimde tamamlar.

## API özeti

### Category API — `8083`

| Method | Endpoint | Açıklama |
|---|---|---|
| `GET` | `/api/v1/categories` | Tüm kategorileri listeler |
| `GET` | `/api/v1/categories/{code}` | Kategori koduna göre lookup yapar |

Hazır kategori kodları: `BK` Bakliyat, `ME` Meyve, `ET` Et, `IC` İçecek, `BL` Balık.

### Product API — `8081`

| Method | Endpoint | Açıklama |
|---|---|---|
| `POST` | `/api/v1/products` | Ürün oluşturur ve kategori/birime göre varsayılan barkodu üretir |
| `GET` | `/api/v1/products` | Ürünleri listeler |
| `GET` | `/api/v1/products/{id}` | Ürün detayını getirir |
| `PUT` | `/api/v1/products/{id}` | Ürünü günceller |
| `DELETE` | `/api/v1/products/{id}` | Ürünü siler |
| `POST` | `/api/v1/products/{id}/barcodes` | Ürüne ek barkod üretir ve bağlar |

Ürün oluşturma/güncelleme gövdesi:

```json
{
  "name": "Elma",
  "code": "ME001",
  "brand": "Migros",
  "unit": "KILOGRAM",
  "categoryCode": "ME"
}
```

Ek barkod gövdesi:

```json
{
  "type": "CASE"
}
```

### Barcode API — `8082`

| Method | Endpoint | Açıklama |
|---|---|---|
| `POST` | `/api/v1/barcodes` | Verilen ürün bağlamı için barkod üretir |

Barcode API gövdesi:

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

Normal kullanımda bu endpoint’i doğrudan çağırmak yerine barkodun ürüne bağlanması için Product API kullanılmalıdır.

## Barkod kuralları

| Kategori ve birim | İzin verilen tipler |
|---|---|
| Meyve + `KILOGRAM` | `PRODUCT`, `CASE` |
| Balık + `KILOGRAM` | `PRODUCT`, `SCALE` |
| Balık + `ADET` | `CASE` |
| Et + herhangi birim | `SCALE` |
| Diğer kategoriler | `PRODUCT` |

- Ürün adı ve ürün kodu tekildir.
- Kategori kodu 2, ürün kodu 5 karakterdir; ürün kodu kategori koduyla başlamalıdır.
- `PRODUCT` barkodu 9 karakterdir.
- `SCALE` barkodu ürün kodunun ilk 5 karakteri ve 3 haneli sequence değerinden oluşur.
- `CASE` barkodu 4 karakterdir.
- Aynı üründe aynı barkod tipinden yalnızca bir tane bulunabilir.

## Konfigürasyon

Her servis `application.yaml` üzerinden aktif profili belirler:

```text
SPRING_PROFILES_ACTIVE=dev
```

Yerel ve Docker profillerindeki veritabanı/servis adresleri aşağıdaki ortam değişkenleriyle değiştirilebilir:

- `PRODUCT_DB_URL`, `PRODUCT_DB_USERNAME`, `PRODUCT_DB_PASSWORD`
- `BARCODE_DB_URL`, `BARCODE_DB_USERNAME`, `BARCODE_DB_PASSWORD`
- `CATEGORY_SERVICE_URL`
- `BARCODE_SERVICE_URL`
- `KAFKA_BOOTSTRAP_SERVERS` — yerel kullanımda varsayılan `localhost:9092`, Compose içinde `kafka:29092`
- `BARCODE_EVENTS_TOPIC` — varsayılan `barcode-events`

## Test ve paketleme

Her servis bağımsız Maven projesidir:

```powershell
cd category-service
./mvnw.cmd test

cd ..\barcode-service
./mvnw.cmd test

cd ..\product-service
./mvnw.cmd test
```

Paket üretmek için ilgili serviste `./mvnw.cmd clean package` komutunu çalıştırın.

## Proje yapısı

```text
Case-Study/
├── category-service/
├── product-service/
├── barcode-service/
├── docker-compose.yml
├── docker-compose.product.yml
└── Case Study (1).md
```

## Swagger / OpenAPI

Her servis OpenAPI 3 dokümantasyonunu otomatik üretir. Servis çalışırken aşağıdaki adreslerden Swagger UI'ı açabilir veya ham OpenAPI JSON'unu alabilirsiniz:

| Servis | Swagger UI | OpenAPI JSON |
|---|---|---|
| Product | `http://localhost:8081/swagger-ui.html` | `http://localhost:8081/v3/api-docs` |
| Barcode | `http://localhost:8082/swagger-ui.html` | `http://localhost:8082/v3/api-docs` |
| Category | `http://localhost:8083/swagger-ui.html` | `http://localhost:8083/v3/api-docs` |

Swagger açıklamaları controller seviyesinde tutulur; `@Tag` servis kaynaklarını, `@Operation` endpoint davranışını açıklar. Request DTO’larında bulunan Jakarta Validation kuralları da OpenAPI şemasına yansıtılır.

## Altyapı ve OOP yaklaşımı

Proje üç bağımsız Maven/Spring Boot uygulamasından oluşan bir mikroservis mimarisidir. Her servis controller, service, repository, mapper/DTO ve exception handler sınırlarıyla katmanlı yapı kullanır. Product Service, Category ve Barcode servisleriyle HTTP üzerinden haberleşir; Barcode Service barkod olayını Kafka’ya yayınlar, Product Service olayı tüketerek ilişkileri tamamlar. PostgreSQL verileri servis bazında ayrıdır; Category Service mevcut durumda sabit in-memory veri sağlar.

Kodda kullanılan temel OOP ve tasarım yaklaşımları:

- **Encapsulation:** Entity ve DTO sınırlarıyla veri erişimi kontrol edilir.
- **Abstraction:** Service, repository ve barkod strategy interface’leri uygulama ayrıntılarını saklar.
- **Polymorphism:** Barkod türleri aynı `BarcodeGenerationStrategy` sözleşmesiyle farklı biçimde üretilir.
- **Strategy + Factory:** Barkod formatları bağımsız Strategy sınıflarında, seçim `BarcodeStrategyFactory` içinde tutulur.
- **Single Responsibility:** Validation, mapping, dış servis client’ları, event publisher/listener ve hata yönetimi ayrıdır.
- **Dependency Inversion:** Bağımlılıklar constructor injection ile sağlanır.

## Test stratejisi ve senaryolar

Her servisin bağımsız unit/controller testleri vardır; Product ve Barcode tarafında gerekli testler Testcontainers/H2 ile veri erişimini de doğrular. Test senaryoları ve manuel entegrasyon akışları şu dosyalardadır:

- [`product-service/TEST-SCENARIOS.md`](product-service/TEST-SCENARIOS.md)
- [`barcode-service/TEST-SCENARIOS.md`](barcode-service/TEST-SCENARIOS.md)
- [`category-service/TEST-SCENARIOS.md`](category-service/TEST-SCENARIOS.md)

Tüm testleri çalıştırmak için her servis klasöründe `./mvnw.cmd test` komutu kullanılmalıdır. Uygulama entegrasyonunu doğrulamak için önce `docker compose up --build -d`, ardından Compose servislerinin health durumunu ve Swagger endpoint’lerini kontrol edin.

Her servis için endpoint, yapılandırma ve çalıştırma ayrıntıları ilgili klasördeki README dosyasında bulunur.