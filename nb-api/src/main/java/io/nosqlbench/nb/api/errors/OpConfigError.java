package io.nosqlbench.nb.api.errors;

/**
 * OpConfigErrors are {@link BasicError}s which are known to occur when
 * there is an invalid set of configuration details for an op. This can
 * occur when the user-provided op template data (such as from YAML)
 * is being inspected by a driver adapter to synthesize operations
 * (or functions which know how to do so).
 */
public class OpConfigError extends ActivityInitError {
    private final String configSource;

    public OpConfigError(String error) {
        this(error,null);
    }

    public OpConfigError(String error, String configSource) {
        super(error);
        this.configSource = configSource;
    }


    public OpConfigError(String error, String configSource, Throwable cause) {
        super(error,cause);
        this.configSource = configSource;
    }

    public String getCfgSrc() {
        return configSource;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Error while configuring op from workload template:");

        String cfgsrc = configSource;
        if (cfgsrc==null) {
            Throwable cause = getCause();
            while (cause instanceof OpConfigError && cfgsrc==null) {
                cfgsrc = ((OpConfigError) cause).getCfgSrc();
                cause = cause.getCause();
            }
        }

        if (cfgsrc!=null) {
            sb.append("configsource:" + configSource);
        }

        if (getCause()!=null) {
            StackTraceElement causeFrame = getCause().getStackTrace()[0];
            sb.append("\n\t caused by ")
                .append(getCause().getMessage())
                .append("\n\t at (")
                .append(causeFrame.getFileName())
                .append(":")
                .append(causeFrame.getLineNumber())
                .append(")");
        }

        return sb.toString();
    }
}
