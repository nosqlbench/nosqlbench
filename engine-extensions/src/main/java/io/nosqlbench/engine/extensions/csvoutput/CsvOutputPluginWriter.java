package io.nosqlbench.engine.extensions.csvoutput;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.graalvm.polyglot.Value;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

public class CsvOutputPluginWriter extends CsvOutput {

    private final CSVPrinter printer;
    private final FileWriter filewriter;
    private final LinkedHashSet<String> headerKeys;
    private final String filename;

    public CsvOutputPluginWriter(String filename, String... headers) {
        try {
            this.filename = filename;
            Path filepath = Path.of(filename);
            Files.createDirectories(filepath.getParent(), PosixFilePermissions.asFileAttribute(
                PosixFilePermissions.fromString("rwxr-x---")
            ));
            CSVFormat fmt = CSVFormat.DEFAULT;
            this.headerKeys = new LinkedHashSet<>(Arrays.asList(headers));
            this.filewriter = new FileWriter(filepath.toString());
            this.printer = new CSVPrinter(filewriter, fmt);
            if (Files.size(Path.of(filename)) == 0) {
                printer.printRecord(headerKeys);
                printer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CsvOutputPluginWriter write(Value value) {
        List<String> lineout = new ArrayList<>();
        Map<String, String> provided = new HashMap<>();
        if (value.isHostObject()) {
            Object o = value.asHostObject();
            if (o instanceof Map) {
                ((Map<?, ?>) o).forEach((k, v) -> {
                    provided.put(k.toString(), v.toString());
                });
            } else {
                throw new RuntimeException("host object provided as '" + o.getClass().getCanonicalName() + ", but only Maps are supported.");
            }
        } else if (value.hasMembers()) {
            for (String vkey : value.getMemberKeys()) {
                provided.put(vkey, value.getMember(vkey).toString());
            }
        } else {
            throw new RuntimeException("Value was not a Map host object nor a type with members.");
        }

        for (String headerKey : headerKeys) {
            if (provided.containsKey(headerKey)) {
                lineout.add(provided.remove(headerKey));
            } else {
                lineout.add("");
            }
        }
        if (provided.size() > 0) {
            throw new RuntimeException("Unqualified column was emitted for file '" + filename);
        }

        try {
            printer.printRecord(lineout);
            printer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }
}
