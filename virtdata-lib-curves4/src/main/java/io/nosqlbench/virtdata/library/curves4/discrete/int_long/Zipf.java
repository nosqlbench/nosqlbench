package io.nosqlbench.virtdata.library.curves4.discrete.int_long;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.ZipfDistribution;

@ThreadSafeMapper
public class Zipf extends IntToLongDiscreteCurve {
    public Zipf(int numberOfElements, double exponent, String... modslist) {
        super(new ZipfDistribution(numberOfElements, exponent), modslist);
    }
}
