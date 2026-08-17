opl# Product Service Test Senaryoları

## Amaç ve strateji

Test piramidi; hızlı unit testler, controller sözleşme testleri, repository/Testcontainers testleri ve Kafka olay tüketimi testlerinden oluşur. Her test izole bir iş kuralını doğrular; dış servisler mock'lanır, veritabanı davranışı gereken senaryolarda gerçek PostgreSQL container'ı kullanılır.

## Otomatik senaryolar

| ID | Katman | Senaryo | Beklenen sonuç |
|---|---|---|---|
| P-01 | Validator | Geçerli ürün adı, kodu, marka, birim ve kategori gönderilir | Doğrulama başarılı olur |
| P-02 | Validator | Ürün kodu 5 karakter değil veya kategori koduyla başlamıyor | İş kuralı hatası döner |
| P-03 | Validator | Zorunlu alanlardan biri boş bırakılır | Validation hatası oluşur |
| P-04 | Service | Kategori client geçerli kategori döndürür ve yeni ürün oluşturulur | Ürün kaydedilir, varsayılan barkod bağlanır |
| P-05 | Service | Aynı ürün adı veya kodu tekrar gönderilir | Duplicate iş hatası döner |
| P-06 | Service | Olmayan ürün ID'si ile getirme/güncelleme/silme yapılır | `NotFoundException` oluşur |
| P-07 | Controller | Geçerli `POST /api/v1/products` isteği yapılır | `201 Created` ve ürün response'u döner |
| P-08 | Controller | Geçersiz JSON veya validation alanı gönderilir | Standart hata gövdesi döner |
| P-09 | Controller | Ürün barkoduna izin verilmeyen tip gönderilir | `400 Bad Request` döner |
| P-10 | Event | Aynı `BarcodeGeneratedEvent` birden fazla kez tüketilir | Ürün-barkod ilişkisi tek kez oluşturulur |
| P-11 | OpenAPI | `/v3/api-docs` ve `/swagger-ui.html` açılır | Product API operasyonları görüntülenir |

## Çalıştırma

```powershell
./mvnw.cmd test
```

İlgili sınıflar: `ProductValidatorTest`, `ProductServiceImplTest`, `ProductControllerTest`, `ProductRepositoryImplTest`, `BarcodeEventListenerTest` ve `ProductServiceApplicationTests`.

## Manuel entegrasyon akışı

1. Kök dizinden `docker compose up --build -d` komutunu çalıştırın.
2. `http://localhost:8083/api/v1/categories/ME` ile kategori servisinin hazır olduğunu doğrulayın.
3. Swagger UI üzerinden veya API ile `ME001` kodlu `KILOGRAM` ürün oluşturun.
4. Product response'unda varsayılan `PRODUCT` barkodunu kontrol edin.
5. `/api/v1/products/{id}/barcodes` üzerinden `CASE` barkodu ekleyin.
6. Aynı tipte ikinci barkod isteğinin reddedildiğini ve Swagger tanımının güncel olduğunu kontrol edin.
