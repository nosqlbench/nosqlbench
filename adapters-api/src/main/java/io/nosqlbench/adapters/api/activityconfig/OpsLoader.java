/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapters.api.activityconfig;

import com.amazonaws.util.StringInputStream;
import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpsDocList;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpsLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplateFormat;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.adapters.api.templating.StrInterpolator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.Option;
import sjsonnet.DefaultParseCache;
import sjsonnet.SjsonnetMain;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OpsLoader {

    private final static Logger logger = LogManager.getLogger(OpsLoader.class);

    public static String[] YAML_EXTENSIONS = new String[]{"yaml", "yml"};

    public static OpsDocList loadContent(Content<?> content, Map<String, String> params) {
        OpTemplateFormat fmt = OpTemplateFormat.valueOfURI(content.getURI());
        return loadString(content.get().toString(), fmt, params, content.getURI());
    }

    public static OpsDocList loadPath(String path, Map<String, ?> params, String... searchPaths) {
        String[] extensions = path.indexOf('.')>-1 ? new String[]{} : YAML_EXTENSIONS;

        Content<?> foundPath = NBIO.all().searchPrefixes(searchPaths).pathname(path).extensionSet(extensions).first()
            .orElseThrow(() -> new RuntimeException("Unable to load path '" + path + "'"));
        OpTemplateFormat fmt = OpTemplateFormat.valueOfURI(foundPath.getURI());
        return loadString(foundPath.asString(), fmt, params, foundPath.getURI());
    }

    public static OpsDocList loadString(final String sourceData, OpTemplateFormat fmt, Map<String, ?> params, URI srcuri) {

        logger.trace(() -> "Applying string transformer to data:" + sourceData);
        StrInterpolator transformer = new StrInterpolator(params);
        String data = transformer.apply(sourceData);
        if (srcuri!=null) {
            logger.info("workload URI: '" + srcuri + "'");
        }

        RawOpsLoader loader = new RawOpsLoader(transformer);
        RawOpsDocList rawOpsDocList = switch (fmt) {
            case jsonnet -> loader.loadString(evaluateJsonnet(srcuri, params));
            case yaml, json -> loader.loadString(data);
            case inline, stmt -> RawOpsDocList.forSingleStatement(data);
        };
        // TODO: itemize inline to support ParamParser

        OpsDocList layered = new OpsDocList(rawOpsDocList);

        transformer.checkpointAccesses().forEach((k, v) -> {
            layered.addTemplateVariable(k, v);
            params.remove(k);
        });

        return layered;
    }

    private static String evaluateJsonnet(URI uri, Map<String, ?> params) {
        List<String> injected = new LinkedList<>(List.of(Path.of(uri).toString()));
        params.forEach((k,v) -> {
            if (v instanceof CharSequence cs) {
                injected.addAll(List.of("--ext-str",k+"="+cs));
            }
        });

        var stdoutBuffer = new ByteArrayOutputStream();
        var stderrBuffer = new ByteArrayOutputStream();
        var stdoutStream = new PrintStream(stdoutBuffer);
        var stderrStream = new PrintStream(stderrBuffer);
        StringInputStream inputStream;
        try {
            inputStream = new StringInputStream("");
        } catch (Exception e) {
            throw new RuntimeException("Error building input stream for jsonnet:" + e, e);
        }

        int resultStatus = SjsonnetMain.main0(
            injected.toArray(new String[0]),
            new DefaultParseCache(),
            inputStream,
            stdoutStream,
            stderrStream,
            new os.Path(Path.of(System.getProperty("user.dir"))),
            Option.empty(),
            Option.empty()
        );

        String stdoutOutput = stdoutBuffer.toString(StandardCharsets.UTF_8);
        String stderrOutput = stderrBuffer.toString(StandardCharsets.UTF_8);
        if ("jsonnet".equals(String.valueOf(params.get("dryrun")))) {
            logger.info("dryrun=jsonnet, dumping result to stdout and stderr:");
            System.out.println(stdoutOutput);
            System.err.println(stderrOutput);
            if (resultStatus==0 && stderrOutput.isEmpty()) {
                logger.info("no errors detected during jsonnet evaluation.");
                System.exit(0);
            } else {
                logger.error("ERRORS detected during jsonnet evaluation:\n" + stderrOutput);
                System.exit(2);
            }
        }
        if (!stderrOutput.isEmpty()) {
            BasicError error = new BasicError("stderr output from jsonnet preprocessing: " + stderrOutput);
            if (resultStatus!=0) {
                throw error;
            } else {
                logger.warn(error.toString(),error);
            }
        }
        logger.info("jsonnet processing read '" + uri +"', rendered " + stdoutOutput.split("\n").length + " lines.");
        logger.trace("jsonnet result:\n" + stdoutOutput);

        return stdoutOutput;
    }

}
