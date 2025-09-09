package openTelemetry.products;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class GetUsersTest {

    // Set up & tear down
    // @BeforeEach
    // public void setup() {
    //     // my custom setup logic
    // }

    // @AfterEach
    // public void setup() {
    //     // my custom setup logic
    // }

    @Test
    public void testPerformance() throws IOException {

        // test initialization
        TestPlanStats stats = testPlan(
                // instead of iterations I can allso write Duration.ofMinutes(1)

                // threadGroup().rampTo(10, Duration.ofSeconds(5)).holdIterating(20)
                // Start with 0 threads and slowly add threads until reaching 10 threads over 5 seconds.
                // Each thread runs the test **20 times** once it starts.

                // threadGroup().rampToAndHold(10, Duration.ofSeconds(5), Duration.ofSeconds(20))
                // Start with 0 threads and slowly add threads until reaching 10 threads over 5 seconds.
                // Once 10 threads are running, **keep them running for 20 seconds** doing the test repeatedly.

                // threadGroup()
                // // Start with 0 threads, slowly add threads to reach 10 over 5 seconds, then keep them running for 20 seconds
                // .rampToAndHold(10, Duration.ofSeconds(5), Duration.ofSeconds(20))

                // // Increase threads from 10 to 100 over 10 seconds, then hold 100 threads for 30 seconds
                // .rampToAndHold(100, Duration.ofSeconds(10), Duration.ofSeconds(30))

                // // Ramp up threads from 100 to 200 over 10 seconds (no holding, just ramping)
                // .rampTo(200, Duration.ofSeconds(10))

                // // Reduce threads from 200 to 100 over 10 seconds, then hold 100 threads for 30 seconds
                // .rampToAndHold(100, Duration.ofSeconds(10), Duration.ofSeconds(30))

                // // Ramp down threads from 100 to 0 over 5 seconds (end of test)
                // .rampTo(0, Duration.ofSeconds(5))

                // // Each thread will execute this HTTP request
                // .children(
                //   httpSampler("http://my.service")
                // );

                threadGroup(1, 10,
                        httpSampler("get_users", "http://localhost:8100/api/v1/products/all")
                ),

                // saves request stats
                jtlWriter("target/getUsersTest")

                // gui
                // resultsTreeVisualizer()
        ).run();

        // requirements for test to pass
        assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
    }
}
