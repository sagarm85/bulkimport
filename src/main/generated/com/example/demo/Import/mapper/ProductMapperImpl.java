package com.example.demo.Import.mapper;

import com.example.demo.Import.dto.ProductDTO;
import com.example.demo.Import.entity.Product;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-02-02T20:51:29+0800",
    comments = "version: 1.3.0.Final, compiler: javac, environment: Java 11.0.13 (JetBrains s.r.o.)"
)
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product mapToProduct(ProductDTO request) {
        if ( request == null ) {
            return null;
        }

        Product product = new Product();

        product.setProductId( request.getProductId() );
        product.setPrice( request.getPrice() );
        product.setStock( request.getStock() );

        return product;
    }

    @Override
    public List<Product> mapToProduct(List<ProductDTO> request) {
        if ( request == null ) {
            return null;
        }

        List<Product> list = new ArrayList<Product>( request.size() );
        for ( ProductDTO productDTO : request ) {
            list.add( mapToProduct( productDTO ) );
        }

        return list;
    }
}
