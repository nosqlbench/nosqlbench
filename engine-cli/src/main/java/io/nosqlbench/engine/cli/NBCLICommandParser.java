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

import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.engine.api.scenarios.NBCLIScenarioParser;

import java.util.*;

/**
 * This parser will return a non-empty optional if there is no error.
 * If the optional is empty, then it means some part of the command structure
 * was not recognized.
 */
public class NBCLICommandParser {

    private static final String FRAGMENT = "fragment";
    private static final String SCRIPT = "script";
    private static final String START = "start";
    private static final String RUN = "run";
    private static final String AWAIT = "await";
    private static final String STOP = "stop";
    private static final String FORCE_STOP = "forceStop";
    private static final String ACTIVITY = "activity";
    private static final String SCENARIO = "scenario";
    private static final String WAIT_MILLIS = "waitmillis";

    public static final Set<String> RESERVED_WORDS = new HashSet<>() {{
        addAll(
                Arrays.asList(
                        FRAGMENT, SCRIPT, START, RUN, AWAIT, STOP, FORCE_STOP, ACTIVITY, SCENARIO, WAIT_MILLIS
                )
        );
    }};

    public static Optional<List<Cmd>> parse(
            LinkedList<String> arglist,
            String... includes
    ) {
        List<Cmd> cmdList = new LinkedList<>();
        PathCanonicalizer canonicalizer = new PathCanonicalizer(includes);
        while (arglist.peekFirst() != null) {
            String word = arglist.peekFirst();
            Cmd cmd;
            switch (word) {
                case FRAGMENT:
                case SCRIPT:
                case START:
                case RUN:
                case AWAIT:
                case STOP:
                case FORCE_STOP:
                case WAIT_MILLIS:
                    cmd = Cmd.parseArg(arglist, canonicalizer);
                    cmdList.add(cmd);
                    break;
                default:
                    Optional<Content<?>> scriptfile = NBIO.local()
                            .prefix("scripts/auto")
                            .name(word)
                            .extension("js")
                            .first();

                    //Script
                    if (scriptfile.isPresent()) {
                        arglist.removeFirst();
                        arglist.addFirst("scripts/auto/" + word);
                        arglist.addFirst("script");
                        cmd = Cmd.parseArg(arglist, canonicalizer);
                        cmdList.add(cmd);
                    } else if (NBCLIScenarioParser.isFoundWorkload(word, includes)) {
                        NBCLIScenarioParser.parseScenarioCommand(arglist, RESERVED_WORDS, includes);
                    } else {
                        return Optional.empty();
                    }
                    break;
            }
        }
        return Optional.of(cmdList);

    }
}
