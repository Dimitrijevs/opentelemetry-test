package openTelemetry.products;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class SequentialTestExample {

    @Test
    void contextLoads() throws Exception {

        DslTestPlan testPlan = testPlan(

            threadGroup(2, 5, // 2 threads, 5 iterations
                httpSampler("http://localhost:8100/api/v1/products/all")
            ),

            threadGroup(3, 3, // 3 threads, 3 iterations
                httpSampler("http://localhost:8100/api/v1/products/welcome-page")
            ),

            jtlWriter("target/sequentialTestExample")
        );

        // Set the test plan to run thread groups sequentially
        testPlan.sequentialThreadGroups(true);

        TestPlanStats stats = testPlan.run();

        assertThat(stats.overall().errorsCount()).isEqualTo(0);
    }
}
