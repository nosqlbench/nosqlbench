package io.nosqlbench.engine.extensions.csvoutput;

public class CsvOutputPluginInstance {

    public CsvOutputPluginWriter open(String filename, String... headers) {
        return new CsvOutputPluginWriter(filename, headers);
    }
}
