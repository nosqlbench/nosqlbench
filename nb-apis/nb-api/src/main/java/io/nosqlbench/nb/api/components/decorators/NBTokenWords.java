/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.components.decorators;

import java.util.Map;

/**
 * <P>This is a canonical way to get the words which are acceptable and valid
 * for token transformations in strings, particularly in identifiers for
 * external reporting or logging. No other methods should be used to determine
 * which tokens are valid across NB components. All tokens which are deemed
 * useful or necessary should be included here. (This derives from built-in labels)</P>
 */
public interface NBTokenWords {
    Map<String,String> getTokens();
}
