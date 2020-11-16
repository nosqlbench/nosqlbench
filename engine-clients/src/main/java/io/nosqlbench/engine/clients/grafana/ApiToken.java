package io.nosqlbench.engine.clients.grafana;

public class ApiToken {
    private String name;
    private String key;

    public ApiToken(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "ApiToken{" +
                "name='" + name + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
