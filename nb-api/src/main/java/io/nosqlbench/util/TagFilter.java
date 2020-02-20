/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.util;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class makes it easy to associate tags and tag values with {@link Tagged}
 * items, filtering matching Tagged items from a set of candidates.
 * <ul>
 * <li><em>tags</em> are the actual tags attached to a {@link Tagged} item.</li>
 * <li><em>filters</em> are the names and values used to filter the tag sets.</li>
 * </ul>
 * Tag names and filter names must be simple words. Filter values can have regex expressions, however.
 * When a filter value starts and ends with a single quote, the quotes are removed as a convencience
 * for deal with shell escapes, etc. This means that value <strong>'five-oh.*five'</strong>
 * is the same as <strong>five-oh.*five</strong>, except that the former will not cause undesirable
 * shell expansion on command lines.
 * <p>
 * When a Tagged item is filtered, the following checks are made for each
 * tag specified in the filter:
 * <ol>
 * <li>The Tagged item must have a tag with the same name as a filter.</li>
 * <li>If the filter has a value in addition to the tag name, then the Tagged item
 * must also have a value for that tag name. Furthermore, the value has to match.</li>
 * <li>If the filter value, converted to a Regex, matches the tag value,
 * it is deemed to be a match.</li>
 * </ol>
 * <p>
 * Because advanced tag usage can sometimes be unintuitive, the tag filtering logic has
 * a built-in log which can explain why a candidate item did or did not match a particular
 * set of filters.
 */
public class TagFilter {
    public static TagFilter MATCH_ALL = new TagFilter("");
    private Map<String, String> filter = new LinkedHashMap<>();

    /**
     * Create a new tag filter. A tag filter is comprised of zero or more tag names, each with an
     * optional value. The tag spec is a simple string format that contains zero or
     * more tag names with optional values.
     * <p>
     * All of the following forms are acceptable for a filter spec:
     * <UL>
     * <li>name1=value1 name2=value2</li>
     * <li>name1:value1, name2=value2</li>
     * <li>name1=value1  name2=value2,name3:value3</li>
     * <li>name1='.*fast.*', name2=1+</li>
     * </UL>
     * <p>
     * That is, you can use spaces or commas between tag (name,value) pairs, and you can also use
     * colons or equals between the actual tag names and values. This is not to support mixed formatting, but it
     * does allow for some flexibility when integrating with other formats. Extra spaces between (name,value)
     * pairs are ignored.</p>
     * <p>As well, you can include regex patterns in your tag filter values. You can also use single quotes to
     * guard against </p>
     * <p>
     * However, the following forms are not acceptable for a tag spec:
     * <dl>
     * <dt>name1: value1</dt>
     * <dd>no extra spaces between the key and value</dd>
     * <dt>name-foo__bar:value1</dt>
     * <dd>No non-word characters in tag names</dd>
     * <dt>name1: value two</dt>
     * <dd>no spaces in tag values</dd>
     * <dt>name1: 'value two'</dt>
     * <dd>no spaces in tag values, even with single-quotes</dd>
     * </dl>
     *
     * @param filterSpec a filter spec as explained in the javadoc
     */
    public TagFilter(String filterSpec) {
        if ((filterSpec != null) && (!filterSpec.isEmpty())) {
            String[] keyvalues = filterSpec.split("[, ] *");
            for (String assignment : keyvalues) {
                String[] keyvalue = assignment.split("[:=]", 2);
                String key = keyvalue[0];
                String value = keyvalue.length > 1 ? keyvalue[1] : null;
                if (value != null) {
                    if ((value.indexOf("\'") == 0) && ((value.indexOf("\'", 1) == (value.length() - 1)))) {
                        value = value.substring(1, value.length() - 1);
                    } else {
                        value = value.trim();
                    }
                }
                filter.put(key, value);
            }
        }
    }

    /**
     * Although this method could early-exit for certain conditions, the full tag matching logic
     * is allowed to complete in order to present more complete diagnostic information back
     * to the user.
     *
     * @param tags The tags associated with a Tagged item.
     * @return a Result telling whether the tags matched and why or why not
     */
    protected Result matches(Map<String, String> tags) {

        List<String> log = new ArrayList<>();
        boolean matched = true;

        for (String filterkey : filter.keySet()) {
            String filterval = filter.get(filterkey);
            String itemval = tags.get(filterkey);


            String detail = "filter(" + filterkey +
                    ((filterval != null) ? ":" + filterval : "") + ") " +
                    "tag(" + ((tags.containsKey(filterkey) ? filterkey : "") +
                    (((tags.get(filterkey) != null) ? ":" + tags.get(filterkey) : "")))
                    + ")";

            if (filterval == null) {
                if (tags.containsKey(filterkey)) {
                    log.add("(☑, ) " + detail + ": matched names");
                } else {
                    log.add("(☐, ) " + detail + ": did not match)");
                    matched = false;
                }
            } else {
                Pattern filterpattern = Pattern.compile("^" + filterval + "$");
                if (itemval == null) {
                    log.add("(☑,☐) " + detail + ": null tag value did not match '" + filterpattern + "'");
                    matched = false;
                } else if (filterpattern.matcher(itemval).matches()) {
                    log.add("(☑,☑) " + detail + ": matched pattern '" + filterpattern + "'");
                } else {
                    log.add("(☑,☐) " + detail + ": did not match '" + filterpattern + "'");
                    matched = false;
                }

            }
        }
        return new Result(matched, log);
    }

    public Result matchesMap(Map<String, String> tags) {
        return matches(tags);
    }

    public Result matchesTaggedResult(Tagged item) {
        return matches(item.getTags());
    }

    public boolean matchesTagged(Tagged item) {
        return matches(item.getTags()).matched();
    }

    public Map<String, String> getMap() {
        return filter;
    }

    public static class Result {
        private boolean matched;
        private List<String> matchLog;

        public Result(boolean matched, List<String> log) {
            this.matched = matched;
            this.matchLog = log;
        }

        public static Result Matched(String reason) {
            return new Result(true, new ArrayList<String>() {{
                add(reason);
            }});
        }

        public static Result Unmatched(String reason) {
            return new Result(false, new ArrayList<String>() {{
                add(reason);
            }});
        }

        public boolean matched() {
            return this.matched;
        }

        public String getLog() {
            return this.matchLog.stream().collect(Collectors.joining("\n"));
        }
    }
}
