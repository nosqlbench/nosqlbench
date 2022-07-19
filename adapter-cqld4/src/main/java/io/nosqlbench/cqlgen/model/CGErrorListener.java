/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.cqlgen.model;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class CGErrorListener extends BaseErrorListener implements Supplier<List<String>> {
    List<String> errors = new ArrayList<>();

    private final Path origin;

    public CGErrorListener(Path origin) {
        this.origin = origin;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        String locref= getLocationRef(origin,line, charPositionInLine);
        String errmsg = "Error in " + origin.toString()+ ":\n" +
            e.toString() + ":\n"+locref;
        errors.add(errmsg);
    }

    public String getLocationRef(Path srcpath, int line, int charPositionInLine) {
        Path cwd = Path.of(".").toAbsolutePath().normalize();
        Path vcwd = cwd;

        while (vcwd!=null) {
            if (Files.exists(Path.of(vcwd + File.separator + ".git"))) {
                break;
            }
            vcwd = vcwd.getParent();
        }

        boolean inij = System.getProperty("sun.java.command","").toLowerCase(Locale.ROOT).contains("intellij");
        vcwd = inij ? cwd.getParent().normalize() : vcwd;


        Path path = srcpath.toAbsolutePath();
        if (vcwd!=null) {
            path = vcwd.relativize(srcpath.toAbsolutePath());
        }

        if (inij) {
            path = Path.of(path.toString().replace("target/classes/","src/main/resources/"));
        }
        return "\tat (" + path + ":" + line+":"+charPositionInLine + ")";
    }

    @Override
    public List<String> get() {
        return this.errors;
    }
}
