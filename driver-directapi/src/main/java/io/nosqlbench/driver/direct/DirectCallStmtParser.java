package io.nosqlbench.driver.direct;

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


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectCallStmtParser implements Function<String, Optional<Map<String, Object>>> {

    @Override
    public Optional<Map<String, Object>> apply(String s) {
        Pattern stmtPattern = Pattern.compile(
            "(?<package>[a-z](\\.[a-z]+)?)?(?<class>[A-Z]\\w+)(\\.(?<staticfield>\\w+))?(\\.(?<method>\\w+))\\((?<args>.+)\\)"
        );
        Matcher matcher = stmtPattern.matcher(s);
        if (matcher.matches()) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            List.of("package","class","staticfield","method").forEach(n -> {
               if (matcher.group(n)!=null) {
                   map.put(n,matcher.group(n));
               }
            });
            if (matcher.group("args")!=null) {
                String args = matcher.group("args");
                String[] argsplit = args.split(",");
                for (int i = 0; i < argsplit.length; i++) {
                    String val = argsplit[i];
                    if (val.startsWith("\\") && val.endsWith("\\")) {
                        val = val.substring(1,val.length()-2);
                    } else if (val.startsWith("'") && val.endsWith("'")) {
                        val = val.substring(1,val.length()-2);
                    }
                    map.put("_arg"+i,argsplit[i]);
                }
            }
            return Optional.of(map);
        } else {
            return Optional.empty();
        }
    }
}
