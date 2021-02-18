package io.nosqlbench.virtdata.library.basics.tests.long_string;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.HashedFileExtractToString;
import org.junit.Test;

import java.util.IntSummaryStatistics;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

public class HashedFileExtractToStringTest {

    @Test
    public void testHashedFileBasic() {
        HashedFileExtractToString extract =
            new HashedFileExtractToString("data/lorem_ipsum_full.txt", 3, 3000);
        IntSummaryStatistics iss = new IntSummaryStatistics();
        for (long cycle = 0; cycle < 50000; cycle++) {
            String apply = extract.apply(cycle);
            iss.accept(apply.length());
            assertThat(apply.length()).isGreaterThanOrEqualTo(3);
            assertThat(apply.length()).isLessThanOrEqualTo(3000);
        }

        System.out.println("Loaded examples from data/lorem_ipsum_full.txt:" + iss.toString());
    }

    @Test
    public void testHashedFileFunction() {
        HashedFileExtractToString extract =
            new HashedFileExtractToString("data/lorem_ipsum_full.txt", (LongUnaryOperator) ((long f) -> 32734 * f));
        IntSummaryStatistics iss = new IntSummaryStatistics();

        for (long cycle = 0; cycle < 50000; cycle++) {
            String apply = extract.apply(cycle);
            iss.accept(apply.length());
        }

        System.out.println("Loaded examples from data/lorem_ipsum_full.txt:" + iss.toString());
    }
}
