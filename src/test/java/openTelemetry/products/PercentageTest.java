package openTelemetry.products;

// import java.time.Duration;

// import static org.assertj.core.api.Assertions.assertThat;
// import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
// import static us.abstracta.jmeter.javadsl.JmeterDsl.percentController;
// import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
// import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

// import org.junit.jupiter.api.Test;

// import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PercentageTest {

    // @Test
    // public void testPerformance() throws Exception {
    //     TestPlanStats stats = testPlan(
    //             threadGroup(2, 10,
    //                     percentController(40, // run this 40% of the times
    //                             httpSampler("http://my.service/status"),
    //                             httpSampler("http://my.service/poll")),
    //                     percentController(70, // run this 70% of the times
    //                             httpSampler("http://my.service/items"))
    //             )
    //     ).run();
    //     assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
    // }
}
