package io.nosqlbench.engine.api.scripting;

import org.graalvm.polyglot.*;

public class GraalJsEvaluator<T> implements ExprEvaluator<T> {

    private final Class<T> resultType;
    private Context context;
    private Source script;

    public GraalJsEvaluator(Class<T> resultType) {
        this.resultType = resultType;
    }

    private Context getContext() {
        if (context == null) {
            Context.Builder contextSettings = Context.newBuilder("js")
                    .allowHostAccess(HostAccess.ALL)
                    .allowNativeAccess(true)
                    .allowCreateThread(true)
                    .allowIO(true)
                    .allowHostClassLookup(s -> true)
                    .allowHostClassLoading(true)
                    .allowCreateProcess(true)
                    .allowAllAccess(true)
                    .allowEnvironmentAccess(EnvironmentAccess.INHERIT)
                    .allowPolyglotAccess(PolyglotAccess.ALL)
                    .option("js.ecmascript-version", "2020")
                    .option("js.nashorn-compat", "true");
            context = contextSettings.build();
        }
        return context;
    }

    @Override
    public T eval() {
        Value result = getContext().eval(this.script);
        T asType = result.as(resultType);
        return asType;
    }

    @Override
    public ExprEvaluator<T> script(String scriptText) {
        this.script = Source.create("js", scriptText);
        return this;
    }

    @Override
    public ExprEvaluator<T> put(String varName, Object var) {
        getContext().getBindings("js").putMember(varName, var);
        return this;
    }
}
