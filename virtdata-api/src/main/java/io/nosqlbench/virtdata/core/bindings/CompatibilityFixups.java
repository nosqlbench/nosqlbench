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

package io.nosqlbench.virtdata.core.bindings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CompatibilityFixups {

    private final static Logger logger = LogManager.getLogger(CompatibilityFixups.class);

    // Not all of these are simple upper-case changes
    private final static Map<String, String> funcs = new HashMap<String, String>() {{
        put("log_normal", "LogNormal");
        put("normal", "Normal");
        put("levy", "Levy");
        put("nakagami", "Nakagami");
        put("exponential", "Exponential");
        put("logistic", "Logistic");
        put("laplace", "Laplace");
        put("cauchy", "Cauchy");
        put("f", "F");
        put("t", "T");
        put("weibull", "Weibull");
        put("chi_squared", "ChiSquared");
        put("gumbel", "Gumbel");
        put("beta", "Beta");
        put("pareto", "Pareto");
        put("gamma", "Gamma");
        put("uniform_real", "Uniform");
        put("uniform_integer", "Uniform");
        put("hypergeometric", "Hypergeometric");
        put("geometric", "Geometric");
        put("poisson", "Poisson");
        put("zipf", "Zipf");
        put("binomial", "Binomial");
        put("pascal", "Pascal");
    }};
    private static final String MAPTO = "mapto_";
    private static final String HASHTO = "hashto_";
    private static final String COMPUTE = "compute_";
    private static final String INTERPOLATE = "interpolate_";

    private final static Pattern oldcurve = Pattern.compile("(?<name>[\\w_]+)(?<lparen>\\()(?<args>.*?)(?<rparen>\\))");

    private final static CompatibilityFixups instance = new CompatibilityFixups();

    public static String fixup(String spec) {
        String fixed = instance.fix(spec);
        if (!fixed.equals(spec)) {
            logger.warn(spec + "' was preprocessed to '" + fixed + "'. Please change to the new one to avoid this warning.");
        }
        return fixed;
    }

    public String fix(String spec) {
        if (spec == null) {
            throw new RuntimeException("Unable to fixup a spec that is null");
        }
        // Fixup curve ctors. These are not HOF, so local matching will work fine. However, they could occur multiple
        // times within an HOF, so multiple replace is necessary.
        Matcher matcher = oldcurve.matcher(spec);
        StringBuilder out = new StringBuilder(spec.length());
        int start = 0;

        while (matcher.find()) {
            out.append(spec, start, matcher.start());
            String replacement = fixCurveCall(matcher.group("name"), matcher.group("args"));
            out.append(replacement);
            start = matcher.end();
        }
        out.append(spec.substring(start));

        return out.toString();


    }

    private String fixCurveCall(String name, String args) {
        boolean map = false;
        boolean compute = false;
        if (name.contains(MAPTO)) {
            name = name.replaceAll(MAPTO, "");
            map = true;
        }
        if (name.contains(HASHTO)) {
            name = name.replaceAll(HASHTO, "");
            map = false;
        }
        if (name.contains(COMPUTE)) {
            name = name.replaceAll(COMPUTE, "");
            compute = true;
        }
        if (name.contains(INTERPOLATE)) {
            name = name.replaceAll(INTERPOLATE, "");
            compute = false;
        }

        String nameReplacement = funcs.get(name);
        if (nameReplacement != null) {
            name = nameReplacement;
            args = map ? args + ",'map'" : args + ",'hash'";
            args = compute ? args + ",'compute'" : args + ",'interpolate'";
        }
        return name + "(" + args + ")";

    }
}
