package io.nosqlbench.engine.extensions.csvoutput;

import org.assertj.core.util.Files;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

public class CsvOutputPluginWriterTest {

    @Test
    public void testCsvOutputWriter() {
        File tmpfile = Files.newTemporaryFile();
        tmpfile.deleteOnExit();
        System.out.println("tmpfile="+ tmpfile.getPath());
        CsvOutputPluginWriter out = new CsvOutputPluginWriter(tmpfile.getPath(), "one", "two");
        out.write(Value.asValue(Map.of("one","one_","two","two_")));
    }


}
