package openTelemetry.products.service;

import java.util.List;

import org.springframework.stereotype.Service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.persistence.EntityNotFoundException;
import openTelemetry.products.dto.ProductRequest;
import openTelemetry.products.dto.ProductResponse;
import openTelemetry.products.mapper.ProductMapper;
import openTelemetry.products.model.Product;
import openTelemetry.products.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    // define class fields
    private static final String INSTRUMENTATION_NAME = ProductService.class.getName();

    private final Meter meter;

    // metric
    private final LongCounter requestCounter;

    // tracer
    private final Tracer tracer;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper, OpenTelemetry openTelemetry) {

        this.productRepository = productRepository;

        this.productMapper = productMapper;

        this.meter = openTelemetry.getMeter(INSTRUMENTATION_NAME);

        this.requestCounter = meter.counterBuilder("app.db.requests")
                .setDescription("Counter_db_requests")
                .build();

        this.tracer = openTelemetry.getTracer(INSTRUMENTATION_NAME);
    }

    public List<ProductResponse> allProducts() {

        requestCounter.add(1);

        List<ProductResponse> productResponses;

        Span dbSpan = tracer.spanBuilder("Database_fetch_and_mapping").startSpan();

        try (Scope dbScope = dbSpan.makeCurrent()) { // Make dbSpan current

            List<Product> products;

            Span fetchSpan = tracer.spanBuilder("Fetch_products_from_db")
                    .setSpanKind(SpanKind.INTERNAL)
                    .startSpan();

            try (Scope fetchScope = fetchSpan.makeCurrent()) { // Make fetchSpan current

                products = productRepository.findAll(); // Now this will be child of fetchSpan

            } finally {
                fetchSpan.end();
            }

            Span mappingSpan = tracer.spanBuilder("Map_products_to_response")
                    .setSpanKind(SpanKind.INTERNAL)
                    .startSpan();

            try (Scope mappingScope = mappingSpan.makeCurrent()) { // Make mappingSpan current
                productResponses = products.stream()
                        .map(product -> productMapper.productResponse(product))
                        .toList();

            } finally {
                mappingSpan.end();
            }

        } finally {
            dbSpan.end();
        }

        return productResponses;
    }

    public ProductResponse createProduct(ProductRequest request) {

        Product product = productMapper.product(request);

        productRepository.save(product);

        return productMapper.productResponse(product);
    }

    public ProductResponse updateProduct(Integer id, ProductRequest request) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + id + " not found"));

        Product updatedProduct = productMapper.updateProduct(existingProduct, request);

        Product savedProduct = productRepository.save(updatedProduct);

        return productMapper.productResponse(savedProduct);
    }

    public ProductResponse deleteProduct(Integer id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + id + " not found"));

        productRepository.delete(product);

        return productMapper.productResponse(product);
    }
}
