package io.nosqlbench.driver.webdriver.side;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
