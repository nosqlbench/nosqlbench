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
import com.google.gson.GsonBuilder;
import io.nosqlbench.adapters.api.activityconfig.yaml.*;
import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.nb.api.nbio.ResolverChain;
import io.nosqlbench.nb.api.advisor.NBAdvisorException;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpsDocList;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpsLoader;
import io.nosqlbench.adapters.api.templating.StrInterpolator;
import io.nosqlbench.nb.api.expr.GroovyExpressionProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
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

///  This class is responsible for loading op templates.
public class OpsLoader {

    private final static Logger logger = LogManager.getLogger(OpsLoader.class);
    private static final GroovyExpressionProcessor EXPRESSION_PROCESSOR = new GroovyExpressionProcessor();

    public static String[] YAML_EXTENSIONS = new String[]{"yaml", "yml"};

    public static OpsDocList loadContent(Content<?> content, Map<String, String> params) {
        OpTemplateFormat fmt = OpTemplateFormat.valueOfURI(content.getURI());
        return loadString(content.get().toString(), fmt, params, content.getURI());
    }

    // used in OpsDocList (at least)
    public static OpsDocList loadPath(String path, Map<String, ?> params, String... searchPaths) {
        String[] extensions = path.indexOf('.') > -1 ? new String[]{} : YAML_EXTENSIONS;
        ResolverChain chain = new ResolverChain(path);
        Content<?> foundPath = NBIO.chain(chain.getChain()).searchPrefixes(searchPaths).pathname(chain.getPath()).extensionSet(extensions).first()
            .orElseThrow(() -> new RuntimeException("Unable to load path '" + path + "'"));
        OpTemplateFormat fmt = OpTemplateFormat.valueOfURI(foundPath.getURI());
        return loadString(foundPath.asString(), fmt, params, foundPath.getURI());
    }

    public static OpsDocList loadString(
        final String sourceData, OpTemplateFormat fmt, Map<String, ?> params, URI srcuri) {

        if (srcuri != null) {
            logger.info("workload URI: '" + srcuri + "'");
        }
        Map<String, ?> expressionParams = params == null ? Map.of() : Map.copyOf(params);
        StrInterpolator transformer = new StrInterpolator(params);

        RawOpsLoader loader = new RawOpsLoader(transformer);
        RawOpsDocList rawOpsDocList = switch (fmt) {
            case jsonnet -> loader.loadString(EXPRESSION_PROCESSOR.process(evaluateJsonnet(srcuri, params), srcuri, expressionParams));
            case yaml, json -> loader.loadString(EXPRESSION_PROCESSOR.process(sourceData, srcuri, expressionParams));
            case inline, stmt -> RawOpsDocList.forSingleStatement(transformer.apply(EXPRESSION_PROCESSOR.process(sourceData, srcuri, expressionParams)));
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
        params.forEach((k, v) -> {
            if (v instanceof CharSequence cs) {
                injected.addAll(List.of("--ext-str", k + "=" + cs));
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
            injected.toArray(new String[0]), new DefaultParseCache(), inputStream, stdoutStream,
            stderrStream, new os.Path(Path.of(System.getProperty("user.dir"))), Option.empty(),
            Option.empty(), null
        );

        String stdoutOutput = stdoutBuffer.toString(StandardCharsets.UTF_8);
        String stderrOutput = stderrBuffer.toString(StandardCharsets.UTF_8);
        if ("jsonnet".equals(String.valueOf(params.get("dryrun")))) {
            logger.info("dryrun=jsonnet, dumping result to stdout and stderr:");
            System.out.println(stdoutOutput);
            System.err.println(stderrOutput);
            if (resultStatus == 0 && stderrOutput.isEmpty()) {
                logger.info("no errors detected during jsonnet evaluation.");
                throw new NBAdvisorException("dryrun=jsonnet: No errors detected.", 0);
            } else {
                logger.error("ERRORS detected during jsonnet evaluation:\n" + stderrOutput);
                throw new NBAdvisorException("dryrun=jsonnet: Errors detected.", 2);
            }
        }
        if (!stderrOutput.isEmpty()) {
            BasicError error = new BasicError(
                "stderr output from jsonnet preprocessing: " + stderrOutput);
            if (resultStatus != 0) {
                throw error;
            } else {
                logger.warn(error.toString(), error);
            }
        }
        logger.info("jsonnet processing read '" + uri + "', rendered " + stdoutOutput.split(
            "\n").length + " lines.");
        logger.trace("jsonnet result:\n" + stdoutOutput);

        return stdoutOutput;
    }

    // TODO These should not be exception based, use explicit pattern checks instead, or tap
    // into the parsers in a non-exception way
    public static boolean isJson(String workload) {
        try {
            new GsonBuilder().setPrettyPrinting().create().fromJson(workload, Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // TODO These should not be exception based, use explicit pattern checks instead, or tap
    // into the parsers in a non-exception way
    public static boolean isYaml(String workload) {
        try {
            Object result = new Load(LoadSettings.builder().build()).loadFromString(workload);
            return (result instanceof Map);
        } catch (Exception e) {
            return false;
        }
    }
}
