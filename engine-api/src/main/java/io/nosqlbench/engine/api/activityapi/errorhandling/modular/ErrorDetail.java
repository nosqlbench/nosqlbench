package io.nosqlbench.engine.api.activityapi.errorhandling.modular;

public class ErrorDetail {
    public final Retry retryable;
    public final int resultCode;

    public boolean isRetryable() {
        return retryable == Retry.DoRetry;
    }

    public enum Retry {
        DoNotRetry,
        DoRetry,
        Unset
    }

    public ErrorDetail(Retry retryable, int resultCode) {
        this.resultCode = resultCode;
        this.retryable = retryable;
    }

    public ErrorDetail withResultCode(int resultCode) {
        if (this.resultCode == resultCode) {
            return this;
        }
        return new ErrorDetail(this.retryable, resultCode);
    }

    public ErrorDetail withRetryable() {
        if (this.retryable == Retry.DoRetry) {
            return this;
        }
        return new ErrorDetail(Retry.DoRetry, this.resultCode);
    }

    public static ErrorDetail OK = new ErrorDetail(Retry.Unset, 0);
    public static ErrorDetail ERROR_NONRETRYABLE = new ErrorDetail(Retry.DoNotRetry, 127);
    public static ErrorDetail ERROR_RETRYABLE = new ErrorDetail(Retry.DoRetry, 127);
    public static ErrorDetail ERROR_UNKNOWN = new ErrorDetail(Retry.Unset, 127);
}
