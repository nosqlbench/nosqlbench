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

package io.nosqlbench.converters.cql.cqlast;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CQBErrorListener extends BaseErrorListener {
    List<String> errors = new ArrayList<>();

    private final Path origin;

    public CQBErrorListener(Path origin) {
        this.origin = origin;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        String locref= getLocationRef(origin,line, charPositionInLine);
        String errmsg = "Error in " + origin.toString()+ "\n:"+locref;
        errors.add(errmsg);
    }

    public String getLocationRef(Path srcpath, int line, int charPositionInLine) {
        boolean inij = System.getProperty("sun.java.command","").toLowerCase(Locale.ROOT).contains("intellij");
        Path vcwd = Path.of(".").toAbsolutePath().normalize();
        vcwd = inij ? vcwd.getParent().normalize() : vcwd;
        Path relpath = vcwd.relativize(srcpath.toAbsolutePath());
        if (inij) {
            relpath = Path.of("local/verizon/");
//            relpath = Path.of(relpath.toString().replace("target/classes/","src/main/resources/"));
        }
        return "\t at (" + relpath + ":" + line+":"+charPositionInLine + ")";
    }

}
