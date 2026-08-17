package com.migros.casestudy.barcode.service;

import com.migros.casestudy.barcode.entity.enums.BarcodeType;
import com.migros.casestudy.barcode.exception.BarcodeStrategyNotFoundException;
import com.migros.casestudy.barcode.strategy.BarcodeGenerationStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class BarcodeStrategyFactory {
    private final Map<BarcodeType, BarcodeGenerationStrategy> strategies;

    public BarcodeStrategyFactory(List<BarcodeGenerationStrategy> strategyList) {
        this.strategies = new EnumMap<>(BarcodeType.class);
        strategyList.forEach(strategy -> strategies.put(strategy.supports(), strategy));
    }

    public BarcodeGenerationStrategy get(BarcodeType type) {
        BarcodeGenerationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new BarcodeStrategyNotFoundException(type);
        }
        return strategy;
    }
}