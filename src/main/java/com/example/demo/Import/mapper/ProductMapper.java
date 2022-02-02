package com.example.demo.Import.mapper;

import com.example.demo.Import.dto.ProductDTO;
import com.example.demo.Import.entity.Product;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {
  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  @Mappings({
      @Mapping(target = "updatedAt", ignore = true)})
  Product mapToProduct(final ProductDTO request);

  List<Product> mapToProduct(final List<ProductDTO> request);
}
