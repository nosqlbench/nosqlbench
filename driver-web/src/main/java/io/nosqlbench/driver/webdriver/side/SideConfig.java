package io.nosqlbench.driver.webdriver.side;

import java.util.List;

public class SideConfig {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Test> getTests() {
        return tests;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
    }

    String id;
    String name;
    String version;
    String url;
    List<Test> tests;

    @Override
    public String toString() {
        return "SideConfig{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", version='" + version + '\'' +
            ", url='" + url + '\'' +
            ", tests=" + tests +
            '}';
    }
}
