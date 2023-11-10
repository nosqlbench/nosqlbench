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
public class SessionCommandParser {
//    private final static Logger logger = LogManager.getLogger(SessionCommandParser.class);

    public static Optional<List<Cmd>> parse(
        LinkedList<String> arglist,
        String... includes
    ) {
        List<Cmd> cmdList = new LinkedList<>();
        PathCanonicalizer canonicalizer = new PathCanonicalizer(includes);
        while (arglist.peekFirst() != null) {
            String word = arglist.peekFirst();
            Cmd.CmdType cmdType = Cmd.CmdType.valueOfAnyCase(word);
            if (cmdType!=null) {
                Cmd cmd = Cmd.parseArg(arglist,canonicalizer);
                cmdList.add(cmd);
            } else {
                Optional<Content<?>> scriptfile = NBIO.local()
                    .searchPrefixes("scripts/auto")
                    .pathname(word)
                    .extensionSet("js")
                    .first();
                if (scriptfile.isPresent()) {
                    arglist.removeFirst();
                    arglist.addFirst("scripts/auto/" + word);
                    arglist.addFirst("script");
//                    cmd = Cmd.parseArg(arglist, canonicalizer);
//                    cmdList.add(cmd);
                } else if (NBCLIScenarioParser.isFoundWorkload(word, includes)) {
                    NBCLIScenarioParser.parseScenarioCommand(arglist, includes);
                } else {
                    System.out.println("unrecognized Cmd: " + word); // instead of using logger due to init precedence
                    return Optional.empty();
                }
            }
        }
        return Optional.of(cmdList);

    }
}
