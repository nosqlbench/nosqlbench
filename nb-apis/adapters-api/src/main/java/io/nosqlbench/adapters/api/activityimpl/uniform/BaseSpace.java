package io.nosqlbench.adapters.api.activityimpl.uniform;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 This example of a space uses the <EM>SelfT</EM> technique to enable
 the self type to be used in method signatures and return types.

 {@inheritDoc} */
public class BaseSpace<SelfT extends BaseSpace<SelfT>> implements Space {

  private final String spaceName;
  private final DriverAdapter<?, SelfT> adapter;
  private final NBLabels labels;

  public BaseSpace(DriverAdapter<?, SelfT> adapter, String spaceName) {
    this(adapter, adapter.getLabels(), spaceName);
  }

  public BaseSpace(DriverAdapter<?, SelfT> adapter, NBLabels parentLabels, String spaceName) {
    this.spaceName = spaceName;
    this.adapter = adapter;
    this.labels = parentLabels.and("space", spaceName);
  }

  // Use a single regex pattern to match all placeholders in curly braces
  private final static Pattern pattern =
      Pattern.compile("\\{([^{}]+)\\}", Pattern.CASE_INSENSITIVE);

  /**
   Interpolate a template string with space-specific values.
   Replaces {SPACEID}, {SPACE}, and {SPACENAME} with their respective values.
   Also replaces any label value wrapped in curly braces with the corresponding label value (case
   insensitive).
   @param template
   The template string to interpolate
   @return The interpolated string
   */
  public String interpolate(String template) {
    if (template == null || template.isEmpty() || !template.contains("{")) {
      return template;
    }

    Matcher matcher = pattern.matcher(template);

    // Use StringBuffer for efficient string manipulation
    StringBuffer result = new StringBuffer();

    // Cache for label values used during this method execution
    Map<String, String> labelCache = new HashMap<>();

    // Special case for space name - cache it once
    String spaceNameValue = getName();

    while (matcher.find()) {
      String placeholder = matcher.group(1);
      String replacement;

      // Check if we've already looked up this label
      String placeholderLower = placeholder.toLowerCase();
      if (labelCache.containsKey(placeholderLower)) {
        replacement = labelCache.get(placeholderLower);
      } else {
        // Only access the labels map if needed
        replacement = null;
        if (labels != null) {
          Map<String, String> labelsMap = labels.asMap();
          // Find the label case-insensitively
          for (Map.Entry<String, String> entry : labelsMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(placeholder)) {
              replacement = entry.getValue();
              break;
            }
          }
        }

        // Cache the result (even if null)
        labelCache.put(placeholderLower, replacement != null ? replacement : matcher.group(0));

        // If no replacement found, keep the original placeholder
        if (replacement == null) {
          replacement = matcher.group(0);
        }
      }

      // Escape $ and \ in the replacement string to avoid issues with appendReplacement
      replacement = Matcher.quoteReplacement(replacement);
      matcher.appendReplacement(result, replacement);
    }

    matcher.appendTail(result);
    return result.toString();
  }

  @Override
  public String getName() {
    return spaceName;
  }

  /**
   Get the labels for this space, which include the parent adapter's labels
   extended with the space name.
   @return The labels for this space
   */
  public NBLabels getLabels() {
    return labels;
  }

  public static class BasicSpace extends BaseSpace<BasicSpace> implements Space {
    public BasicSpace(DriverAdapter<? extends CycleOp<?>, BasicSpace> adapter, long idx) {
      super(adapter, String.valueOf(idx));
    }

    public BasicSpace(
        DriverAdapter<? extends CycleOp<?>, BasicSpace> adapter,
        long idx,
        String originalName
    )
    {
      super(adapter, originalName);
    }

    public BasicSpace(
        DriverAdapter<? extends CycleOp<?>, BasicSpace> adapter,
        NBLabels parentLabels,
        long idx
    )
    {
      super(adapter, parentLabels, String.valueOf(idx));
    }

    public BasicSpace(
        DriverAdapter<? extends CycleOp<?>, BasicSpace> adapter,
        NBLabels parentLabels,
        long idx,
        String originalName
    )
    {
      super(adapter, parentLabels, originalName);
    }
  }
}
