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

package io.nosqlbench.cqlgen.transformers.namecache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.cqlgen.core.CGWorkloadExporter;
import io.nosqlbench.cqlgen.model.CqlKeyspaceDef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class NameCache {
    private final static Logger logger = LogManager.getLogger(CGWorkloadExporter.APPNAME+"/namecache");

    private String path;
    private final Map<String, NamedKeyspace> keyspaces = new LinkedHashMap();

    public NamedKeyspace keyspace(String ksname) {
        return keyspaces.computeIfAbsent(ksname, v -> new NamedKeyspace(ksname));
    }

    public NamedKeyspace computeAlias(CqlKeyspaceDef labeledKs, Function<CqlKeyspaceDef, String> ksfunc) {
        return keyspaces.computeIfAbsent(
            labeledKs.getName(),
            ksname -> new NamedKeyspace(ksname)
                .alias(ksfunc.apply(labeledKs)
                ));
    }

    public static NameCache loadOrCreate(Path path) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (Files.exists(path)) {
            BufferedReader reader = null;
            try {
                reader = Files.newBufferedReader(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            NameCache nameCache = gson.fromJson(reader, NameCache.class);
            nameCache.setPath(path.toString());
            return nameCache;
        } else {
            return new NameCache().setPath(path.toString());
        }
    }

    public NameCache setPath(String path) {
        if (this.path!=null) {
            if (this.path.equals(path)) {
                logger.debug("mapfile unchanged '" + path + "'");
            } else {
                logger.info(() -> "mapfile changed from '" + this.path + "' to '" + path + "'");
                this.path = path;
            }
        }
        return this;
    }

    public NameCache Save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this);
        Path saveto = Path.of(this.path);
        try {
            Files.writeString(saveto, json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Collection<NamedKeyspace> keyspaces() {
        return this.keyspaces.values();
    }

}
