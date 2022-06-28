/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

}
