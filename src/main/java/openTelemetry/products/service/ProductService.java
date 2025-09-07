package openTelemetry.products.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import openTelemetry.products.dto.ProductRequest;
import openTelemetry.products.dto.ProductResponse;
import openTelemetry.products.mapper.ProductMapper;
import openTelemetry.products.model.Product;
import openTelemetry.products.repository.ProductRepository;

@Service
@Slf4j
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

    public ProductService(ProductRepository productRepository, ProductMapper productMapper,
            OpenTelemetry openTelemetry) {

        this.productRepository = productRepository;

        this.productMapper = productMapper;

        this.meter = openTelemetry.getMeter(INSTRUMENTATION_NAME);

        this.requestCounter = meter.counterBuilder("app.db.requests")
                .setDescription("Counter_db_requests")
                .build();

        this.tracer = openTelemetry.getTracer(INSTRUMENTATION_NAME);
    }

    public List<ProductResponse> allProducts() {
        log.info("Starting product fetch operation");
        
        requestCounter.add(1);
        List<ProductResponse> productResponses;

        Span dbSpan = tracer.spanBuilder("Database_fetch_and_mapping")
                .startSpan();

        try (Scope dbScope = dbSpan.makeCurrent()) {

            // Add span attributes for better observability
            dbSpan.setAttribute("operation.type", "database");
            dbSpan.setAttribute("service.component", "product-service");

            List<Product> products = fetchProductsFromDatabase();
            productResponses = mapProductsToResponse(products);
            
            // Add metrics to span
            dbSpan.setAttribute("products.count", products.size());
            log.info("Successfully processed {} products", products.size());

        } catch (Exception e) {

            dbSpan.recordException(e);

            dbSpan.setStatus(StatusCode.ERROR, "Failed to fetch and map products");

            log.error("Error processing products", e);
            throw e;
        } finally {
            dbSpan.end();
        }

        log.info("Product fetch operation completed successfully");

        return productResponses;
    }

    private List<Product> fetchProductsFromDatabase() {
        Span fetchSpan = tracer.spanBuilder("Fetch_products_from_db")
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        try (Scope fetchScope = fetchSpan.makeCurrent()) {
            log.info("Fetching products from database");

            List<Product> products = productRepository.findAll();
            
            fetchSpan.setAttribute("db.operation", "findAll");
            fetchSpan.setAttribute("db.collection.name", "products");
            fetchSpan.setAttribute("products.fetched", products.size());
            
            log.debug("Fetched {} products from database", products.size());
            return products;

        } catch (Exception e) {

            fetchSpan.recordException(e);

            fetchSpan.setStatus(StatusCode.ERROR, "Database fetch failed");

            log.error("Failed to fetch products from database", e);
            throw e;
        } finally {

            fetchSpan.end();
        }
    }

    private List<ProductResponse> mapProductsToResponse(List<Product> products) {
        Span mappingSpan = tracer.spanBuilder("Map_products_to_response")
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        try (Scope mappingScope = mappingSpan.makeCurrent()) {
            
            log.info("Mapping {} products to response format", products.size());

            List<ProductResponse> productResponses = products.stream()
                    .map(product -> {
                        try {
                            return productMapper.productResponse(product);
                        } catch (Exception e) {
                            log.warn("Failed to map product with id: {}", product.getId(), e);
                            return null; // or handle differently
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            mappingSpan.setAttribute("mapping.input_count", products.size());
            mappingSpan.setAttribute("mapping.output_count", productResponses.size());
            
            if (productResponses.size() != products.size()) {
                log.warn("Some products failed to map. Input: {}, Output: {}", 
                        products.size(), productResponses.size());
            }

            log.debug("Successfully mapped {} products", productResponses.size());
            return productResponses;

        } catch (Exception e) {
            mappingSpan.recordException(e);
            mappingSpan.setStatus(StatusCode.ERROR, "Product mapping failed");
            log.error("Failed to map products to response", e);
            throw e;
        } finally {

            mappingSpan.end();
        }
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
