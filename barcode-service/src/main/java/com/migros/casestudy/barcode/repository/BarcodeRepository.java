package com.migros.casestudy.barcode.repository;

import com.migros.casestudy.barcode.entity.Barcode;
import com.migros.casestudy.barcode.entity.enums.BarcodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.Optional;

@Repository
public interface BarcodeRepository extends JpaRepository<Barcode, Long> {
    boolean existsByProductIdAndType(Long productId, BarcodeType type);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Barcode> findTopByTypeOrderBySequenceDesc(BarcodeType type);
}