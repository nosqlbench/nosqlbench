/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.nosqlbench.nb.api.expr;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Auto-loads Groovy script libraries from the lib/groovy/ directory to extend
 * the expression evaluation environment. Scripts marked with the {@code @Library}
 * annotation are automatically loaded when the expression processor is initialized.
 *
 * <h2>Library Function Syntax</h2>
 * <p>To make functions available in expressions, define them as closures assigned
 * to variables in your library scripts:</p>
 * <pre>
 * // In lib/groovy/mylib.groovy:
 * /*
 *  * @Library
 *  * My custom library functions
 *  {@literal *}/
 *
 * // Define functions as closures
 * greet = { name -> "Hello, ${name}!" }
 * multiply = { a, b -> a * b }
 * square = { n -> n * n }
 *
 * // Classes are also supported
 * class Calculator {
 *     static int add(int a, int b) { return a + b }
 * }
 * </pre>
 *
 * <p>These functions can then be used in expressions:</p>
 * <pre>
 * {{= greet('World') }}
 * {{= square(5) * multiply(2, 3) }}
 * {{= Calculator.add(10, 20) }}
 * </pre>
 *
 * <h2>Function Documentation</h2>
 * <p>Library functions can be documented using JavaDoc-style comments before the function definition.
 * The first line is used as the synopsis, and the entire comment body is used as the description.
 * Example lines are extracted automatically:</p>
 * <pre>
 * /**
 *  * Get the first n elements of a list.
 *  * Example: {{= take([1, 2, 3, 4, 5], 3) }} produces [1, 2, 3]
 *  {@literal *}/
 * take = { list, n -> list.take(n) }
 * </pre>
 */
@Service(value = ExprFunctionProvider.class, selector = "groovy-libraries")
public class GroovyLibraryAutoLoader implements ExprFunctionProvider {

    private static final Logger LOGGER = LogManager.getLogger(GroovyLibraryAutoLoader.class);
    private static final String DEFAULT_LIBRARY_DIR = "lib/groovy";
    private static final String LIBRARY_MARKER = "@Library";

    // Pattern to match method definitions: def methodName(...) or static def methodName(...)
    private static final Pattern METHOD_PATTERN = Pattern.compile(
        "(?:static\\s+)?def\\s+([A-Za-z_][\\w]*)\\s*\\(",
        Pattern.MULTILINE
    );

    // Stores metadata for library functions indexed by function name
    private final Map<String, ExprFunctionMetadata> libraryMetadata = new LinkedHashMap<>();

    // Stores compiled script classes for metadata extraction
    private final List<Class<?>> loadedScriptClasses = new ArrayList<>();

    /**
     * Loads all Groovy library scripts from the lib/groovy/ directory using the provided shell.
     * Scripts are considered libraries if they contain the @Library marker annotation or comment.
     *
     * <p>This method uses the provided GroovyShell to evaluate library scripts, ensuring that
     * any methods or classes defined in the libraries are available to subsequent script
     * evaluations using the same shell.</p>
     *
     * @param shell the Groovy shell to use for loading libraries
     */
    public void loadLibrariesWithShell(GroovyShell shell) {
        loadLibrariesFromPathWithShell(shell, DEFAULT_LIBRARY_DIR);
    }

    /**
     * Loads all Groovy library scripts from the lib/groovy/ directory into the provided binding.
     * Scripts are considered libraries if they contain the @Library marker annotation or comment.
     *
     * @param binding the Groovy binding to load libraries into
     * @param compilerConfiguration the compiler configuration to use
     * @deprecated Use {@link #loadLibrariesWithShell(GroovyShell)} instead for proper method visibility
     */
    @Deprecated
    public static void loadLibraries(Binding binding, CompilerConfiguration compilerConfiguration) {
        loadLibrariesFromPath(binding, compilerConfiguration, DEFAULT_LIBRARY_DIR);
    }

