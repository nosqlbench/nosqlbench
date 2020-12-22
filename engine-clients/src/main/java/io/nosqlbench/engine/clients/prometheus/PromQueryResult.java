package io.nosqlbench.engine.clients.prometheus;

public class PromQueryResult<T> {
    String status;
    T data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PromQueryResult{" +
                "status='" + status + '\'' +
                ", data=" + data +
                '}';
    }
}
