package io.nosqlbench.engine.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionInfo {

    private final Properties versionProperties = new Properties();

    public VersionInfo() {
        InputStream versionStream = getClass().getResourceAsStream("/version.properties");
        try {
            versionProperties.load(versionStream);
        } catch (IOException e) {
            throw new RuntimeException("unable to read version properties:" + e);
        }
    }

    public String getVersion() {
        return versionProperties.getProperty("version");
    }

    public String getArtifactId() {
        return versionProperties.getProperty("artifactId");
    }

    public String getGroupId() {
        return versionProperties.getProperty("groupId");
    }

    public String getArtifactCoordinates() {
        return "<dependency>\n" +
                " <groupId>" + getGroupId() + "</groupId>\n" +
                " <artifactId>"+ getArtifactId() + "</artifactId>\n" +
                " <version>" + getVersion() + "</version>\n" +
                "</dependency>";
    }

}
