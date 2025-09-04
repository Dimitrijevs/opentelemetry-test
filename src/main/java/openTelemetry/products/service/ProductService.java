package openTelemetry.products.service;

import java.util.List;
import java.util.logging.Logger;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.stereotype.Service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
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

    //logger
    private static final Logger logger = Logger.getLogger("jul-logger");

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {

        this.productRepository = productRepository;

        this.productMapper = productMapper;

        OpenTelemetry openTelemetry = initOpenTelemetry();

        this.meter = openTelemetry.getMeter(INSTRUMENTATION_NAME);

        this.requestCounter = meter.counterBuilder("app.db.requests")
                .setDescription("Counter_db_requests")
                .build();

        this.tracer = openTelemetry.getTracer(INSTRUMENTATION_NAME);

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    static OpenTelemetry initOpenTelemetry() {

        // setup resource with service name

        Resource resource = Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), "products-service"));

        // metrics

        OtlpGrpcMetricExporter otlpGrpcMetricExporter = OtlpGrpcMetricExporter.builder()
                .setEndpoint("http://ht-otel-collector:4317")
                .build();

        PeriodicMetricReader periodicMetricReader = PeriodicMetricReader.builder(otlpGrpcMetricExporter)
                .setInterval(java.time.Duration.ofSeconds(15))
                .build();

        // Traces

        OtlpGrpcSpanExporter otlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://ht-otel-collector:4317")
                .build();

        SimpleSpanProcessor simpleSpanProcessor = SimpleSpanProcessor.builder(otlpGrpcSpanExporter)
                .build();

        // logs 

        OtlpGrpcLogRecordExporter otlpGrpcLogRecordExporter = OtlpGrpcLogRecordExporter.builder()
                .setEndpoint("http://ht-otel-collector:4317")
                .build();

        BatchLogRecordProcessor batchLogRecordProcessor = BatchLogRecordProcessor.builder(otlpGrpcLogRecordExporter)
                .build();

        // providers

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(simpleSpanProcessor)
                .build();

        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .registerMetricReader(periodicMetricReader)
                .build();

        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
                .setResource(resource)
                .addLogRecordProcessor(batchLogRecordProcessor)
                .build();

        // sdk

        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
                .setMeterProvider(sdkMeterProvider)
                .setTracerProvider(sdkTracerProvider)
                .setLoggerProvider(loggerProvider)
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(openTelemetrySdk::close));

        return openTelemetrySdk;
    }

    public List<ProductResponse> allProducts() {

        requestCounter.add(1);

        logger.info("Request recieved succesfully");

        List<ProductResponse> productResponses;

        Span dbSpan = tracer.spanBuilder("Database_fetch_and_mapping").startSpan();

        try (Scope dbScope = dbSpan.makeCurrent()) { // Make dbSpan current

            List<Product> products;

            Span fetchSpan = tracer.spanBuilder("Fetch_products_from_db")
                    .setSpanKind(SpanKind.INTERNAL)
                    .startSpan();

            try (Scope fetchScope = fetchSpan.makeCurrent()) { // Make fetchSpan current

                products = productRepository.findAll(); // Now this will be child of fetchSpan

                logger.info("Fetched data correctly");
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

                logger.info("Mapped data correctly");
            } finally {
                mappingSpan.end();
            }

        } finally {
            dbSpan.end();
        }

        logger.info("Business logic finished");

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
