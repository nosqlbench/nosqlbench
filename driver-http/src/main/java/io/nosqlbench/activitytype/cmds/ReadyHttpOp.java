package io.nosqlbench.activitytype.cmds;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import io.nosqlbench.nb.api.errors.BasicError;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongFunction;

public class ReadyHttpOp implements LongFunction<HttpOp> {

    private final CommandTemplate propertyTemplate;

    // only populated if there is no value which is an actual bindings template
    private final HttpOp cachedOp;

    public ReadyHttpOp(OpTemplate stmtDef) {
        propertyTemplate = new CommandTemplate(stmtDef,
                List.of(
                        HttpFormatParser::parseUrl,
                        HttpFormatParser::parseInline
                )
        );

        if (propertyTemplate.isStatic()) {
            cachedOp = apply(0);
        } else {
            cachedOp = null;
        }
    }

    @Override
    public HttpOp apply(long value) {

        // If the request is invariant, simply return it, since it is thread-safe
        if (this.cachedOp != null) {
            return this.cachedOp;
        }

        Map<String, String> cmd = propertyTemplate.getCommand(value);

        HttpRequest.Builder builder = HttpRequest.newBuilder();

        HttpRequest.BodyPublisher bodyPublisher = cmd.containsKey("body") ?
                HttpRequest.BodyPublishers.ofString(cmd.remove("body"))
                : HttpRequest.BodyPublishers.noBody();

        String method = cmd.containsKey("method") ? cmd.remove("method") : "GET";

        builder.method(method, bodyPublisher);

        if (cmd.containsKey("version")) {
            String versionName = cmd.remove("version")
                    .replaceAll("/1.1", "_1_1")
                    .replaceAll("/2.0", "_2");
            HttpClient.Version version = HttpClient.Version.valueOf(versionName);
            builder.version(version);
        }

        if (cmd.containsKey("uri")) {
            URI uri = URI.create(cmd.remove("uri"));
            builder.uri(uri);
        }

        Set<String> headers = cmd.keySet();
        for (String header : headers) {
            if (header.charAt(0) >= 'A' && header.charAt(0) <= 'Z') {
                builder.header(header, cmd.remove(header));
            } else {
                throw new BasicError("HTTP request parameter '" + header + "' was not recognized as a basic request parameter, and it is not capitalized to indicate that it is a header.");
            }
        }

        String ok_status = cmd.remove("ok-status");
        String ok_body = cmd.remove("ok-body");

        if (cmd.size()>0) {
            throw new BasicError("Some provided request fields were not used: " + cmd.toString());
        }

        HttpRequest request = builder.build();
        return new HttpOp(request, ok_status, ok_body);
    }

}
