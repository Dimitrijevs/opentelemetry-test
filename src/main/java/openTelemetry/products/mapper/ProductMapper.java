package openTelemetry.products.mapper;

import org.springframework.stereotype.Component;

import openTelemetry.products.dto.ProductRequest;
import openTelemetry.products.dto.ProductResponse;
import openTelemetry.products.model.Product;

@Component
public class ProductMapper {

    public ProductResponse productResponse(Product product) {

        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .updatedAt(product.getUpdatedAt())
                .createdAt(product.getCreatedAt())
                .build();
    }

    public Product product(ProductRequest productRequest) {

        return Product.builder()
                .title(productRequest.getTitle())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();
    }

    public Product updateProduct(Product existingProduct, ProductRequest request) {
        
        existingProduct.setTitle(request.getTitle());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
    
        return existingProduct;
    }
}
