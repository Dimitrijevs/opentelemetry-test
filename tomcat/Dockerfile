FROM openjdk:21-jdk-slim

WORKDIR /app

# Download the OpenTelemetry Java agent
ADD --chmod=644 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

# Copy your application JAR (go up one level from tomcat/)
COPY ../build/libs/*.jar app.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar"
ENV OTEL_SERVICE_NAME="products-service"
ENV OTEL_TRACES_EXPORTER="otlp"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="http://ht-otel-collector:4317"
ENV OTEL_EXPORTER_OTLP_PROTOCOL="grpc"

EXPOSE 8100

ENTRYPOINT ["java", "-jar", "app.jar"]
