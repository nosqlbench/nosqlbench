package io.nosqlbench.virtdata.library.basics.shared.distributions;

class LabeledStatistic {
    public final String label;
    public final double total;
    public final int count;
    public final double min;
    public final double max;

    public LabeledStatistic(String label, double weight) {
        this.label = label;
        this.total = weight;
        this.min = weight;
        this.max = weight;
        this.count = 1;
    }

    private LabeledStatistic(String label, double total, double min, double max, int count) {
        this.label = label;
        this.total = total;
        this.min = min;
        this.max = max;
        this.count = count;
    }

    public LabeledStatistic merge(LabeledStatistic tuple) {
        return new LabeledStatistic(
            this.label,
            this.total + tuple.total,
            Math.min(this.min, tuple.min),
            Math.max(this.max, tuple.max),
            this.count + tuple.count
        );
    }

    public double count() {
        return count;
    }

    public double avg() {
        return total / count;
    }

    public double sum() {
        return total;
    }

    @Override
    public String toString() {
        return "EntryTuple{" +
            "label='" + label + '\'' +
            ", total=" + total +
            ", count=" + count +
            '}';
    }

    public double min() {
        return this.min;
    }

    public double max() {
        return this.max;
    }
}
