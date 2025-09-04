package openTelemetry.products.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import openTelemetry.products.dto.ProductRequest;
import openTelemetry.products.dto.ProductResponse;
import openTelemetry.products.service.ProductService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> AllProducts() {

        return productService.allProducts();
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody @Validated ProductRequest request) {

        return productService.createProduct(request);
    }

    @PutMapping("/update/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse updateProduct(@PathVariable Integer id, @RequestBody @Validated ProductRequest request) {

        return productService.updateProduct(id, request);
    }

    @PostMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse deleteProduct(@PathVariable Integer id) {

        return productService.deleteProduct(id);
    }
}
