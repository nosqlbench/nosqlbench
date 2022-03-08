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

package io.nosqlbench.virtdata.api.processors;

import java.util.List;
import java.util.Map;

public interface DocCtorData {

    /**
     * @return the {@link Class#getSimpleName()} of the documented ctor.
     */
    String getClassName();

    /**
     * @return javadoc for the documented ctor, or null if it isn't provided
     */
    String getCtorJavaDoc();

    /**
     * @return an ordered map of the arguments of the documented constructor in name,type form.
     */
    Map<String, String> getArgs();

    /**
     * @return a list of examples, where each is list of (example syntax, comment..)
     */
    List<List<String>> getExamples();
}
