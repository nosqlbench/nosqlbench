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

package io.nosqlbench.engine.cli;

import java.util.List;
import java.util.Map;

/**
 * Add cmd
 */
public interface ScriptBuffer {
    List<Cmd> getCommands();

    /**
     * Add parsed commands to the script buffer
     * @param cmd A parsed command
     * @return This ScriptBuffer
     */
    ScriptBuffer add(Cmd... cmd);

    /**
     * Get the text image of the combined script with
     * all previously added commands included
     * @return The script text
     */
    String getParsedScript();

    /**
     * Get a map which contains all of the params which came from
     * commands of global scope, like {@code script} and {@code fragment} commands.
     * If one of these commands overwrites a named parameter from another,
     * an error should be logged at warning or higher level.
     * @return A globa params map.
     */
    Map<String, String> getCombinedParams();
}