    /**
     * Loads all Groovy library scripts from the specified directory using the provided shell.
     * Scripts are considered libraries if they contain the @Library marker annotation or comment.
     *
     * @param shell the Groovy shell to use for loading libraries
     * @param libraryDirPath the directory path to search for libraries
     */
    public void loadLibrariesFromPathWithShell(GroovyShell shell, String libraryDirPath) {
        // First, try to load from classpath resources (bundled libraries)
        try {
            loadLibrariesFromClasspath(shell, libraryDirPath);
        } catch (Exception ex) {
            LOGGER.debug("Could not load classpath libraries from: {} - {}", libraryDirPath, ex.getMessage());
            LOGGER.debug("Full exception:", ex);
        }

        // Then, try to load from filesystem (user-provided libraries)
        // Resolve relative paths against user.dir to support tests that change working directory
        Path libraryPath = Paths.get(libraryDirPath);
        if (!libraryPath.isAbsolute()) {
            String userDir = System.getProperty("user.dir");
            libraryPath = Paths.get(userDir, libraryDirPath);
        }

        if (!Files.exists(libraryPath)) {
            LOGGER.debug("Groovy library directory does not exist: {}", libraryPath.toAbsolutePath());
            return;
        }

        if (!Files.isDirectory(libraryPath)) {
            LOGGER.warn("Groovy library path is not a directory: {}", libraryPath.toAbsolutePath());
            return;
        }

        try (Stream<Path> paths = Files.walk(libraryPath)) {
            List<Path> libraryFiles = paths.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".groovy"))
                .toList();

            if (libraryFiles.isEmpty()) {
                LOGGER.debug("No Groovy files found in: {}", libraryPath.toAbsolutePath());
                return;
            }

            for (Path libraryFile : libraryFiles) {
                try {
                    String content = Files.readString(libraryFile);
                    if (content.contains(LIBRARY_MARKER)) {
                        loadLibraryScript(shell, libraryFile.getFileName().toString(), content);
                    }
                } catch (Exception ex) {
                    LOGGER.error("Failed to load Groovy library: {}", libraryFile, ex);
                    throw new RuntimeException("Failed to load Groovy library: " + libraryFile, ex);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error scanning for Groovy libraries in: {}", libraryPath, e);
        }
    }

    /**
     * Load libraries from classpath resources (bundled libraries).
     * This method loads from ALL matching resources on the classpath, not just the first one.
     */
    private void loadLibrariesFromClasspath(GroovyShell shell, String libraryDirPath) throws IOException, URISyntaxException {
        ClassLoader classLoader = GroovyLibraryAutoLoader.class.getClassLoader();
        // Use getResources() to find ALL occurrences of this path on the classpath
        List<URL> resources = Collections.list(classLoader.getResources(libraryDirPath));

        if (resources.isEmpty()) {
            LOGGER.debug("No classpath resources found at: {}", libraryDirPath);
            return;
        }

        LOGGER.debug("Found {} classpath resource location(s) for: {}", resources.size(), libraryDirPath);

        // Load libraries from each resource location
        for (URL resource : resources) {
            loadLibrariesFromResource(shell, resource, libraryDirPath);
        }
    }

