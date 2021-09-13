package io.nosqlbench.engine.extensions.csvoutput;

public class CsvOutputPluginInstance {

    public CsvOutput open(String filename, String... headers) {
        return new CsvOutputWriter(filename, headers);
    }
}
