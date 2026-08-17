# Category Service

Ürün servisinin kullandığı kategori lookup verisini sunan Spring Boot mikroservisidir. Veri kaynağı şu anda sabit in-memory repository’dir; kategori ekleme veya güncelleme endpoint’i bulunmaz.

## Çalıştırma

Gereksinimler: Java `21` ve Maven Wrapper.

```powershell
./mvnw.cmd spring-boot:run
```

Servis `dev` profilinde `8083` portunda başlar. JAR üretip çalıştırmak için:

```powershell
./mvnw.cmd clean package
java -jar target/category-service-0.0.1-SNAPSHOT.jar
```

## API

Base URL: `http://localhost:8083`

Swagger UI: `http://localhost:8083/swagger-ui.html`  
OpenAPI JSON: `http://localhost:8083/v3/api-docs`

| Method | Endpoint | Başarı yanıtı |
|---|---|---|
| `GET` | `/api/v1/categories` | `200 OK`, kategori listesi |
| `GET` | `/api/v1/categories/{code}` | `200 OK`, tek kategori |

Örnek:

```powershell
curl http://localhost:8083/api/v1/categories
curl http://localhost:8083/api/v1/categories/ME
```

Hazır kategoriler:

| Kod | Ad |
|---|---|
| `BK` | Bakliyat |
| `ME` | Meyve |
| `ET` | Et |
| `IC` | İçecek |
| `BL` | Balık |

Kategori kodları aranırken büyük/küçük harf normalize edilir. Bulunamayan kategori için standart hata yanıtı döner.

## Konfigürasyon

`src/main/resources/application.yaml` varsayılan olarak `dev` profilini seçer. Profil ve port değiştirmek için:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
./mvnw.cmd spring-boot:run
```

Docker profili de `8083` portunu kullanır.

## Test

```powershell
./mvnw.cmd test
```

Testler controller ve service katmanlarında başarılı lookup, normalize edilmiş kod araması ve bulunamayan kategori hatasını kapsar. Ayrıntılı senaryolar için [`TEST-SCENARIOS.md`](TEST-SCENARIOS.md) dosyasına bakın.

## Altyapı ve tasarım

Kategori servisi küçük ve bağımsız bir lookup servisidir. `CategoryController` HTTP sözleşmesini, `CategoryService` iş akışını, `CategoryRepository` veri erişim soyutlamasını, `InMemoryCategoryRepository` ise mevcut sabit veri kaynağını sağlar; `CategoryMapper` entity ile response DTO arasındaki dönüşümü yapar.

Uygulanan OOP ve tasarım ilkeleri:

- **Abstraction:** `CategoryService` ve `CategoryRepository` arayüzleri uygulama ayrıntılarını gizler.
- **Polymorphism:** Repository arayüzü sayesinde in-memory kaynak ileride PostgreSQL veya başka bir kaynakla değiştirilebilir.
- **Encapsulation:** Kategori verisi entity içinde, dış sözleşme ise `CategoryResponse` içinde tutulur.
- **Single Responsibility:** Controller, service, repository, mapper ve exception handler ayrıştırılmıştır.
- **Dependency Inversion:** Controller servise, servis repository soyutlamasına constructor injection ile bağlanır.

## Sağlık ve sınırlar

Bu servis şu anda yazma endpoint’i sunmaz; kategori listesi uygulama başlangıcında sabit veri olarak yüklenir. Product Service’in çalışabilmesi için yerel akışta önce veya Compose ile birlikte `8083` portunda hazır olmalıdır.

## Docker

Bu servisin Dockerfile’ı hazır JAR dosyasını `target/*.jar` konumundan kopyalar. Bu nedenle image oluşturmadan önce paketleme yapılmalıdır:

```powershell
./mvnw.cmd clean package -DskipTests
docker build -t b2b-category-service .
docker run --rm -p 8083:8083 b2b-category-service
```

Tüm sistemi çalıştırmak için kök dizindeki `docker-compose.yml` dosyasını kullanın.