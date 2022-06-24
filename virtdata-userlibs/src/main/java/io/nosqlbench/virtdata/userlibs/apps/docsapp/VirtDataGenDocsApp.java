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

package io.nosqlbench.virtdata.userlibs.apps.docsapp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.processors.DocFuncData;
import io.nosqlbench.virtdata.core.bindings.VirtDataDocs;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs.FDoc;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs.FDocCat;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs.FDocFunc;
import io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs.FDocFuncs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class VirtDataGenDocsApp implements Runnable {

    private final static Logger logger = LogManager.getLogger(VirtDataGenDocsApp.class);

    private final static String CATEGORIES = "categories";
    private final static String CATEGORIES_SPLIT = "split";
    private final static String CATEGORIES_COMBINED = "combined";

    private final static String FORMAT = "format";
    private static final String FORMAT_MARKDOWN = "markdown";
    private static final String FORMAT_JSON = "json";

    private final static String BLURBS_DIRS = "blurbsdirs";

    private final static String BASE_FILENAME = "funcref";

    private final String[] args;
    private final Map<String, Writer> writers = new HashMap<>();

    private String baseFileName = BASE_FILENAME;
    private String categories = CATEGORIES_SPLIT;
    private String format = FORMAT_MARKDOWN;

    private String blurbsDirs = "docs/category_blurbs:src/main/resources/docs/category_blurbs:virtdata-userlibs/src/main/resources/docs/category_blurbs";
    private String basedir = "";

    public static void main(String[] args) {
        new VirtDataGenDocsApp(args).run();
    }

    public VirtDataGenDocsApp(String[] args) {
        this.args = args;
    }

    public void run() {
        LinkedList<String> largs = new LinkedList<>(Arrays.asList(args));
        if (args.length > 0 && args[0].contains("help")) {
            System.out.println(
                "usage:\n" +
                    "[basefile <name>] [basedir <dir>] [categories combined|split] [format json|markdown] " +
                    "[blurbsdirs <dir>[:...]]\n\n"
            );
            return;
        }
        while (largs.peekFirst() != null) {
            String argtype = largs.removeFirst();
            if (largs.peekFirst() == null) {
                throw new RuntimeException(VirtDataGenDocsApp.class + " expects args in param value couplets.");
            }

            String argval = largs.removeFirst().toLowerCase();
            switch (argtype) {
                case "basefile":
                    this.baseFileName = argval;
                    break;
                case "basedir":
                    this.basedir = argval;
                    break;
                case BLURBS_DIRS:
                    this.blurbsDirs = argval;
                    break;
                case CATEGORIES:
                    if (!argval.equals(CATEGORIES_SPLIT) && !argval.equals(CATEGORIES_COMBINED)) {
                        throw new RuntimeException("categories must either be " + CATEGORIES_SPLIT + ", or " + CATEGORIES_COMBINED + ".");
                    }
                    this.categories = argval;
                    break;
                case FORMAT:
                    if (!argval.equals(FORMAT_MARKDOWN) && !argval.equals(FORMAT_JSON)) {
                        throw new RuntimeException("format must either be " + FORMAT_MARKDOWN + ", or " + FORMAT_JSON + ".");
                    }
                    this.format = argval;
                    break;
                default:
            }
        }

        Optional<FDoc> docsinfo = loadAllDocs();

        if (!docsinfo.isPresent()) {
            return;
        }

        try {
            String extension = (this.format.equals(FORMAT_MARKDOWN)) ? ".md" : ".json";

            for (FDocCat docsForCatName : docsinfo.get()) {
                String categoryName = docsForCatName.getCategoryName();
                categoryName = categoryName.isEmpty() ? "EMPTY" : categoryName;

                String filename = this.baseFileName
                    + (this.categories.equals(CATEGORIES_SPLIT) ? "_" + categoryName : "")
                    + extension;

                Writer writer = getWriterFor(filename);

                for (FDocFuncs docsForFuncName : docsForCatName) {
                    if (format.equals(FORMAT_JSON)) {
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        writer.append(gson.toJson(docsForFuncName));
                    } else if (format.equals(FORMAT_MARKDOWN)) {
                        String markdown = docsForFuncName.asMarkdown();
                        writer.append(markdown);
                    }
                }
            }
            for (Writer writer : writers.values()) {
                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Writer getWriterFor(String outputname) {
        FileWriter fileWriter = null;
        if (!writers.containsKey(outputname)) {
            try {
                outputname = basedir.isEmpty() ? outputname : basedir + "/" + outputname;
                Path parent = Path.of(outputname).getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                fileWriter = new FileWriter(outputname, false);
                writers.put(outputname, fileWriter);

                String[] blurbsdirs = blurbsDirs.split(":");
                for (String blurbsdir : blurbsdirs) {
                    Optional<Path> bdir = NBIO.findFirstLocalPath(blurbsdir + "/");
                    if (bdir.isPresent()) {
                        Path blurbsFile = bdir.get().resolve(Path.of(outputname).getFileName().toString());
                        if (Files.exists(blurbsFile)) {
                            String blurb = Files.readString(blurbsFile, StandardCharsets.UTF_8);

                            logger.debug("writing blurb to " + outputname);
                            fileWriter.append(blurb);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return writers.get(outputname);
    }

    private Optional<FDoc> loadAllDocs() {
        List<String> errors = new ArrayList<>();
        FDoc docsinfo = new FDoc();
        List<DocFuncData> allDocs = VirtDataDocs.getAllDocs();

        for (DocFuncData docFuncData : allDocs) {
            FDocFunc fDocFunc = new FDocFunc(docFuncData);
            Set<Category> categories = fDocFunc.getCategories();
            if (categories.size() == 0) {
                for (FDocCat knownCategory : docsinfo) {
                    for (FDocFuncs knownFunctionDocs : knownCategory) {
                        if (knownFunctionDocs.getFunctionName().equals(fDocFunc.getFuncName())) {
                            categories = knownFunctionDocs.iterator().next().getCategories();
                            break;
                        }
                    }
                    if (categories.size() > 0) {
                        break;
                    }
                }
            }

            if (categories.size()==0) {
                categories = Set.of(Category.general);
                errors.add("function " + fDocFunc.getFuncName() + " had no categories assigned.");

            }

            for (Category categoryName : categories) {
                FDocCat fDocCat = docsinfo.addCategory(categoryName.toString());
                fDocCat.addFunctionDoc(fDocFunc);
            }
        }
        if (errors.size()>0) {
            errors.forEach(System.out::println);
            return Optional.empty();
        } else {
            return Optional.of(docsinfo);
        }
    }

}
