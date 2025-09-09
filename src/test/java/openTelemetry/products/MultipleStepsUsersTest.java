package openTelemetry.products;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class MultipleStepsUsersTest {

    @Test
    public void test() throws IOException {

        TestPlanStats stats = testPlan(
                // thread group with 1 thread, running 1 iteration
                // instead of iterations I can allso write Duration.ofMinutes(1)
                threadGroup(1, 10,
                        // default HTTP settings for all requests in this thread group
                        httpDefaults()
                                // Character encoding to UTF-8 for all HTTP requests
                                .encoding(StandardCharsets.UTF_8),
                        // First HTTP request - GET to the welcome page
                        httpSampler("get-welcome-page", "http://localhost:8100/api/v1/products/welcome-page"),
                        // Second HTTP request - GET to all products
                        httpSampler("get-all-products", "http://localhost:8100/api/v1/products/all")
                                // Child elements to this sampler
                                .children(
                                        // Extract productId from the response using regex
                                        // Looks for: name="productId" value="CAPTURED_VALUE"
                                        regexExtractor("product_id", "name=\"id\" value=\"6\"")
                                                // If regex doesn't match, use this default value
                                                .defaultValue("product_id#6_NOT_FOUND")
                                ),
                        // Third HTTP request - POST to /products/create endpoint
                        httpSampler("create-product-json", "http://localhost:8100/api/v1/products/create")
                                .method(HTTPConstants.POST)
                                .contentType(ContentType.APPLICATION_JSON)
                                // Send JSON body instead of form parameters
                                .body("{"
                                        + "\"title\":\"iPhone SE " + "${__Random(10000,99999)}-${__time(yyyy-MM-dd-HH-mm-ss)}\","
                                        + "\"description\":\"iPhone SE ${__Random(10000,99999)}-${__time(yyyy-MM-dd-HH-mm-ss)} description\","
                                        + "\"price\":" + "${__Random(400,1000)}"
                                        + "}"),
                        // Fourth HTTP request - GET to /cart (to view cart)
                        httpSampler("get-products-all-2", "http://localhost:8100/api/v1/products/all")
                ),

                // jtlWriter("target/multipleStepsUsersTest").withAllFields()
                jtlWriter("target/multipleStepsUsersTest")

                // jtlWriter("target/jtls/success")
                //         .logOnly(SampleStatus.SUCCESS),
                // jtlWriter("target/jtls/error")
                //         .logOnly(SampleStatus.ERROR)

                // responseFileSaver(Instant.now().toString().replace(":", "-") + "-response")

                // to overwrite file name
                // jtlWriter(directory, fileName)
        // gui
        // can be added int variable to show amount of iterations ( by default is shown last 500 )
        // resultsTreeVisualizer()
        ).run(); // Execute the test plan

        // to show test plan in app
        // .showInGui();
        // Assert that no errors occurred during the test
        assertThat(stats.overall().errorsCount()).isEqualTo(0);

        assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
    }
}
