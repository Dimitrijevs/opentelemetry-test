package openTelemetry.products;

import java.io.IOException;
import java.time.Duration;

import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.jupiter.api.Test;

import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class UsersTransactionTest {

    String defaultPath = "/api/v1/products";

    String defaultAllProducts = "/all";

    String createProducts = "/create";

    String updateProduct = "/update";

    @Test
    public void testTransactions() throws IOException {

        TestPlanStats stats = testPlan(
            httpDefaults().url("http://localhost:8100"),

            threadGroup(2, 10,

                transaction("createAndEditProduct",

                    httpSampler(defaultPath + createProducts)
                        .method(HTTPConstants.POST)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body("{"
                                + "\"title\":\"iPhone SE " + "${__Random(10000,99999)}-${__time(yyyy-MM-dd-HH-mm-ss)}\","
                                + "\"description\":\"iPhone SE ${__Random(10000,99999)}-${__time(yyyy-MM-dd-HH-mm-ss)} description\","
                                + "\"price\":" + "${__Random(400,1000)}"
                                + "}")

                        // Extract 'id' from JSON response
                        .children(
                            jsonExtractor("PRODUCT_ID", "id"),
                            jsonExtractor("PRODUCT_NAME", "title"),
                            jsonExtractor("PRODUCT_DESCRIPTION", "description"),
                            jsonExtractor("PRODUCT_PRICE", "price")
                        ),

                    httpSampler(defaultPath + updateProduct + "/${PRODUCT_ID}") // GET /update/PRODUCT_ID
                        .method(HTTPConstants.PUT)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body("{"
                                + "\"title\":\"${PRODUCT_NAME}_UPDATED\","
                                + "\"description\":\"${PRODUCT_DESCRIPTION}\","
                                + "\"price\":" + "${PRODUCT_PRICE}"
                                + "}")
                )
            ),

            jtlWriter("target/UsersTransactionTest") 

        ).run();

        assertThat(stats.overall().errorsCount()).isEqualTo(0);

        assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(2));
    }
}
