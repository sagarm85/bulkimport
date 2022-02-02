package com.example.demo.Import.service.impl;

import com.example.demo.Import.dto.ProductDTO;
import com.example.demo.Import.entity.Product;
import com.example.demo.Import.mapper.ProductMapper;
import com.example.demo.Import.repository.ProductRepository;
import java.util.List;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ImportProducts {

  @Value("${api.insert_batch_size}")
  private int insertBatchSize;

  @Autowired
  ProductRepository productRepository;

  public Long pushProductsData(final List<ProductDTO> batchProducts) {
    long startTime = System.currentTimeMillis();
    final List<List<ProductDTO>> insertBatchProducts = ListUtils.partition(batchProducts,
        insertBatchSize);
    insertBatchProducts.forEach(products -> {
      this.saveAll(ProductMapper.INSTANCE.mapToProduct(products));
//      products.clear();
    });
//    insertBatchProducts.clear();
    return System.currentTimeMillis() - startTime;
  }

  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  private void saveAll(final List<Product> productData) {
    productRepository.saveAllAndFlush(productData);
  }
}
