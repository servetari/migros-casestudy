package com.migros.casestudy.barcode.service.impl;

import com.migros.casestudy.barcode.dto.request.GenerateBarcodeRequest;
import com.migros.casestudy.barcode.dto.response.BarcodeResponse;
import com.migros.casestudy.barcode.entity.Barcode;
import com.migros.casestudy.barcode.entity.ProductBarcodeContext;
import com.migros.casestudy.barcode.exception.BarcodeAlreadyExistsException;
import com.migros.casestudy.barcode.exception.BarcodeSequenceExhaustedException;
import com.migros.casestudy.barcode.repository.BarcodeRepository;
import com.migros.casestudy.barcode.service.BarcodeEligibilityPolicy;
import com.migros.casestudy.barcode.service.BarcodeEventPublisher;
import com.migros.casestudy.barcode.service.BarcodeService;
import com.migros.casestudy.barcode.service.BarcodeStrategyFactory;
import com.migros.casestudy.barcode.strategy.BarcodeGenerationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BarcodeServiceImpl implements BarcodeService {
    private final BarcodeRepository barcodeRepository;
    private final BarcodeEligibilityPolicy eligibilityPolicy;
    private final BarcodeStrategyFactory strategyFactory;
    private final BarcodeEventPublisher eventPublisher;

    @Override
    @Transactional
    public BarcodeResponse generate(GenerateBarcodeRequest request) {
        ProductBarcodeContext context = new ProductBarcodeContext(
                request.productId(), request.productCode(), request.categoryCode(),
                request.categoryName(), request.unit()
        );
        eligibilityPolicy.validate(context, request.type());

        if (barcodeRepository.existsByProductIdAndType(request.productId(), request.type())) {
            throw new BarcodeAlreadyExistsException(request.productId(), request.type());
        }

        BarcodeGenerationStrategy strategy = strategyFactory.get(request.type());
        int sequence = barcodeRepository.findTopByTypeOrderBySequenceDesc(request.type())
                .map(barcode -> barcode.getSequence() + 1)
                .orElse(1);
        if (sequence < 1 || sequence > strategy.maxSequence()) {
            throw new BarcodeSequenceExhaustedException(request.type(), strategy.maxSequence());
        }
        String code = strategy.generate(context, sequence);

        Barcode barcode = barcodeRepository.save(Barcode.builder()
                .code(code)
                .type(request.type())
                .productId(request.productId())
                .sequence(sequence)
                .build());
        if (eventPublisher != null) {
            eventPublisher.publish(barcode);
        }

        return new BarcodeResponse(
                barcode.getId(), barcode.getCode(), barcode.getType(), barcode.getProductId()
        );
    }
}