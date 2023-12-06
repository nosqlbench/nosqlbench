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

package io.nosqlbench.engine.api.activityapi.sysperf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.Optional;

public class SysPerf {
    public final static Logger logger = LogManager.getLogger(SysPerf.class);

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static SysPerfData cachedData;
    private static final long currentImplVersion = 1L;
    private static SysPerf instance;

    private SysPerf() {
    }

    public synchronized static SysPerf get() {
        throw new RuntimeException("This should not be used in this " +
                "release.");
//        if (instance == null) {
//            instance = new SysPerf();
//        }
//        return instance;
    }

    private static File getPerfCacheFile() {
        String sysperfFileName = "/.nosqlbench/sysperf.yaml";
        Optional<File> cacheAt = Optional.ofNullable(System.getenv().get("HOME")).map(s -> s + sysperfFileName).map(File::new);
        return cacheAt.orElseThrow(() -> new RuntimeException("Unable to map file location for " + sysperfFileName));
    }

    /**
     * WARNING: If you are trying to debug this, JMH will not cooperate by default.
     * @param forceRun Force the benchmarks to run
     * @return a SysPerfData performance data summary
     */
    public synchronized SysPerfData getPerfData(boolean forceRun) {
        if (forceRun) {
            logger.debug("forced system perf run");
            cachedData = new SysPerfBaseliner().getSysPerfData();
            save(true);
        }
        cachedData = load();
        if (cachedData == null) {
            logger.debug("lazy system perf run");
            cachedData = new SysPerfBaseliner().getSysPerfData();
            save(true);
        }

        logger.debug("system peformance data: " + cachedData.toString());
        return cachedData;

    }

    public synchronized void reset() {
        try {
            File cache = getPerfCacheFile();
            if (cache.exists()) {
                if (!cache.delete()) {
                    throw new RuntimeException("Could not delete cache file: " + cache.getCanonicalPath());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Optional<FileTime> getCacheFileTime() {
        File cache = getPerfCacheFile();
        try {
            return Optional.of(Files.getLastModifiedTime(cache.toPath()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public synchronized SysPerfData load() {
        File cache = getPerfCacheFile();
        try {
            if (!cache.exists()) {
                return null;
            }
            byte[] bytes = new byte[0];
            bytes = Files.readAllBytes(cache.toPath());
            String perfdata = new String(bytes, CHARSET);

            Load yaml = new Load(LoadSettings.builder().build());
            SysPerfData perfinfo = (SysPerfData) yaml.loadFromString(perfdata);
            cachedData = perfinfo;
            logger.info("Loaded previously cached system timing data from " + cache.getCanonicalPath());
            return cachedData;
        } catch (IOException e) {
            logger.error("error while loading sysperf data",e);
            if (cache.exists()) {
                boolean deleted = cache.delete();
                if (deleted) {
                    logger.warn("Deleted unreadable sysperf file " + cache.getPath());
                } else {
                    logger.error("Error deleting unreadable " + cache.getPath());
                }
            }
            return null;
        }
    }

    public synchronized void save(boolean forceSave) {
        File cache = getPerfCacheFile();
        try {
            if (!cache.exists() || forceSave) {
                Files.createDirectories(cache.toPath().getParent());
                Dump dump = new Dump(DumpSettings.builder().build());
                if (cache.exists()) {
                    cache.delete();
                }
                String filedata = dump.dumpToString(cachedData);
                Files.write(cache.toPath(), filedata.getBytes(CHARSET), StandardOpenOption.CREATE_NEW);
                logger.info("Wrote system timing data to cachefile " + cache.getCanonicalPath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
