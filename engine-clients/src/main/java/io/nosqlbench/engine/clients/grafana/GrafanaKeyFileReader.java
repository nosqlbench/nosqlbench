package io.nosqlbench.engine.clients.grafana;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class GrafanaKeyFileReader implements Supplier<String> {
    private final static Logger logger = LogManager.getLogger("ANNOTATORS" );

    private final Path keyfilePath;

    public GrafanaKeyFileReader(Path path) {
        this.keyfilePath = path;
    }

    public GrafanaKeyFileReader(String sourcePath) {
        this.keyfilePath = Path.of(sourcePath);
    }

    @Override
    public String get() {
        if (!Files.exists(keyfilePath)) {
            logger.warn("apikeyfile does not exist at '" + keyfilePath.toString());
            return null;
        } else {
            try {
                String apikey = Files.readString(keyfilePath, StandardCharsets.UTF_8);
                apikey = apikey.trim();
                return apikey;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
