package com.example.demo.Import.repository;

import com.example.demo.Import.entity.ProductImportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImportHistoryRepository extends
    JpaRepository<ProductImportHistory, Integer> {

}
