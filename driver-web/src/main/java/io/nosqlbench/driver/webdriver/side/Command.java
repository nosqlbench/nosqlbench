package io.nosqlbench.driver.webdriver.side;

import java.util.List;

public class Command {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<List<String>> getTargets() {
        return targets;
    }

    public void setTargets(List<List<String>> targets) {
        this.targets = targets;
    }

    String id;
    String comment;
    String command;
    String target;
    String value;
    List<List<String>> targets;

    @Override
    public String toString() {
        return "Command{" +
            "id='" + id + '\'' +
            ", comment='" + comment + '\'' +
            ", command='" + command + '\'' +
            ", target='" + target + '\'' +
            ", value='" + value + '\'' +
            ", targets=" + targets +
            '}';
    }
}
