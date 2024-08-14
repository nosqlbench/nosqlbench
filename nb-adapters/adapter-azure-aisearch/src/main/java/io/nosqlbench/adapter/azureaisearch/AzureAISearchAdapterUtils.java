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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.util.SearchPagedIterable;

public class AzureAISearchAdapterUtils {
	public static final String AZURE_AI_SEARCH = "azure_aisearch";

	public static List<String> splitNames(String input) {
		assert StringUtils.isNotBlank(input) && StringUtils.isNotEmpty(input);
		return Arrays.stream(input.split("( +| *, *)")).filter(StringUtils::isNotBlank).toList();
	}

	public static List<Long> splitLongs(String input) {
		assert StringUtils.isNotBlank(input) && StringUtils.isNotEmpty(input);
		return Arrays.stream(input.split("( +| *, *)")).filter(StringUtils::isNotBlank).map(Long::parseLong).toList();
	}

	/**
	 * Mask the numeric digits in the given string with '*'.
	 *
	 * @param unmasked The string to mask
	 * @return The masked string
	 */
	protected static String maskDigits(String unmasked) {
		assert StringUtils.isNotBlank(unmasked) && StringUtils.isNotEmpty(unmasked);
		int inputLength = unmasked.length();
		StringBuilder masked = new StringBuilder(inputLength);
		for (char ch : unmasked.toCharArray()) {
			if (Character.isDigit(ch)) {
				masked.append("*");
			} else {
				masked.append(ch);
			}
		}
		return masked.toString();
	}

	/**
	 * Prepares an integer array of the indices of keys containing the result
	 * vectors.
	 *
	 * @param field    field to search for the index values of the vectors.
	 * @param response results from which we need to search for the indexes.
	 * @return an {@code int[]} of the indexes of the vectors.
	 */
	public static int[] searchDocumentsResponseIdToIntArray(String field, SearchPagedIterable response) {
		return response.stream().mapToInt(r -> {
			SearchDocument returnObj = r.getDocument(SearchDocument.class);
			return Integer.valueOf((String) returnObj.get(field));
		}).toArray();
	}
}