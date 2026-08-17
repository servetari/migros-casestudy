# Category Service Test Senaryoları

## Amaç ve strateji

Bu servis için testler küçük ve hızlı tutulur: service unit testleri lookup davranışını, controller testleri HTTP sözleşmesini, uygulama testi ise Spring context'inin ayağa kalkmasını doğrular. Veri kaynağı in-memory olduğu için harici veritabanı gerekmemektedir.

## Otomatik senaryolar

| ID | Katman | Senaryo | Beklenen sonuç |
|---|---|---|---|
| C-01 | Service | Tüm kategoriler istenir | BK, ME, ET, IC ve BL listelenir |
| C-02 | Service | `ME` kodu ile arama yapılır | Meyve kategorisi döner |
| C-03 | Service | `me` gibi küçük harfli kod gönderilir | Kod normalize edilerek aynı kategori döner |
| C-04 | Service | Bilinmeyen kod gönderilir | `NotFoundException` oluşur |
| C-05 | Controller | `GET /api/v1/categories` çağrılır | `200 OK` ve kategori listesi döner |
| C-06 | Controller | `GET /api/v1/categories/ME` çağrılır | `200 OK` ve tek kategori döner |
| C-07 | Controller | Olmayan kategori çağrılır | Standart hata yanıtı döner |
| C-08 | OpenAPI | `/v3/api-docs` ve `/swagger-ui.html` açılır | Category API operasyonları görüntülenir |

## Çalıştırma

```powershell
./mvnw.cmd test
```

İlgili sınıflar: `CategoryControllerTest`, `CategoryServiceImplTest` ve `CategoryServiceApplicationTests`.

## Manuel doğrulama

```powershell
curl http://localhost:8083/api/v1/categories
curl http://localhost:8083/api/v1/categories/me
curl http://localhost:8083/api/v1/categories/XX
```

İlk iki çağrının başarılı, son çağrının standart hata gövdesiyle sonuçlandığı ve Swagger UI'da iki GET operasyonunun listelendiği kontrol edilir.
