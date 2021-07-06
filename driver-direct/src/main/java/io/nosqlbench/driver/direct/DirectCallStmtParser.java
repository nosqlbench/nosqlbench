package io.nosqlbench.driver.direct;

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
