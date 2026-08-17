package com.migros.casestudy.product.event;

import com.migros.casestudy.product.entity.Product;
import com.migros.casestudy.product.entity.ProductBarcode;
import com.migros.casestudy.product.repository.ProductRepository;
import com.migros.casestudy.product.validation.ProductBarcodeEligibilityPolicy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BarcodeEventListener {
    private final ProductRepository productRepository;

    public BarcodeEventListener(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @KafkaListener(topics = "${app.kafka.topics.barcode-events}")
    @Transactional
    public void handle(BarcodeGeneratedEvent event) {
        Product product = productRepository.findById(event.productId())
                .orElseThrow(() -> new IllegalStateException(
                        "Barkod olayı için ürün bulunamadı: " + event.productId()
                ));

        if (!ProductBarcodeEligibilityPolicy.isAllowed(product, event.type())) {
            return;
        }

        boolean alreadyLinked = product.getBarcodes().stream()
                .anyMatch(barcode -> barcode.getType() == event.type()
                        || barcode.getCode().equals(event.code()));
        if (!alreadyLinked) {
            product.addBarcode(new ProductBarcode(event.code(), event.type()));
            productRepository.save(product);
        }
    }
}