package io.nosqlbench.engine.api.activityimpl;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.engine.api.templating.CommandTemplate;

import java.util.Map;

public class DiagRunnableOpDispenser<O extends Runnable> implements OpDispenser<Runnable> {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final CommandTemplate cmdTpl;

    public DiagRunnableOpDispenser(CommandTemplate commandTemplate) {
        this.cmdTpl = commandTemplate;
    }

    @Override
    public Runnable apply(long value) {
        Map<String, String> command = cmdTpl.getCommand(value);
        String body = gson.toJson(command);
        return new DiagRunnableOp(body);
    }
}
