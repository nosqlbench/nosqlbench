package io.virtdata.apps.valuesapp;

public class RunData {
    public String spec;
    public int threads;
    public long min;
    public long max;
    public int buffersize;
    public boolean isolated;
    public double totalGenTimeMs;
    public double totalCmpTimeMs;

    public RunData(
            String spec,
            int threads, long min, long max, int buffersize, boolean isolated,
            double totalGenTimeMs, double totalCmpTimeMs) {
        this.spec = spec;
        this.threads = threads;
        this.min = min;
        this.max = max;
        this.buffersize = buffersize;
        this.isolated = isolated;
        this.totalGenTimeMs = totalGenTimeMs;
        this.totalCmpTimeMs = totalCmpTimeMs;
    }

    @Override
    public String toString() {
        return  "         run data = [derived values in brackets]\n" +
                "        specifier = '" + spec + "'\n" +
                "          threads = " + threads + "\n" +
                "              min = " + min + "\n" +
                "              max = " + max + "\n" +
                "          [count] = " + (max - min) + "\n" +
                "       buffersize = " + buffersize + "\n" +
                "         isolated = " + isolated + "\n" +
                " [totalGenTimeMs] = " + totalGenTimeMs + "\n" +
                " [totalCmpTimeMs] = " + totalCmpTimeMs + "\n" +
                String.format("      [genPerMs] = %.3f\n", ((double) threads * (double) (max - min)) / totalGenTimeMs) +
                String.format("      [cmpPerMs] = %.3f\n", ((double) threads * (double) (max - min)) / totalCmpTimeMs) +
                String.format("      [genPerS] = %.3f\n", 1000.0d * ((double) threads * (double) (max-min)) / totalGenTimeMs) +
                String.format("      [cmpPerS] = %.3f\n", 1000.0d * ((double) threads * (double) (max-min)) / totalCmpTimeMs);
    }
}
