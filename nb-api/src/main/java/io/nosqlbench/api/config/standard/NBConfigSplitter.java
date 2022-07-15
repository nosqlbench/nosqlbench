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

package io.nosqlbench.api.config.standard;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class NBConfigSplitter {
    private enum State {
        any,
        json, escaped
    }

    /**
     * Split off any clearly separate config loader specifications from the beginning or end,
     * so they can be composed as an ordered set of config loaders.
     *
     * @param configs The string containing driver config specs as described in the cqld4.md
     *                documentation.
     * @return A list of zero or more strings, each representing a config source
     */
    // for testing
    public static List<String> splitConfigLoaders(String configs) {
        LinkedList<State> states = new LinkedList<>();
        List<String> elements = new ArrayList<>();
        StringBuilder element = new StringBuilder();
        states.push(State.any);

        for (int i = 0; i < configs.length(); i++) {
            State state = states.peek();
            Objects.requireNonNull(state);
            char c = configs.charAt(i);

            if (state == State.escaped) {
                element.append(c);
                states.pop();
            } else if (c == '\\') {
                states.push(State.escaped);
                continue;
            }

            switch (state) {
                case any:
                    if (c == ',') {
                        elements.add(element.toString());
                        element.setLength(0);
                    } else if (c == '{') {
                        states.push(State.json);
                        element.append(c);
                    } else {
                        element.append(c);
                    }
                    break;
                case json:
                    if (c == '{') {
                        states.push(State.json);
                        element.append(c);
                    } else if (c == '}') {
                        states.pop();
                        element.append(c);
                    } else {
                        element.append(c);
                    }
            }

        }
        if (element.length()>0) {
            elements.add(element.toString());
        }

//        List<String> configs = new ArrayList<>();
//        Pattern preconfig = Pattern.compile("(?<pre>(\\w+://.+?)|[a-zA-z0-9_:'/\\\\]+?)\\s*,\\s*(?<rest>.+)");
//        Matcher matcher = preconfig.matcher(configs);
//        while (matcher.matches()) {
//            configs.add(matcher.group("pre"));
//            configs = matcher.group("rest");
//            matcher = preconfig.matcher(configs);
//        }
//        Pattern postconfig = Pattern.compile("(?<head>.+?)\\s*,\\s*(?<post>(\\w+://.+?)|([a-zA-Z0-9_:'/\\\\]+?))");
//        matcher = postconfig.matcher(configs);
//        LinkedList<String> tail = new LinkedList<>();
//        while (matcher.matches()) {
//            tail.push(matcher.group("post"));
//            configs = matcher.group("head");
//            matcher = postconfig.matcher(configs);
//        }
//        if (!configs.isEmpty()) {
//            configs.add(configs);
//        }
//        while (tail.size() > 0) {
//            configs.add(tail.pop());
//        }
//        return configs;

        return elements;
    }


}
