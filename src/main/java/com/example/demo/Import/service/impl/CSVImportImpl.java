package com.example.demo.Import.service.impl;

import com.example.demo.Import.dto.ProductDTO;
import com.example.demo.Import.repository.ProductImportHistoryRepository;
import com.example.demo.Import.service.CSVImport;
import com.example.demo.Import.util.ApiConstants;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CSVImportImpl implements CSVImport {

  @Value("${api.batch_size}")
  private int batchSize;

  @Value("${api.csv.foldername}")
  private String csvfolder;

  @Autowired
  ImportProducts importProducts;

  @Autowired
  ProductImportHistoryRepository productImportHistoryRepository;

  @Autowired
  @Qualifier(ApiConstants.IMPORT_PROCESSOR_THREAD)
  Executor existingThreadPool;


  @Override
  public void processFiles() {
    try {
      AtomicInteger batch = new AtomicInteger();
      AtomicReference<Long> totalTimeInSeconds = new AtomicReference<>(0l);
      final List<File> files = this.getAllFilesFromResource(csvfolder);
      files.forEach(file -> {
//        ProductImportHistory productImportHistory = new ProductImportHistory();
//        productImportHistory.setFileName(file.getName());
//        productImportHistory.setStartAt(Instant.ofEpochMilli(System.currentTimeMillis()).atZone(
//            ZoneId.of("UTC")).toLocalDateTime());
        final CsvToBean<ProductDTO> csvToBean = getCsvToBeanObject(file);
        if (csvToBean == null) {
          return;
        }
        final List<List<ProductDTO>> batchProductDTOs = ListUtils.partition(csvToBean.parse(), batchSize);
        batchProductDTOs.forEach(productDTOS -> {
          CompletableFuture.supplyAsync(
              () -> importProducts.pushProductsData(productDTOS), existingThreadPool).thenAccept(
              totaltime -> {
//                totalTimeInSeconds.getAndSet(totaltime/1000);
                System.out.println(
                    "Total time taken for batch:" + (batch.incrementAndGet()) + " is "
                        + totaltime / 1000);
              });
        });
        csvToBean.getCapturedExceptions().stream().forEach(e -> {
          log.error(
              String.join(", ", "File Name:" + file.getName(),
                  " Inconsistent Data: " + e.getLine()),
              e);
        });
      });
    } catch (final URISyntaxException ex) {
      log.error("Failed to process csv files: " + ex.getMessage());
    } catch (final IOException ex) {
      log.error("Failed to process csv files: " + ex.getMessage());
    }
  }

  private CsvToBean<ProductDTO> getCsvToBeanObject(File file) {
    CsvToBeanBuilder<ProductDTO> beanBuilder = null;
    try {
      beanBuilder = new CsvToBeanBuilder<>(
          new InputStreamReader(new FileInputStream(file)));
    } catch (FileNotFoundException e) {
      log.error("Failed to process the file: " + String.join("" + file.getName()), e);
      return null;
    }
    final CsvToBean<ProductDTO> csvToBean = beanBuilder.withType(ProductDTO.class)
        .withThrowExceptions(Boolean.FALSE).withMappingStrategy(getHeaderMappingStrategy()).build();
    return csvToBean;
  }

  private HeaderColumnNameTranslateMappingStrategy getHeaderMappingStrategy() {
    final Map<String, String> columnMappings = Stream.of(new String[][]{
        {"product_id", "productId"},
        {"price", "price"},
        {"stock", "stock"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
    final HeaderColumnNameTranslateMappingStrategy mappingStrategy =
        new HeaderColumnNameTranslateMappingStrategy();
    mappingStrategy.setColumnMapping(columnMappings);
    mappingStrategy.setType(ProductDTO.class);
    return mappingStrategy;
  }

  private List<File> getAllFilesFromResource(final String folder)
      throws URISyntaxException, IOException {
    final ClassLoader classLoader = getClass().getClassLoader();
    final URL resource = classLoader.getResource(folder);
    return Files.walk(Paths.get(resource.toURI()))
        .filter(Files::isRegularFile)
        .map(x -> x.toFile())
        .collect(Collectors.toList());
  }
}
