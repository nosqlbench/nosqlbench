/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.util;

import io.nosqlbench.docsys.core.PathWalker;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.Scenarios;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.virtdata.api.VirtDataResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NosqlBenchFiles {

    private final static Logger logger = LoggerFactory.getLogger(NosqlBenchFiles.class);

    public static InputStream findRequiredStreamOrFile(String basename, String extension, String... searchPaths) {
        Optional<InputStream> optionalStreamOrFile = findOptionalStreamOrFile(basename, extension, searchPaths);
        return optionalStreamOrFile.orElseThrow(() -> new RuntimeException(
                "Unable to find " + basename + " with extension " + extension + " in file system or in classpath, with"
                        + " search paths: " + Arrays.stream(searchPaths).collect(Collectors.joining(","))
        ));
    }

    public static Optional<InputStream> findOptionalStreamOrFile(String basename, String extension, String... searchPaths) {

        boolean needsExtension = (extension != null && !extension.isEmpty() && !basename.endsWith("." + extension));
        String filename = basename + (needsExtension ? "." + extension : "");

        ArrayList<String> paths = new ArrayList<String>() {{
            add(filename);
            if (!isRemote(basename)) {
            addAll(Arrays.stream(searchPaths).map(s -> s + File.separator + filename)
                    .collect(Collectors.toCollection(ArrayList::new)));
            }

        }};

        for (String path : paths) {
            Optional<InputStream> stream = getInputStream(path);
            if (stream.isPresent()) {
                return stream;
            }
        }

        return Optional.empty();
    }

    public static Optional<Path> findOptionalPath(String basename, String extension, String... searchPaths) {

        boolean needsExtension = (extension != null && !extension.isEmpty() && !basename.endsWith("." + extension));
        String filename = basename + (needsExtension ? "." + extension : "");

        ArrayList<String> paths = new ArrayList<String>() {{
            add(filename);
            if (!isRemote(basename)) {
                addAll(Arrays.stream(searchPaths).map(s -> s + File.separator + filename)
                        .collect(Collectors.toCollection(ArrayList::new)));
            }

        }};

        for (String path : paths) {

            Optional<InputStream> stream = getInputStream(path);
            if (stream.isPresent()) {
                return Optional.of(Path.of(path));
            }
        }

        return Optional.empty();
    }

    private static boolean isRemote(String path) {
        return (path.toLowerCase().startsWith("http:")
                || path.toLowerCase().startsWith("https:"));
    }

    public static Optional<InputStream> getInputStream(String path) {

        // URLs, if http: or https:
        if (isRemote(path)) {
            URL url;
            try {
                url = new URL(path);
                InputStream inputStream = url.openStream();
                if (inputStream!=null) {
                    return Optional.of(inputStream);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Files
        try {
            InputStream stream = new FileInputStream(path);
            return Optional.of(stream);
        } catch (FileNotFoundException ignored) {
        }

        // Classpath
        ClassLoader classLoader = NosqlBenchFiles.class.getClassLoader();
        InputStream stream = classLoader.getResourceAsStream(path);
        if (stream != null) {
            return Optional.of(stream);
        }

        return Optional.empty();
    }

    public static String readFile(String basename) {
        InputStream requiredStreamOrFile = findRequiredStreamOrFile(basename, "");
        try (BufferedReader buffer = new BufferedReader((new InputStreamReader(requiredStreamOrFile)))) {
            String filedata = buffer.lines().collect(Collectors.joining("\n"));
            return filedata;
        } catch (IOException ioe) {
            throw new RuntimeException("Error while reading required file to string", ioe);
        }
    }

    public static Map<String, List<String>> getWorkloadsWithScenarioScripts() {

        String dir = "activities/";

        Path basePath = VirtDataResources.findPathIn(dir);
        List<Path> yamlPathList = PathWalker.findAll(basePath).stream().filter(f -> f.toString().endsWith(".yaml")).collect(Collectors.toList());

        HashMap workloadMap = new HashMap();
        for (Path yamlPath : yamlPathList) {
            String substring = yamlPath.toString().substring(1);
            StmtsDocList stmts = StatementsLoader.load(logger, substring);

            Scenarios scenarios = stmts.getDocScenarios();

            List<String> scenarioNames = scenarios.getScenarioNames();

            if (scenarioNames != null && scenarioNames.size() >0){
                workloadMap.put(yamlPath.getFileName().toString(), scenarioNames);
            }
        }

        return workloadMap;
    }

}
