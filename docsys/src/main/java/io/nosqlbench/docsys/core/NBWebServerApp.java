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

package io.nosqlbench.docsys.core;

import io.nosqlbench.api.spi.BundledApp;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

@Service(value=NBWebServerApp.class,selector="appserver")
public class NBWebServerApp implements BundledApp {
    private static final Logger logger = LogManager.getLogger(NBWebServerApp.class);

    public static void main(String[] args) {
        new NBWebServerApp().applyAsInt(args);
    }

    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private static void generate(String[] args) throws IOException {
        Path dirpath = args.length == 0 ?
                Path.of("docs") :
                Path.of(args[0]);

        StandardOpenOption[] OVERWRITE = {StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.CREATE,StandardOpenOption.WRITE};

        logger.info(() -> "generating to directory " + dirpath);

    }

    private static void runServer(String[] serverArgs) {
        NBWebServer server = new NBWebServer();
        server.withContextParam("logpath", Path.of("logs")); // default

        for (int i = 0; i < serverArgs.length; i++) {
            String arg = serverArgs[i];
            if (arg.matches(".*://.*")) {
                if (!arg.toLowerCase().contains("http://")) {
                    String suggested = arg.toLowerCase().replaceAll("https", "http");
                    throw new RuntimeException("ERROR:\nIn this release, only 'http://' URLs are supported.\nTLS will be added in a future release.\nSee https://github.com/nosqlbench/nosqlbench/issues/35\n" +
                        "Consider using " + suggested);
                }
                server.withURL(arg);
            } else if (Files.exists(Path.of(arg))) {
                server.addPaths(Path.of(arg));
            } else if (arg.matches("\\d+")) {
                server.withPort(Integer.parseInt(arg));
            } else if (arg.matches("--public")) {
                int nextidx = i+1;
                String net_addr = "0.0.0.0";
                if (
                    serverArgs.length>nextidx+1 &&
                        serverArgs[nextidx].matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+")
                ) {
                    i++;
                    net_addr = serverArgs[nextidx];
                }
                logger.info("running public server on interface with address " + net_addr);
                server.withHost(net_addr);
            } else if (arg.matches("--workspaces")) {
                String workspaces_root = serverArgs[i + 1];
                logger.info("Setting workspace directory to workspace_dir");
                server.withContextParam("workspaces_root", workspaces_root);
            } else if (arg.matches("--logdir")) {
                String logdir_path = serverArgs[i + 1];
                logger.info(() -> "Setting docserver logdir to " + logdir_path);
                server.withContextParam("logpath", Path.of(logdir_path));
            }
        }
//
        server.run();
    }

    private static void showHelp(String... helpArgs) {
        System.out.println(
                "Usage: appserver " +
                    " [url] " +
                    " [path]... " + "\n" +
                    "\n" +
                    "If [url] is provided, then the scheme, address and port are all taken from it.\n" +
                    "Any additional paths are served from the filesystem, in addition to the internal ones.\n" +
                    "\n" +
                    "For now, only http:// is supported."
        );
    }

    private static void search(String[] searchArgs) {
    }

    private static void listTopics() {

    }

    @Override
    public int applyAsInt(String[] args) {
        if (args.length > 0 && args[0].contains("help")) {
            showHelp();
        } else if (args.length > 0 && args[0].contains("generate")) {
            try {
                String[] genargs = Arrays.copyOfRange(args, 1, args.length);
                logger.info(() -> "Generating with args [" + String.join("][", args) + "]");
                generate(genargs);
            } catch (IOException e) {
                logger.error(() -> "could not generate files with command " + String.join(" ", args));
                e.printStackTrace();
            }
        } else {
            runServer(args);
        }
        return 0;
    }
}
