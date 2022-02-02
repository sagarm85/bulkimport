package com.example.demo.Import.entity;

import com.example.demo.Import.util.ApiConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = ApiConstants.PRODUCT_TABLE)
@JsonIgnoreProperties(
    value = {"updated_at"},
    allowGetters = true
)
public class Product {
  @Id
  private Long productId;

  @Column
  private Double price;

  @Column
  private Double stock;

  @Column(name = "updated_at", nullable = true)
  @Temporal(TemporalType.TIMESTAMP)
  @LastModifiedDate
  private Date updatedAt;

  @PrePersist
  public void prePersist() {
    this.updatedAt = new Date();
  }

  @PostPersist
  public void postPersist() {
    this.updatedAt = new Date();
  }
}
