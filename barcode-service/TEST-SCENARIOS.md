# Barcode Service Test Senaryoları

## Amaç ve strateji

Barkod üretiminde iş kuralları ile biçim üretimi ayrıştırılır. `BarcodeEligibilityPolicy` izin kontrolünü, Strategy implementasyonları kod formatını, `BarcodeService` ise sequence, kalıcılık ve event yayınlama akışını test eder.

## Otomatik senaryolar

| ID | Bileşen | Senaryo | Beklenen sonuç |
|---|---|---|---|
| B-01 | Strategy | `PRODUCT` için sequence değeri verilir | 9 haneli barkod üretilir |
| B-02 | Strategy | `SCALE` için `ME001` ve sequence verilir | Ürün kodu + 3 haneli sequence formatı üretilir |
| B-03 | Strategy | `CASE` için sequence verilir | 4 haneli barkod üretilir |
| B-04 | Strategy | Sequence tek ve çok haneli sınır değerlerdedir | Sol sıfır doldurma kuralları korunur |
| B-05 | Policy | Meyve + kilogram için `PRODUCT` veya `CASE` istenir | Doğrulama başarılı olur |
| B-06 | Policy | Balık + kilogram için `SCALE` istenir | Doğrulama başarılı olur |
| B-07 | Policy | Balık + adet için `CASE` istenir | Doğrulama başarılı olur |
| B-08 | Policy | Et için `PRODUCT` veya balık + kilogram için `CASE` istenir | Barkod tipi reddedilir |
| B-09 | Policy | Ürün kodu 5 karakter değildir veya kategoriyle başlamaz | Girdi reddedilir |
| B-10 | Service | Geçerli istekle barkod üretilir | Kayıt oluşturulur ve event yayınlanır |
| B-11 | Service | Aynı ürün ve tip tekrar istenir | İkinci barkod engellenir |
| B-12 | OpenAPI | `/v3/api-docs` ve `/swagger-ui.html` açılır | Barcode API ve request alanları görüntülenir |

## Strategy genişletme kontrol listesi

Yeni bir barkod tipi eklenirken:

1. `BarcodeType` enum değeri eklenir.
2. `BarcodeGenerationStrategy` arayüzünü uygulayan sınıf yazılır.
3. Uygunluk matrisi `BarcodeEligibilityPolicy` içinde güncellenir.
4. `BarcodeGenerationStrategyTest` ve policy testine pozitif/negatif senaryolar eklenir.
5. README ve Swagger açıklamaları güncellenir.

## Çalıştırma

```powershell
./mvnw.cmd test
```

İlgili sınıflar: `BarcodeGenerationStrategyTest`, `BarcodeEligibilityPolicyTest`, `BarcodeServiceTest`, `BarcodeKafkaConfigurationTest`.
