package io.nosqlbench.driver.webdriver.side;

import java.util.List;

public class Test {
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

    public List<Command> getCommands() {
        return commands;
    }

    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    String id;
    String name;
    List<Command> commands;

    @Override
    public String toString() {
        return "Test{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", commands=" + commands +
            '}';
    }
}
