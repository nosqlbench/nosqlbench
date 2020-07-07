package io.nosqlbench.activitytype.cql.datamappers.functions.string_string;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Function;

@ThreadSafeMapper
public class SnappyComp implements Function<String, ByteBuffer> {

    private Snappy snappy = new Snappy();

    @Override
    public ByteBuffer apply(String s) {
        try {
            return ByteBuffer.wrap(Snappy.compress(s));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
