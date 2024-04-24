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

package io.nosqlbench.engine.core.clientload;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public abstract class LinuxSystemFileReader {
    protected Logger logger;
    protected String filePath;

    public LinuxSystemFileReader(String filePath) {
        logger = LogManager.getLogger(this.getClass());
        this.filePath = filePath;
    }

    public boolean fileExists() {
        Path path = Paths.get(filePath);
        return Files.exists(path);
    }

    protected Double extract(String regex, int groupIdx){
        Pattern pattern = Pattern.compile(regex);
        MatchResult result = findFirstMatch(pattern);
        if (result == null)
            return null;
        assert (1 <= groupIdx && groupIdx <= result.groupCount());
        return Double.valueOf(result.group(groupIdx));
    }

    protected MatchResult findFirstMatch(Pattern pattern) {
        Matcher matcher = null;
        try (FileReader file = new FileReader(filePath);
             BufferedReader reader = new BufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                matcher = pattern.matcher(line);
                if (matcher.find())
                    break;
            }
        } catch (FileNotFoundException e) {
            logger.warn("File not found: " + filePath);
        } catch (final Throwable t) {
            throw new RuntimeException("Failed to read " + filePath);
        }
        if (matcher == null)
            return null;
        return matcher.toMatchResult();
    }

    protected List<MatchResult> findAllLinesMatching(Pattern pattern) {
        List<MatchResult> results = new ArrayList<>();
        Matcher matcher;
        try (FileReader file = new FileReader(filePath);
             BufferedReader reader = new BufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    matcher = pattern.matcher(line);
                    if (matcher.find())
                        results.add(matcher.toMatchResult());
                } catch (Exception e) {
                    logger.error("Error processing line: " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            logger.warn("File not found: " + filePath);
        }
        catch (final Throwable t) {
            throw new RuntimeException("Failed to read " + filePath);
        }
        return results;
    }
}
