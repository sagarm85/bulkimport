package com.example.demo.Import.service.impl;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
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
      final List<File> files = this.getAllFilesFromResource(csvfolder);
      files.forEach(file -> {
        final CsvToBean<ProductDTO> csvToBean = getCsvToBeanObject(file);
        if (csvToBean == null) {
          return;
        }
        final List<List<ProductDTO>> batchProductDTOs = ListUtils.partition(csvToBean.parse(),
            batchSize);
        try {
          batchProductDTOs.stream()
              .map(productDTOS -> CompletableFuture.supplyAsync(
                  () -> importProducts.pushProductsData(productDTOS), existingThreadPool))
              .collect(collectingAndThen(toList(), l -> allOfOrException(l)))
              .thenAccept(timerList -> {
                timerList.stream()
                    .forEach(time1 -> log.info("Times in seconds: " + time1 / 1000));
              }).get();
        } catch (final InterruptedException e) {
          e.printStackTrace();
        } catch (final ExecutionException e) {
          e.printStackTrace();
        }

//        batchProductDTOs.forEach(productDTOS -> {
//          CompletableFuture.supplyAsync(
//              () -> importProducts.pushProductsData(productDTOS), existingThreadPool).thenAccept(
//              totaltime -> {
////                totalTimeInSeconds.getAndSet(totaltime/1000);
//                System.out.println(
//                    "Total time taken for task is: "
//                        + totaltime / 1000);
//              });
//        });

        csvToBean.getCapturedExceptions().stream().forEach(e -> {
          log.error(
              String.join(", ", "File Name:" + file.getName(),
                  ", Inconsistent Data: " + String.join(",", e.getLine())));
        });
      });
    } catch (final URISyntaxException ex) {
      log.error("Failed to process csv files: " + ex.getMessage());
    } catch (final IOException ex) {
      log.error("Failed to process csv files: " + ex.getMessage());
    }
  }

//  private <T, R> CompletableFuture<List<R>> inParallelBatching(
//      List<T> source, Function<T, R> mapper, Executor executor, int batches) {
//
//    return BatchingStream.partitioned(source, batches)
//        .map(batch -> supplyAsync(
//            () -> batching(mapper).apply(batch), executor))
//        .collect(collectingAndThen(toList(), l -> allOfOrException(l)))
//        .thenApply(list -> {
//          List<R> result = new ArrayList<>(source.size());
//          for (List<R> rs : list) {
//            result.addAll(rs);
//          }
//          return result;
//        });
//  }

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
        .collect(toList());
  }

  static <T> CompletableFuture<List<T>> allOfOrException(Collection<CompletableFuture<T>> futures) {
    CompletableFuture<List<T>> result = futures.stream()
        .collect(collectingAndThen(
            toList(),
            l -> CompletableFuture.allOf(l.toArray(new CompletableFuture[0]))
                .thenApply(__ -> l.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList()))));

    for (CompletableFuture<?> f : futures) {
      f.handle((__, ex) -> ex == null
          || result.completeExceptionally(ex));
    }

    return result;
  }
}