    /**
     * Load libraries from a single classpath resource location.
     */
    private void loadLibrariesFromResource(GroovyShell shell, URL resource, String libraryDirPath) throws IOException, URISyntaxException {
        URI uri = resource.toURI();
        Map<String, String> scriptsToLoad = new LinkedHashMap<>();

        synchronized (GroovyLibraryAutoLoader.class) {
            FileSystem fileSystem = null;
            boolean created = false;
            try {
                Path libraryPath;
                if (uri.getScheme().equals("jar")) {
                    // Resource is inside a JAR file
                    try {
                        fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                        created = true;
                    } catch (FileSystemAlreadyExistsException e) {
                        fileSystem = FileSystems.getFileSystem(uri);
                        created = false;
                    }
                    libraryPath = fileSystem.getPath(libraryDirPath);
                } else {
                    // Resource is in the regular filesystem (IDE/test environment)
                    libraryPath = Paths.get(uri);
                }

                if (!Files.exists(libraryPath)) {
                    return;
                }

                // Scan and read all library files while the filesystem is open (if JAR)
                try (Stream<Path> paths = Files.walk(libraryPath)) {
                    List<Path> candidates = paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".groovy"))
                        .toList();

                    for (Path candidate : candidates) {
                        String content = Files.readString(candidate);
                        if (content.contains(LIBRARY_MARKER)) {
                            scriptsToLoad.put(candidate.getFileName().toString(), content);
                        }
                    }
                }
            } finally {
                if (created && fileSystem != null) {
                    try {
                        fileSystem.close();
                    } catch (IOException ex) {
                        LOGGER.warn("Failed to close filesystem for: {}", uri, ex);
                    }
                }
            }
        }

        if (!scriptsToLoad.isEmpty()) {
            LOGGER.debug("Loading {} Groovy library file(s) from classpath resource: {}", scriptsToLoad.size(), resource);
            for (Map.Entry<String, String> entry : scriptsToLoad.entrySet()) {
                try {
                    loadLibraryScript(shell, entry.getKey(), entry.getValue());
                } catch (Exception ex) {
                    LOGGER.error("Failed to load classpath library script: {} from {}", entry.getKey(), resource, ex);
                    throw new RuntimeException("Failed to load classpath library script: " + entry.getKey(), ex);
                }
            }
        }
    }

    /**
     * Loads and executes a Groovy library script from its content.
     */
    private void loadLibraryScript(GroovyShell shell, String name, String content) {
        LOGGER.debug("Loading Groovy library script: {}", name);

        // Parse and evaluate the script - this makes the methods available in subsequent evaluations
        // using the same GroovyShell/Binding
        Script script = shell.parse(content, name);
        script.run();

        // Copy script methods to the binding so they're available for subsequent evaluations
        copyScriptMethodsToBinding(script, shell.getContext());

        // Extract metadata from the script class
        extractMetadataFromScript(script.getClass(), name);

        LOGGER.debug("Successfully loaded Groovy library script: {}", name);
    }

    /**
     * Loads all Groovy library scripts from the specified directory into the provided binding.
     * Scripts are considered libraries if they contain the @Library marker annotation or comment.
     *
     * @param binding the Groovy binding to load libraries into
     * @param compilerConfiguration the compiler configuration to use
     * @param libraryDirPath the directory path to search for libraries
     * @deprecated Use {@link #loadLibrariesFromPathWithShell(GroovyShell, String)} instead
     */
    @Deprecated
    public static void loadLibrariesFromPath(Binding binding, CompilerConfiguration compilerConfiguration, String libraryDirPath) {
        // Resolve relative paths against user.dir to support tests that change working directory
        Path libraryPath = Paths.get(libraryDirPath);
        if (!libraryPath.isAbsolute()) {
            String userDir = System.getProperty("user.dir");
            libraryPath = Paths.get(userDir, libraryDirPath);
        }

        if (!Files.exists(libraryPath)) {
            LOGGER.debug("Groovy library directory does not exist: {}", libraryPath.toAbsolutePath());
            return;
        }

        if (!Files.isDirectory(libraryPath)) {
            LOGGER.warn("Groovy library path is not a directory: {}", libraryPath.toAbsolutePath());
            return;
        }

        GroovyShell shell = new GroovyShell(binding, compilerConfiguration);

        try (Stream<Path> paths = Files.walk(libraryPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".groovy"))
                 .forEach(path -> {
                     try {
                         String content = Files.readString(path);
                         if (content.contains(LIBRARY_MARKER)) {
                             loadLibraryFileStatic(shell, path.getFileName().toString(), content);
                         }
                     } catch (IOException e) {
                         LOGGER.error("Failed to load Groovy library: {}", path, e);
                     }
                 });
        } catch (IOException ex) {
            LOGGER.error("Error scanning for Groovy libraries in: {}", libraryPath, ex);
        }
    }

    /**
     * Static helper for loading a library file without extracting metadata.
     * Used by deprecated static methods.
     */
    private static void loadLibraryFileStatic(GroovyShell shell, String name, String content) {
        LOGGER.debug("Loading Groovy library: {}", name);
        shell.evaluate(content, name);
        LOGGER.debug("Successfully loaded Groovy library: {}", name);
    }

    /**
     * Copies script-level methods into the binding as closures so they're available
     * in subsequent script evaluations.
     */
    private void copyScriptMethodsToBinding(Script script, Binding binding) {
        Class<?> scriptClass = script.getClass();
        Set<String> registeredMethods = new HashSet<>();

        for (java.lang.reflect.Method method : scriptClass.getDeclaredMethods()) {
            // Skip special methods like run() and internal methods
            if (method.getName().equals("run") || method.getName().equals("main") ||
                method.getName().startsWith("$") || method.getName().startsWith("super$") ||
                method.getName().equals("getMetaClass") || method.getName().equals("setMetaClass")) {
                continue;
            }

            String methodName = method.getName();

            // Skip if we've already registered this method name (handles overloaded methods)
            if (registeredMethods.contains(methodName)) {
                continue;
            }
            registeredMethods.add(methodName);

            // Create a closure that uses Groovy's MetaClass to invoke the method
            // This properly handles default parameters, method overloading, and type coercion
            groovy.lang.Closure<?> closure = new groovy.lang.Closure<Object>(script) {
                public Object doCall(Object... args) {
                    try {
                        // Use MetaClass for proper Groovy method invocation
                        return script.getMetaClass().invokeMethod(script, methodName, args);
                    } catch (Exception e) {
                        throw new RuntimeException("Error invoking library method: " + methodName, e);
                    }
                }
            };

            binding.setVariable(methodName, closure);
            LOGGER.debug("Registered library method as closure: {}", methodName);
        }
    }

    /**
     * Extracts function metadata from annotated methods in a Groovy script class.
     *
     * @param scriptClass the compiled script class
     * @param sourceName the source file name for logging
     */
    private void extractMetadataFromScript(Class<?> scriptClass, String sourceName) {
        loadedScriptClasses.add(scriptClass);

        for (java.lang.reflect.Method method : scriptClass.getDeclaredMethods()) {
            io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec spec =
                method.getAnnotation(io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec.class);

            if (spec != null) {
                String functionName = spec.name().isBlank() ? method.getName() : spec.name();
                String synopsis = spec.synopsis().isBlank() ? functionName + "()" : spec.synopsis();

                // Extract examples if present
                List<ExprFunctionExample> examples = new ArrayList<>();
                io.nosqlbench.nb.api.expr.annotations.ExprExample[] exampleAnnotations =
                    method.getAnnotationsByType(io.nosqlbench.nb.api.expr.annotations.ExprExample.class);
                for (io.nosqlbench.nb.api.expr.annotations.ExprExample example : exampleAnnotations) {
                    examples.add(ExprFunctionExample.fromAnnotation(example));
                }

                ExprFunctionMetadata metadata = new ExprFunctionMetadata(
                    functionName,
                    synopsis,
                    spec.description(),
                    examples,
                    "groovy-libraries:" + sourceName
                );

                libraryMetadata.put(functionName, metadata);
                LOGGER.debug("Registered metadata for library function: {} from {}", functionName, sourceName);
            }
        }
    }

    /**
     * Returns metadata for all functions loaded from library scripts.
     * This allows the GroovyLibraryAutoLoader to act as an ExprFunctionProvider
     * and contribute metadata to the expression runtime.
     *
     * @return map of function names to their metadata
     */
    public Map<String, ExprFunctionMetadata> getLibraryMetadata() {
        return Map.copyOf(libraryMetadata);
    }
}
