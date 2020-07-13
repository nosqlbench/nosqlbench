package io.nosqlbench.activitytype.http;

import java.io.PrintStream;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class HttpConsoleFormats {

    private final Set<String> includes;
    private final long modulo;

    public HttpConsoleFormats(Set<String> includes) {
        long mod = 1L;
        Set<String> incl = new HashSet<>();

        for (String include : includes) {
            if (include.matches("[0-9]+")) {
                mod = Long.parseLong(include);
            } else if (include.toLowerCase().equals("all")) {
                incl.add("headers");
                incl.add("stats");
                incl.add("content");
            } {
                incl.add(include);
            }
        }
        this.includes = incl;
        this.modulo = mod;
    }

    public void summarizeRequest(HttpRequest request, PrintStream out, long cycle) {
        if ((cycle%modulo)!=0) {
            return;
        }
        out.println("----  REQUEST cycle=" + cycle);
        out.println(" --- " + request.method() + " " + request.uri() + " " + request.version().orElse(HttpClient.Version.HTTP_2));

        if (includes.contains("headers")) {
            out.println("  -- headers:");
            summariseHeaders(request.headers(),out);
        }

        out.println("  -- body length:" + request.bodyPublisher().get().contentLength());
    }

    public void summarizeResponse(HttpResponse<String> response, PrintStream out, long cycle, long nanos) {
        if ((cycle%modulo)!=0) {
            return;
        }
        out.println("----  RESPONSE for cycle=" + cycle + " status=" + response.statusCode() + " took=" + (nanos/1_000_000) + "ms");

        if (includes.contains("stats")) {
            int redirects=0;
            Optional<HttpResponse<String>> walkResponses = response.previousResponse();
            while (walkResponses.isPresent()) {
                walkResponses=walkResponses.get().previousResponse();
                redirects++;
            }
            System.out.println(" redirects = " + redirects);
        }

       summariseHeaders(response.headers(),out);

        if (this.includes.contains("content")) {
            System.out.println("  --  body:");
            System.out.println(response.body());
        }
    }

    private static void summariseHeaders(HttpHeaders headers, PrintStream out) {
        out.println(" ---  headers:");
        headers.map().forEach((k,v) -> {
            out.print("  --- " + k + ":");
            if (v.size()>1) {
                out.println();
                v.forEach( h -> {
                    out.println("    - " + h);
                });
            } else {
                out.println(" " + v.get(0));
            }
        });
    }
}
