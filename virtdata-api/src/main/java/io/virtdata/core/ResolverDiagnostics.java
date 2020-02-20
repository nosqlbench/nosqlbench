package io.virtdata.core;

import io.virtdata.api.DataMapper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class ResolverDiagnostics {

    private final static Logger logger  = LogManager.getLogger(ResolverDiagnostics.class);private ResolvedFunction resolvedFunction;
    private final StringBuilder log = new StringBuilder();
    private Throwable error;

    public ResolverDiagnostics() {
    }

    public <T> Optional<DataMapper<T>> getOptionalMapper() {
        return Optional.ofNullable(resolvedFunction).map(ResolvedFunction::getFunctionObject).map(DataMapperFunctionMapper::map);

    }

    public Optional<ResolvedFunction> getResolvedFunction() {
        return Optional.ofNullable(getResolvedFunctionOrThrow());
    }

    public ResolvedFunction getResolvedFunctionOrThrow() {
        if (error!=null) {
            throw new RuntimeException(error.getMessage(),error);
        }
        return resolvedFunction;
    }

    public ResolverDiagnostics error(Exception e) {
        this.error = e;
        log.append("ERROR encountered while resolving function:\n");
        log.append(e.toString()).append("\n");
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        e.printStackTrace(pw);
        String stacktrace = writer.toString();

        log.append("stack trace:\n");
        log.append(stacktrace);
        return this;
    }

    public ResolverDiagnostics setResolvedFunction(ResolvedFunction resolvedFunction) {
        this.resolvedFunction = resolvedFunction;
        return this;
    }

    public ResolverDiagnostics trace(String s) {
        logger.trace(s);
        log.append(s).append("\n");
        return this;
    }

    public String toString() {
        return log.toString();
    }

}
