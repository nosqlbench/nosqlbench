/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.cli;

public class SessionNamer {

    public String format(String sessionName) {
        String nameTemplate = sessionName;
        if (nameTemplate==null || nameTemplate.isEmpty()) {
            nameTemplate = "scenario_%tY%tm%td_%tH%tM%tS_%tL";
        }

        int splits = nameTemplate.split("%").length -1;
        Long[] times = new Long[splits];
        long now = System.currentTimeMillis();
        for (int i = 0; i < times.length; i++) times[i] = now;

        sessionName = String.format(nameTemplate, (Object[]) times);

        return sessionName;
    }
}
