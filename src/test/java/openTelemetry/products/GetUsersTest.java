package openTelemetry.products;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import static us.abstracta.jmeter.javadsl.JmeterDsl.htmlReporter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jtlWriter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.prometheus.DslPrometheusListener.PrometheusMetric;

import static us.abstracta.jmeter.javadsl.prometheus.DslPrometheusListener.prometheusListener;

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

                threadGroup(2, 20,
                        httpSampler("get_users", "http://localhost:8100/api/v1/products/all")
                ),
                // saves request stats
                jtlWriter("target/getUsersTest"),

                htmlReporter("target_reports/getUsersTest"),

                prometheusListener()
                        .metrics(
                                PrometheusMetric.responseTime("ResponseTime", "Response time of samplers")
                                        .labels(PrometheusMetric.SAMPLE_LABEL, PrometheusMetric.RESPONSE_CODE)
                                        .quantile(0.5, 0.5)
                                        .quantile(0.95, 0.1)
                                        .quantile(0.99, 0.01)
                                        .maxAge(Duration.ofMinutes(1)),
                                PrometheusMetric.successRatio("SuccessRatio", "Success ratio of samplers")
                                        .labels(PrometheusMetric.SAMPLE_LABEL, PrometheusMetric.RESPONSE_CODE)
                        )
                        .port(9270)
                        .endWait(Duration.ofSeconds(120))
        // gui
        // resultsTreeVisualizer()
        ).run();

        // requirements for test to pass
        assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofMinutes(5));
    }
}
