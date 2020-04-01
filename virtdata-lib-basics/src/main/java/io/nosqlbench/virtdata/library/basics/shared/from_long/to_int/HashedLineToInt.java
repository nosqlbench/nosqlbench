package io.nosqlbench.virtdata.library.basics.shared.from_long.to_int;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.nb.api.pathutil.VirtDataResources;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.function.LongToIntFunction;

/**
 * Return a pseudo-randomly selected integer value from a file of numeric values.
 * Each line in the file must contain one parsable integer value.
 */
@ThreadSafeMapper
public class HashedLineToInt implements LongToIntFunction {
    private final static Logger logger  = LogManager.getLogger(HashedLineToInt.class);
    private int[] values;
    private final String filename;
    private final Hash intHash;

    public HashedLineToInt(String filename) {
        this.filename = filename;
        List<String> lines = VirtDataResources.readDataFileLines(filename);
        this.values = lines.stream().mapToInt(Integer::parseInt).toArray();
        this.intHash = new Hash();
    }

    public String toString() {
        return getClass().getSimpleName() + ":" + filename;
    }

    @Override
    public int applyAsInt(long value) {
        int itemIdx = intHash.applyAsInt(value) % values.length;
        return values[itemIdx];
    }
}

