/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.adapter.http.apps;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.apps.BundledApp;
import io.nosqlbench.nb.api.nbio.NBIO;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.NonPrintableStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service(value= BundledApp.class,selector = "openapi-exporter")
public class OpenApiExporter implements BundledApp {

    private final static Logger logger = LogManager.getLogger(OpenApiExporter.class);
    private final Map<String,Map<String,String>> typeMapById = new LinkedHashMap<>();

    private static record Argv(
        String infile,
        String outfile, String baseurl, Map<String,String> headers,
        Set<String> enableMethods,
        int limit
    ) {}

    private Argv argv;

    @Override
    public int applyAsInt(String[] args) {
        argv = parseArgs(args);
        String source_spec = NBIO.fs().pathname(argv.infile).first()
            .orElseThrow(() -> new RuntimeException("unable to read " + argv.infile))
            .asString();

        // PARSE
        OpenAPIV3Parser parser = new OpenAPIV3Parser();
        SwaggerParseResult parseResult = parser.readContents(source_spec);
        OpenAPI model = parseResult.getOpenAPI();

        // TRANSFORM
        Map<String,Object> workload = new LinkedHashMap<>();
        Map<String,Map<String,String>> scenarios = new LinkedHashMap<>();
        scenarios.put("default",Map.of("all","run driver=http"));
        workload.put("scenarios",scenarios);
        workload.put("params",Map.of("ok-status",".+"));

        Map<String,Object> transformed = transformModelToWorkload(model);
        Map<String, String> bindings = stubBindings(typeMapById);
        bindings.forEach((k,v) -> { System.out.println(k+":"+v); });
        workload.put("bindings", bindings);
        workload.putAll(transformed);


        DumpSettings dumpSettings = DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndent(2)
            .setDefaultScalarStyle(ScalarStyle.PLAIN)
            .setMaxSimpleKeyLength(1000)
            .setWidth(100)
            .setSplitLines(true)
            .setIndentWithIndicator(true)
            .setMultiLineFlow(true)
            .setNonPrintableStyle(NonPrintableStyle.ESCAPE)
            .build();

        // DUMP FILE
        Dump dump = new Dump(dumpSettings);
        String workloadText = dump.dumpToString(workload);
        PrintStream outstream = System.out;
        if (argv.outfile()!=null && !argv.outfile().isEmpty()) {
            Path outpath = Path.of(argv.outfile());
            if (Files.isRegularFile(outpath)) {
                logger.warn("overwriting file " + outpath.toString());
            }
            try {
                outstream= new PrintStream(Files.newOutputStream(outpath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        outstream.println(workloadText);
        return 0;
    }

    private Map<String, String> stubBindings(Map<String, Map<String, String>> typeMapById) {
        Map<String,String> stub = new LinkedHashMap<>();
        for (String id : typeMapById.keySet()) {
            Map<String, String> namesAndTypes = typeMapById.get(id);
            for (String varname : namesAndTypes.keySet()) {
                String vartype = namesAndTypes.get(varname);
                String defaultDef = switch (vartype) {
                    case "string" -> "ToString();";
                    default -> "Identity()";
                };
                stub.put("_" + id + "_" + varname, vartype);
                stub.put(varname,defaultDef);
            }
        }
        return stub;
    }

    public OpenApiExporter() {
    }

    private Map<String, Object> transformModelToWorkload(OpenAPI model) {
        Map<String,Object> workload = new LinkedHashMap<>();
        Map<String,Object> opsblock = new LinkedHashMap<>();
        Map<String,Object> opdefs = new LinkedHashMap<>();

        Paths paths = model.getPaths();
        ArrayList<String> sortedPaths = new ArrayList<>(paths.keySet());
        Collections.sort(sortedPaths);
        for (String pathName : sortedPaths) {
            PathItem pathItem = paths.get(pathName);
            Map<PathItem.HttpMethod, Operation> opmap = pathItem.readOperationsMap();

            for (PathItem.HttpMethod opMethod : opmap.keySet()) {
                Operation op = opmap.get(opMethod);
                if (!argv.enableMethods.isEmpty() && !argv.enableMethods.contains(opMethod.name().toLowerCase())) {
                    System.err.println("skipping method " + opMethod.name() + " id:" + op.getOperationId());
                    continue;
                }
                Operation opInfo = opmap.get(opMethod);
                Map<String,Object> opdef = new LinkedHashMap<>();
                opdef.put("op",buildRequestBlock(model, pathName, pathItem, opMethod, op));
                Optional.ofNullable(opInfo.getSummary()).ifPresent(summary -> opdef.put("description",summary));
                Optional.ofNullable(opInfo.getDescription()).ifPresent(summary -> opdef.put("description",summary));
                opdefs.put(opInfo.getOperationId()+"_"+opMethod.name().toLowerCase(),opdef);

                if (argv.limit()>0 && opdefs.size()>=argv.limit()) {
                    System.err.println("stopping after " + argv.limit() + " templates.");
                    break;
                }
            }
            if (argv.limit()>0 && opdefs.size()>=argv.limit()) {
                System.err.println("stopping after " + argv.limit() + " templates.");
                break;
            }

        }
        opsblock.put("ops",opdefs);
        workload.put("blocks",Map.of("block1",opsblock));

        return workload;
    }

    public static void main(String[] args) {
        int exitCode = new OpenApiExporter().applyAsInt(args);
        System.exit(exitCode);
    }

    private Argv parseArgs(String[] args) {
        LinkedList<String> argv = new LinkedList<>(Arrays.asList(args));
        Set<String> enabledMethods = new HashSet<>();
        Map<String,String> headers = new LinkedHashMap<>();
        ListIterator<String> iter = argv.listIterator();
        String baseurl = "{baseurl}";
        String infile = "specify-me.yaml";
        int limit = 0;
        String outfile = null;
        while (iter.hasNext()) {
            String word = iter.next();
            if (word.equals("-h")) {
                String header = iter.next();
                String[] parts = header.split(":", 2);
                if (parts.length==1) {
                    throw new RuntimeException("You MUST provide aux headers in the name: value form. This one was not in that form: '" + header + "'");
                }
                headers.put(parts[0],parts[1].trim());
            } else if (word.equals("-m")) {
                String enable=iter.next();
                io.swagger.models.Method enabledMethod = io.swagger.models.Method.forValue(enable);
                enabledMethods.add(enabledMethod.name().toLowerCase());
            } else if (word.equals("-l")) {
                limit = Integer.parseInt(iter.next());
            } else if (word.equals("-b")) {
                baseurl = iter.next();
            } else if (word.equals("-I")) {
                infile = iter.next();
            } else if (word.equals("-O")) {
                outfile = iter.next();
            } else {
                System.out.println("""
                    EXAMPLE:           nb5 openapi-exporter -I myspec.yaml -O myworkload.yaml -b "TEMPLATE(baseurl)"

                    -I <inputfile>     Set the input file for the OpenAPI spec, as in:
                                       -I myspec.yaml

                    [-O <outputfile>]  Set the output file for the workload, as in:
                                       -O myworkload.yaml
                                       If unspecified, the result is printed to stdout.

                    [-m [method]]      Include methods in generated workload. This argument can be specified multiple times, as in:
                                       -m get -m post
                                       If unspecified, then all methods are included by default.

                    [-h "<header>"]    Add a header to each request. This can be specified multiple times, as in:
                                       -h "Authorization: Bearer sometoken" -h "X-Special-Header: I love spicy chicken wings."
                                       If unspecified, then no additional headers are added beyond those in the spec.

                    [-l <limit> ]      Limit the generated op templates to some number, usually used to test a small scale
                                       version of the rendered workload before doing the full scale test, as in:
                                       -l 10
                                       If unspecified, all operations included in the spec are rendered as op templates.

                    [-b <baseurl> ]    Set the base url which will be used with every request. This is often set to
                                       a template var form and the specified when the workload is run, as in:
                                       -b "TEMPLATE(baseurl)" ...
                                       If unspecified "{baseurl}" is used, which will require a binding to be set for it.
                    """);
            }
        }
        return new Argv(infile, outfile, baseurl, headers,enabledMethods, limit);
    }

//    private Map<String,Object> buildOpBodyTemplate(OpenAPI model, String path, PathItem pathitem, PathItem.HttpMethod method, Operation op) {
//        Map<String,Object> opfields = new LinkedHashMap<>();
//        opfields.put("op", buildRequestBlock(model, path, pathitem, method, op));
//        return opfields;
//    }

    private String buildRequestBlock(OpenAPI model, String path, PathItem pathinfo, PathItem.HttpMethod method, Operation op) {
        StringBuilder body = new StringBuilder();
        Map<String, String> headers = new LinkedHashMap<>();

//        String urlPath = buildUrlPath(model, path, pathinfo, op);
        // Main Line
        body.append(method.name()).append(" ").append(argv.baseurl()+path).append(" ").append("HTTP/2.0").append("\n");

        Curl curlData= loadCurlData(op);
        headers.putAll(curlData.headers);
        // Overlay/Overwrite any duplicates
        headers.putAll(argv.headers);
        headers.forEach((k,v) -> {
            body.append(k).append(": ").append(v).append("\n");
        });

        if (op.getParameters()!=null) {
            List<Parameter> requiredQueryParams = op.getParameters().stream()
                .filter(Parameter::getRequired)
//                .filter(p -> p.getIn().equals("query"))
                .toList();
            for (Parameter paramModel : requiredQueryParams) {
                paramModel = getRefOr(model,paramModel);
                updateBindings(op, paramModel.getName(), getRefOr(model, paramModel.getSchema()).getType());
            }
        }

        RequestBody rqBody = op.getRequestBody();
        if (rqBody!=null) {
            Content content = rqBody.getContent();
            if (content.size()>1) {
                logger.warn("There were multiple types of content supported for " + op.getOperationId() + ", but we are using the first one only.");
            }
            Map.Entry<String, MediaType> contentEntry = content.entrySet().iterator().next();
            String contentType = contentEntry.getKey();
            MediaType firstBodyMediaType = contentEntry.getValue();

            Schema bodySchema = getRefOr(model,firstBodyMediaType.getSchema());
            BodySchemaAssembly contentAssembly = new BodySchemaAssembly();
            String bodyTemplate = contentAssembly.assembleBodyTemplate(model, "body", bodySchema, path, pathinfo, op);
            Map<String, String> bindingCache = contentAssembly.getBindingCache();
            typeMapById.computeIfAbsent(op.getOperationId(),i -> new LinkedHashMap<>()).putAll(bindingCache);

            MediaType mediaType = content.get(contentType);

            body.append("\n\n").append(bodyTemplate);
        }
        return body.toString();
    }

    private void updateBindings(Operation op, String name, String type) {
        Map<String, String> idmap = typeMapById.computeIfAbsent(op.getOperationId(), k -> new LinkedHashMap<>());
        if (!idmap.containsKey(op.getOperationId())) {
            idmap.put(name,type);
        }
    }

    private Curl loadCurlData(Operation op) {
        if (op.getExtensions().containsKey("x-oaiMeta")) {
            Map<String,Object> xoaiMeta = (Map<String,Object>) op.getExtensions().get("x-oaiMeta");
            if (xoaiMeta!=null && xoaiMeta.containsKey("examples")) {
                String curltext;
                Object examples = xoaiMeta.get("examples");
                if (examples instanceof Map map) {
                    if (map.containsKey("request")) {
                        Object requestObject = map.get("request");
                        if (requestObject instanceof Map rqmap) {
                            if (rqmap.containsKey("curl")) {
                                curltext = rqmap.get("curl").toString();
                                Curl curlModel = parseCurl(curltext);
                                return curlModel;
                            }
                        }

                    }
                } else if (examples instanceof List list) {
                    if (!(list.get(0) instanceof Map lm)) {
                        throw new RuntimeException("unknown inner type:" + list.get(0).getClass());
                    }
                    List<Map<String,Map<String,String>>> exampleList = new ArrayList<>();
                    for (Object o : list) {
                        exampleList.add((Map<String,Map<String,String>>)o);
                    }
                    List<Map<String, Map<String, String>>> curlEntry = exampleList.stream().filter(m -> m.containsKey("request") && m.get("request").containsKey("curl")).toList();
                    curltext = curlEntry.get(0).get("request").get("curl");
                    Curl curlModel = parseCurl(curltext);
                    return curlModel;
                } else {
                    throw new RuntimeException("unknown type for curl extraction: " + examples);
                }
            } else {
                logger.warn("no x-oaiMeta/examples to pull example headers and uri from");
            }
        } else {
            logger.warn("No x-oaiMeta to pull example headers and uri from");
        }
        return new Curl(null,Map.of());

    }

    private final static Pattern urlPattern = Pattern.compile(" *curl( -X POST)? +\"?(?<url>https:\\/\\/[^ \"]+)\"? *\\\\(?<remainder>.*)$", Pattern.DOTALL | Pattern.MULTILINE);
    private final static Pattern headersPattern = Pattern.compile(" *-H +\"(?<name>[^:]+) +(?<value>[^\"]+)\"",Pattern.DOTALL|Pattern.MULTILINE);
    private final record Curl(
        URI uri,
        Map<String,String> headers
    ) {}
    private Curl parseCurl(String curl) {
        Map<String,String> headers = new LinkedHashMap<>();
        Matcher urlMatcher = urlPattern.matcher(curl);
        if (urlMatcher.matches()) {
            String url = urlMatcher.group("url");
            URI uri = URI.create(url);
            String remainder = urlMatcher.group("remainder");
            Matcher headerMatcher = headersPattern.matcher(remainder);
            while (headerMatcher.find()) {
                String name = headerMatcher.group("name");
                String value = headerMatcher.group("value");
                headers.put(name,value);
            }
            return new Curl(uri,headers);
        } else {
            throw new RuntimeException("Unable to parse curl example:\n"+curl);
        }


    }


    public static <T> T getRefOr(OpenAPI model, T or) {
        if (or==null) {
            return null;
        }
        String modelref = null;
        try {
            Method refmethod = or.getClass().getMethod("get$ref");
            modelref = refmethod.invoke(or).toString();
        } catch (Exception ignored) {}
        if (modelref==null) {
            return or;
        }
        final String ref = modelref;
        Class<T> orClass = (Class<T>)or.getClass();


        String[] ids = modelref.split("/");
        return switch (ids[0]) {
            case "#" -> switch (ids[1]) {
                case "components" -> switch(ids[2]) {
                    case "schemas" -> Optional.ofNullable(orClass.cast(model.getComponents().getSchemas().get(ids[3])))
                        .orElseThrow(() -> new RuntimeException("schema " + ids[3] + " not found for path " + ref));
                    default -> throw new RuntimeException("Add support: component type '" + ids[2] + " not found for path " + ref);
                };
                default -> throw new RuntimeException("Add support: components not found for path " + ref);
            };
            default -> throw new RuntimeException("unrecognized ref format: '" + ref + "'");
        };
    }

}
