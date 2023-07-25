/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapters.api.util;

import io.nosqlbench.api.errors.OpConfigError;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public class AdaptersApiVersionInfo {

    private final Properties versionProperties = new Properties();

    public AdaptersApiVersionInfo() {
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

    public void assertNewer(String min_version) {
        String[] min_components = min_version.split("\\.|-|_");
        String[] current_components = getVersion().split("\\.|-|_");
        for (int i = 0; i < min_components.length; i++) {
            String minField = min_components[i];
            String currentField = current_components[i];
            if (minField.matches("\\d+")) {
                if (currentField.matches("\\d+")) {
                    if ((Integer.parseInt(currentField) > Integer.parseInt(minField))) {
                        // We're in a completely newer version
                        break;
                    }
                    if (Integer.parseInt(currentField)<Integer.parseInt(minField)) {
                        throw new OpConfigError("This workload can only be loaded by a NoSQLBench runtime version " + min_version + " or higher." +
                            " You are running version " + getVersion());
                    }
                }
                else {
                    throw new OpConfigError("could not compare min_version '" + min_version + " to current version " + getVersion());
                }
            } else {
                throw new OpConfigError("It is impossible to compare min_version " + min_version + " numerically with " + getVersion());
            }
        }
    }

    public void assertVersionPattern(String versionRegex) {
        if (versionRegex!=null) {
            Pattern versionpattern = Pattern.compile(versionRegex);
            String version = new AdaptersApiVersionInfo().getVersion();
            if (!versionpattern.matcher(version).matches()) {
                throw new OpConfigError("Unable to load yaml with this version '" + version + " since " +
                    "the required version doesn't match version_regex '" + versionRegex + "' from yaml.");
            }
        }

    }
}
