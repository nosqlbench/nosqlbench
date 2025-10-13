/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionInfo {

    private final Properties versionProperties = new Properties();

    public VersionInfo() {
        InputStream versionStream = getClass().getResourceAsStream("/version.properties");
        try {
            if (versionStream != null) {
                versionProperties.load(versionStream);
            } else {
                // Provide defaults if version.properties is not found
                versionProperties.setProperty("version", "unknown");
                versionProperties.setProperty("artifactId", "nb-engine-cli");
                versionProperties.setProperty("groupId", "io.nosqlbench");
            }
        } catch (IOException e) {
            throw new RuntimeException("unable to read version properties:" + e);
        }
    }

    public String getVersion() {
        return versionProperties.getProperty("version", "unknown");
    }

    public String getArtifactId() {
        return versionProperties.getProperty("artifactId", "unknown");
    }

    public String getGroupId() {
        return versionProperties.getProperty("groupId", "io.nosqlbench");
    }

    public String getGitCommitId() {
        return versionProperties.getProperty("git.commit.id", "unknown");
    }

    public String getGitCommitIdAbbrev() {
        return versionProperties.getProperty("git.commit.id.abbrev", "unknown");
    }

    public String getGitCommitTime() {
        return versionProperties.getProperty("git.commit.time", "unknown");
    }

    public String getBuildTimestamp() {
        return versionProperties.getProperty("build.timestamp", "unknown");
    }

    public String getArtifactCoordinates() {
        return "<dependency>\n" +
                " <groupId>" + getGroupId() + "</groupId>\n" +
                " <artifactId>"+ getArtifactId() + "</artifactId>\n" +
                " <version>" + getVersion() + "</version>\n" +
                "</dependency>";
    }

    public String getDetailedVersion() {
        StringBuilder sb = new StringBuilder();
        sb.append("Version: ").append(getVersion()).append("\n");
        sb.append("Artifact: ").append(getGroupId()).append(":").append(getArtifactId()).append("\n");
        sb.append("Git Commit: ").append(getGitCommitIdAbbrev());
        if (!"unknown".equals(getGitCommitId())) {
            sb.append(" (").append(getGitCommitId()).append(")");
        }
        sb.append("\n");
        sb.append("Git Commit Time: ").append(getGitCommitTime()).append("\n");
        sb.append("Build Time: ").append(getBuildTimestamp());
        return sb.toString();
    }

}
