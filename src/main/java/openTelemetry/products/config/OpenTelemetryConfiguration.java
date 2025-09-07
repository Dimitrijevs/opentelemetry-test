package openTelemetry.products.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.ServiceAttributes;
import jakarta.annotation.PreDestroy;

@Configuration
public class OpenTelemetryConfiguration {

        @Value("${otel.service.name}")
        private String serviceName;

        @Value("${otel.exporter.otlp.endpoint}")
        private String otlpEndpoint;

        @Value("${otel.metric.export.interval}")
        private Duration metricExportInterval;

        private OpenTelemetry openTelemetry;

        @Bean
        public OpenTelemetry openTelemetry() {
                // Setup resource with service name
                Resource resource = Resource.getDefault()
                                .merge(Resource.create(Attributes.of(
                                                ServiceAttributes.SERVICE_NAME, serviceName)));

                // gRPC Span exporter (no need to add /v1/traces for gRPC)
                SpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                                .setEndpoint(otlpEndpoint)
                                .build();

                // gRPC Metrics exporter (no need to add /v1/metrics for gRPC)
                PeriodicMetricReader metricReader = PeriodicMetricReader.builder(
                                OtlpGrpcMetricExporter.builder()
                                                .setEndpoint(otlpEndpoint)
                                                .build())
                                .setInterval(metricExportInterval)
                                .build();

                // gRPC Log exporter (no need to add /v1/logs for gRPC)
                LogRecordExporter logRecordExporter = OtlpGrpcLogRecordExporter.builder()
                                .setEndpoint(otlpEndpoint)
                                .build();

                SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
                                .setResource(resource)
                                .setLogLimits(LogLimitsConfig::logLimits)
                                .addLogRecordProcessor(BatchLogRecordProcessor.builder(logRecordExporter).build())
                                .build();

                // Build providers
                SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                                .setResource(resource)
                                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                                .build();

                SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                                .setResource(resource)
                                .registerMetricReader(metricReader)
                                .build();

                // Build OpenTelemetry SDK
                this.openTelemetry = OpenTelemetrySdk.builder()
                                .setTracerProvider(tracerProvider)
                                .setMeterProvider(meterProvider)
                                .setLoggerProvider(loggerProvider)
                                .build();

                return this.openTelemetry;
        }

        @PreDestroy
        public void cleanup() {
                if (openTelemetry instanceof OpenTelemetrySdk) {
                        ((OpenTelemetrySdk) openTelemetry).close();
                }
        }
}