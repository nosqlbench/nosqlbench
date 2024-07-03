/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapter.ragstack;

import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class RagstackSpace {
    private final static Logger logger = LogManager.getLogger(RagstackSpace.class);
    private final NBConfiguration config;
    private final String name;

    private String astraToken;
    private String astraApiEndpoint;
    private String openApiKey;
    private String namespace;
    private String collection;

    private PyObject vstore;

    public RagstackSpace(String name, NBConfiguration cfg) {
        this.config = cfg;
        this.name = name;
        setToken();
        setApiEndpoint();
        setOpenApiKey();
        setCollection();
        setupPython();
    }

    public PyObject getVstore() {
        return vstore;
    }

    private void setupPython() {
        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.exec("import os");
            pyInterp.exec("from datasets import load_dataset");
            pyInterp.exec("from dotenv import load_dotenv");
            pyInterp.exec("from langchain_community.document_loaders import PyPDFDirectoryLoader");
            pyInterp.exec("from langchain_astradb import AstraDBVectorStore");
            pyInterp.exec("from langchain_openai import OpenAIEmbeddings");
            pyInterp.exec("from langchain_core.documents import Document");
            pyInterp.exec("vstore = AstraDBVectorStore(embedding=OpenAIEmbeddings(), " +
                "collection_name=" + this.collection + ", " +
                "token=os.environ[\"" + this.astraToken + "\"], " +
                "api_endpoint=os.environ[\"" + this.astraApiEndpoint + "\"])");
            vstore = pyInterp.get("vstore");
        }
    }

    private void setApiEndpoint() {
        Optional<String> epConfig = config.getOptional("astraApiEndpoint");
        Optional<String> epFileConfig = config.getOptional("astraApiEndpointFile");
        if (epConfig.isPresent() && epFileConfig.isPresent()) {
            throw new BasicError("You can only configure one of astraApiEndpoint or astraApiEndpointFile");
        }
        if (epConfig.isEmpty() && epFileConfig.isEmpty()) {
            throw new BasicError("You must configure one of astraApiEndpoint or astraApiEndpointFile");
        }
        epFileConfig
            .map(Path::of)
            .map(p -> {
                try {
                    return Files.readString(p);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .map(String::trim)
            .ifPresent(ep -> this.astraApiEndpoint = ep);
        epConfig.ifPresent(ep -> this.astraApiEndpoint = ep);
    }

    private void setOpenApiKey() {
        Optional<String> oakConfig = config.getOptional("openApiKey");
        Optional<String> oakFileConfig = config.getOptional("openApiKeyFile");
        if (oakConfig.isPresent() && oakFileConfig.isPresent()) {
            throw new BasicError("You can only configure one of openApiKey or openApiKeyFile");
        }
        if (oakConfig.isEmpty() && oakFileConfig.isEmpty()) {
            throw new BasicError("You must configure one of openApiKey or openApiKeyFile");
        }
        oakFileConfig
            .map(Path::of)
            .map(p -> {
                try {
                    return Files.readString(p);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .map(String::trim)
            .ifPresent(ep -> this.openApiKey = ep);
        oakConfig.ifPresent(ep -> this.openApiKey = ep);
    }

    private void setCollection() {
        Optional<String> maybeNamespace = config.getOptional("namespace");
        maybeNamespace.ifPresent(n -> this.namespace = n);
        Optional<String> maybeCollection = config.getOptional("collection");
        maybeCollection.ifPresent(c -> this.collection = c);
    }

    private void setToken() {
        String tokenFileContents = null;
        Optional<String> tokenFilePath = config.getOptional("astraTokenFile");
        if (tokenFilePath.isPresent()) {
            tokenFileContents = getTokenFileContents(tokenFilePath.get());
        }
        this.astraToken = (tokenFileContents != null) ? tokenFileContents : config.get("astraToken");
    }

    private String getTokenFileContents(String filePath) {
        Path path = Paths.get(filePath);
        try {
            return Files.readAllLines(path).getFirst();
        } catch (IOException e) {
            String error = "Error while reading token from file:" + path;
            logger.error(error, e);
            throw new RuntimeException(e);
        }
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(RagstackSpace.class)
            .add(
                Param.optional("astraTokenFile", String.class)
                    .setDescription("file to load the Astra token from")
            )
            .add(
                Param.optional("astraToken", String.class)
                    .setDescription("the Astra token used to connect to the database")
            )
            .add(
                Param.optional("astraApiEndpoint", String.class)
                    .setDescription("the API endpoint for the Astra database")
            )
            .add(
                Param.optional("astraApiEndpointFile", String.class)
                    .setDescription("file to load the API endpoint for the Astra database")
            )
            .add(
                Param.optional("openApiKeyFile", String.class)
                    .setDescription("")
            )
            .add(
                Param.optional("openApiKey", String.class)
                    .setDescription("")
            )
            .add(
                Param.defaultTo("namespace", "default_namespace")
                    .setDescription("The Astra namespace to use")
            )
            .add(
                Param.optional("collection", String.class)
                    .setDescription("optional collection to use")
            )
            .asReadOnly();
    }

}
