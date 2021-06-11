package io.nosqlbench.virtdata.library.basics.shared.distributions;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * All tests in this class are based on a CSV file with the following contents.
 *
 * <pre> {@code
 * NAME,WEIGHT,MEMO
 * alpha,1,this is sparta
 * beta,2,this is sparta
 * gamma,3,this is sparta
 * delta,4,this is sparta
 * epsilon,5,this is sparta
 * alpha,6,this is sparta
 * } </pre>
 */
public class CSVSamplerTest {


    /**
     * In this test, alpha appears twice, and all others once, so alpha should appear roughly 2x more frequently
     */
    @Test
    public void testByCount() {
        CSVSampler sampler = new CSVSampler("name", "weightfoo", "count", "basicdata");
        String value = sampler.apply(1);

        Map<String,Double> results = new HashMap<>();
        for (int i = 0; i < 100000; i++) {
            String name = sampler.apply(i);
            results.compute(name, (k,v) -> v==null ? 1d : v + 1d);
        }
        System.out.println(results);
        assertThat(results.get("alpha")).isCloseTo(results.get("beta")*2, Percentage.withPercentage(5.0d));
        assertThat(results.get("alpha")).isCloseTo(results.get("gamma")*2, Percentage.withPercentage(5.0d));
        assertThat(results.get("alpha")).isCloseTo(results.get("delta")*2, Percentage.withPercentage(5.0d));
        assertThat(results.get("alpha")).isCloseTo(results.get("epsilon")*2, Percentage.withPercentage(5.0d));
    }

    /**
     * In this test, alpha's weights sum to 1/3 of the total weight, thus it should appear roughly 1/3 of the time
     */
    @Test
    public void testBySum() {
        CSVSampler sampler = new CSVSampler("name", "weight", "sum", "basicdata");
        String value = sampler.apply(1);

        Map<String,Double> results = new HashMap<>();
        for (int i = 0; i < 100000; i++) {
            String name = sampler.apply(i);
            results.compute(name, (k,v) -> v==null ? 1d : v + 1d);
        }
        System.out.println(results);
        assertThat(results.get("alpha")).isCloseTo(33333, Percentage.withPercentage(2.0d));
    }

    /**
     * In this test, alpha's weights avg to 3.5, or 3.5/17.5 or 20%, so should appear 20% of the time.
     */
    @Test
    public void testByAvgs() {
        CSVSampler sampler = new CSVSampler("name", "weight", "avg", "basicdata");
        String value = sampler.apply(1);

        Map<String,Double> results = new HashMap<>();
        for (int i = 0; i < 100000; i++) {
            String name = sampler.apply(i);
            results.compute(name, (k,v) -> v==null ? 1d : v + 1d);
        }
        System.out.println(results);
        assertThat(results.get("alpha")).isCloseTo(20000, Percentage.withPercentage(2.0d));
    }

    /**
     * In this test, alpha is 1/15 of the total weight, or 6.6% of expected frequency
     */
    @Test
    public void testByMin() {
        CSVSampler sampler = new CSVSampler("name", "weight", "min", "basicdata");
        String value = sampler.apply(1);

        Map<String,Double> results = new HashMap<>();
        for (int i = 0; i < 100000; i++) {
            String name = sampler.apply(i);
            results.compute(name, (k,v) -> v==null ? 1d : v + 1d);
        }
        System.out.println(results);
        assertThat(results.get("alpha")).isCloseTo(6666, Percentage.withPercentage(2.0d));
    }

    /**
     * In this test, alpha is 6/20 of expected frequency or 30%
     */
    @Test
    public void testByMax() {
        CSVSampler sampler = new CSVSampler("name", "weight", "max", "basicdata");
        String value = sampler.apply(1);

        Map<String,Double> results = new HashMap<>();
        for (int i = 0; i < 100000; i++) {
            String name = sampler.apply(i);
            results.compute(name, (k,v) -> v==null ? 1d : v + 1d);
        }
        System.out.println(results);
        assertThat(results.get("alpha")).isCloseTo(30000, Percentage.withPercentage(2.0d));
    }

    /**
     * In this test, alpha is 1/5 of the distinct names included.
     */
    @Test
    public void testByName() {
        CSVSampler sampler = new CSVSampler("name", "does not matter", "name", "basicdata");
        String value = sampler.apply(1);

        Map<String,Double> results = new HashMap<>();
        for (int i = 0; i < 100000; i++) {
            String name = sampler.apply(i);
            results.compute(name, (k,v) -> v==null ? 1d : v + 1d);
        }
        System.out.println(results);
        assertThat(results.get("alpha")).isCloseTo(20000, Percentage.withPercentage(2.0d));
    }



}
