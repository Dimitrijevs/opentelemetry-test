package openTelemetry.products;

// import java.time.Duration;

// import static org.assertj.core.api.Assertions.assertThat;
// import org.junit.jupiter.api.Test;

// import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
// import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
// import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
// import us.abstracta.jmeter.javadsl.blazemeter.BlazeMeterEngine;
// import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class BlazeMeter {

    // private String blazeSecret = "aee8c2734b0e66e6e14dcf7a:48f71741b61bb86ad26f6bfe1cb09266cf22f533aef18149a430529d9559e9ebe36974c5";

    // @Test
    // public void testPerformance() throws Exception {

    //     System.out.println("SecretTest = " + blazeSecret);

    //     TestPlanStats stats = testPlan(

    //             // threads - virtual users, iterations - iterations per threat
    //             // 2 threads and 10 iterations each (BlazeMeter will overwrite these settings)
    //             threadGroup(2, 10,
    //                     httpSampler("http://localhost:8100/api/v1/products/all")
    //             )

    //     // Run the test on BlazeMeter cloud instead of locally
    //     ).runIn(new BlazeMeterEngine(blazeSecret)   // blazemeter token
    //             .testName("DSL test") // test name
    //             .totalUsers(5) // total virtual users
    //             .holdFor(Duration.ofSeconds(30)) // Duration to hold all users active
    //             .testTimeout(Duration.ofSeconds(120))); // Maximum time the test can run before stopping

    //     assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
    // }
}
