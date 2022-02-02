package com.example.demo.Import.entity;

import com.example.demo.Import.util.ApiConstants;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = ApiConstants.PRODUCT_IMPORT_HISTORY)
public class ProductImportHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private String fileName;
  private Integer batchId;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
}
