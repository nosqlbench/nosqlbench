package io.nosqlbench.engine.api.activityimpl.uniform;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseDriverAdapter<R extends Runnable> implements DriverAdapter<R> {

    @Override
    public final Function<Map<String, Object>, Map<String, Object>> getPreprocessor() {
        List<Function<Map<String,Object>,Map<String,Object>>> mappers = new ArrayList<>();
        List<Function<Map<String,Object>,Map<String,Object>>> stmtRemappers =
            getStmtRemappers().stream()
                .map(m -> new FieldDestructuringMapper("stmt",m))
                .collect(Collectors.toList());
        mappers.addAll(stmtRemappers);
        mappers.addAll(getFieldRemappers());

        if (mappers.size()==0) {
            return (i) -> i;
        }

        Function<Map<String,Object>,Map<String,Object>> remapper = null;
        for (int i = 0; i < mappers.size(); i++) {
            if (i==0) {
                remapper=mappers.get(i);
            } else {
                remapper = remapper.andThen(mappers.get(i));
            }
        }

        return remapper;
    }

    /**
     *
     */
    protected final static class FieldDestructuringMapper implements Function<Map<String,Object>,Map<String,Object>> {

        private final String fieldname;
        private final Function<String, Optional<Map<String, Object>>> thenfunc;

        public FieldDestructuringMapper(String fieldName, Function<String,Optional<Map<String,Object>>> thenfunc) {
            this.fieldname = fieldName;
            this.thenfunc = thenfunc;
        }

        @Override
        public Map<String, Object> apply(Map<String, Object> stringObjectMap) {
            if (stringObjectMap.containsKey(fieldname)) {
                Object o = stringObjectMap.get(fieldname);
                if (o instanceof CharSequence) {
                    String rawfield = o.toString();
                    Optional<Map<String, Object>> optionalResult = thenfunc.apply(rawfield);
                    if (optionalResult.isPresent()) {
                        Map<String, Object> resultmap = optionalResult.get();
                        LinkedHashMap<String, Object> returnmap = new LinkedHashMap<>(stringObjectMap);
                        returnmap.remove(fieldname);
                        resultmap.forEach((k,v)->{
                            if (returnmap.containsKey(k)) {
                                throw new RuntimeException("element '" + k + "' already exist during field remapping.");
                            }
                            returnmap.put(k,v);
                        });
                        return returnmap;
                    } else {
                        return stringObjectMap;
                    }
                } else {
                    throw new RuntimeException("During op mapping, can't parse something that is not a CharSequence: '" + fieldname + "' (type is " + o.getClass().getCanonicalName() + ")");
                }
            } else {
                return stringObjectMap;
            }
        }
    }

    public List<Function<String, Optional<Map<String,Object>>>> getStmtRemappers() {
        return List.of();
    }

    public List<Function<Map<String,Object>,Map<String,Object>>> getFieldRemappers() {
        return List.of();
    }

}
