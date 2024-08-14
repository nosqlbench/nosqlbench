/*
 * Copyright (c) 2020-2024 nosqlbench
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

package io.nosqlbench.adapter.azureaisearch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;

import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;

/**
 * The {@code AzureAISearchSpace} class is a context object which stores all
 * stateful contextual information needed to interact with the
 * <b>{@code Azure AI Search}</b> database instance.
 *
 * @see <a href=
 *      "https://learn.microsoft.com/en-us/azure/developer/java/sdk/troubleshooting-dependency-version-conflict">Troubleshooting
 *      guide</a>
 * @see <a href=
 *      "https://learn.microsoft.com/en-us/java/api/overview/azure/search-documents-readme?view=azure-java-stable">AI
 *      Search quick start guide</a>
 * @see <a href=
 *      "https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/search/azure-search-documents/">Azure
 *      AI Search Java searchIndexClient</a>
 */
public class AzureAISearchSpace implements AutoCloseable {
	private final static Logger logger = LogManager.getLogger(AzureAISearchSpace.class);
	private final String name;
	private final NBConfiguration cfg;

	protected SearchIndexClient searchIndexClient;

	/**
	 * Create a new {@code AzureAISearchSpace} Object which stores all stateful
	 * contextual information needed to interact with the <b>Azure AI Search</b>
	 * database instance.
	 *
	 * @param name The name of this space
	 * @param cfg  The configuration ({@link NBConfiguration}) for this nb run
	 */
	public AzureAISearchSpace(String name, NBConfiguration cfg) {
		this.name = name;
		this.cfg = cfg;
	}

	public synchronized SearchIndexClient getSearchIndexClient() {
		if (searchIndexClient == null) {
			searchIndexClient = createSearchClients();
		}
		return searchIndexClient;
	}

	private SearchIndexClient createSearchClients() {
		String uri = cfg.get("endpoint");
		var requiredToken = cfg.getOptional("token_file").map(Paths::get).map(tokenFilePath -> {
			try {
				return Files.readAllLines(tokenFilePath).getFirst();
			} catch (IOException e) {
				String error = "Error while reading token from file:" + tokenFilePath;
				logger.error(error, e);
				throw new RuntimeException(e);
			}
		}).orElseGet(() -> cfg.getOptional("token").orElseThrow(() -> new RuntimeException(
				"You must provide either a 'token_file' or a 'token' to configure a Azure AI Search client")));

		logger.info("{}: Creating new Azure AI Search Client with (masked) token/key [{}], uri/endpoint [{}]",
				this.name, AzureAISearchAdapterUtils.maskDigits(requiredToken), uri);

		var searchIndexClientBuilder = new SearchIndexClientBuilder().endpoint(uri);
		if (!requiredToken.isBlank()) {
			searchIndexClientBuilder = searchIndexClientBuilder.credential(new AzureKeyCredential(requiredToken));
		} else {
			TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
			searchIndexClientBuilder = searchIndexClientBuilder.credential(tokenCredential);
		}
		// Should we leave these below to leverage the SearchServiceVersion.getLatest()?
		String apiVersion = cfg.getOptional("api_version").orElse(SearchServiceVersion.V2024_07_01.name());
		logger.warn(
				"Latest search service version supported by this client is '{}', but we're using '{}' version. Ignore this warning if both are same.",
				SearchServiceVersion.getLatest(), apiVersion);
		return searchIndexClientBuilder.serviceVersion(SearchServiceVersion.valueOf(apiVersion)).buildClient();
	}

	public static NBConfigModel getConfigModel() {
		return ConfigModel.of(AzureAISearchSpace.class)
				.add(Param.optional("token_file", String.class, "the file to load the api token/key from"))
				.add(Param.defaultTo("token", "azure-aisearch-admin-key-changeme")
						.setDescription("the Azure AI Search api token/key to use to connect to the database"))
				.add(Param.defaultTo("endpoint", "localhost:8080").setDescription(
						"the URI endpoint in which the database is running. Check out https://learn.microsoft.com/en-us/azure/search/search-create-service-portal."))
				.add(Param.optional("api_version", String.class,
						"the api version to be used. Example 'V2024-07-01'. Defaults to latest service version supported by the SDK client version"))
				.asReadOnly();
	}

	@Override
	public void close() throws Exception {
		searchIndexClient = null;
	}
}
