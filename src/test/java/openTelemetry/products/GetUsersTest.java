package openTelemetry.products;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class GetUsersTest {

    @Test
    public void testPerformance() throws IOException {

        // test initialization
        TestPlanStats stats = testPlan(

                threadGroup(1, 10,
                        httpSampler("get_users", "http://localhost:8100/api/v1/products/all")
                ),
                
                // saves request stats
                jtlWriter("target/getUsersTest")
        ).run();

        // requirements for test to pass
        assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
    }
}
