package io.nosqlbench.driver.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SecureUtils {
    private final static Logger logger = LoggerFactory.getLogger(SecureUtils.class);

    public static String readSecret(String description, String source) {
        if (source==null) {
            return null;
        }

        if (source.startsWith("file:")) {
            String sourceFile = source.substring("file:".length());
            try {
                return Files.readString(Path.of(sourceFile), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (source.startsWith("console:")||source.equals("")) {
            System.out.print("Enter " + description+":");

            char[] chars = System.console().readPassword("%s:",description);
            return new String(chars);
        } else {
            logger.warn("Parameter for '" + description + "' was passed directly. This is less secure." +
                    " Consider using 'file:<file>' or 'console:' for this value instead");
            return source;
        }
    }
}
