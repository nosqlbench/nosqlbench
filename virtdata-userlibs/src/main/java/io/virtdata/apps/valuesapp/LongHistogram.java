package io.virtdata.apps.valuesapp;

public class LongHistogram implements PostProcessor {

    long[] buckets;
    long mask;

    public LongHistogram(int bits) {
        if (bits>16) {
            throw new RuntimeException("bits > 16 may cause OOM");
        }
        mask=0L;
        for (int i = 1; i <= bits; i++) {
            mask&=1L<<i;
        }
        System.out.println("mask:" + mask);
    }

    @Override
    public void process(Object[] values) {
        for (Object value : values) {
            long v = (long) value;

        }

    }

    @Override
    public void close() {

    }
}
